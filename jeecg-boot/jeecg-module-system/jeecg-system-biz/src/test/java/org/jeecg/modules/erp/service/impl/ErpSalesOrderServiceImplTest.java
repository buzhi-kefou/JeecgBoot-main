package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpSalesOrderEntity;
import org.jeecg.modules.erp.entity.ErpSalesOrderFinanceEntity;
import org.jeecg.modules.erp.mapper.ErpSalesOrderEntityMapper;
import org.jeecg.modules.erp.mapper.ErpSalesOrderFinanceEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpSalesOrderServiceImplTest {

    @Test
    void queryByDateRequestsSaleOrdersAndSavesInsideTransaction() {
        ErpSalesOrderEntityMapper salesOrderMapper = mock(ErpSalesOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        ErpSalesOrderFinanceEntityMapper financeMapper = mock(ErpSalesOrderFinanceEntityMapper.class);
        List<String> events = new ArrayList<>();
        List<ErpSalesOrderEntity> erpSalesOrders = List.of(
                salesOrder("1001", finance(9001L)),
                salesOrder("1002", finance(9002L)));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpSalesOrderEntity.class))).thenAnswer(invocation -> {
            QueryDto queryDto = invocation.getArgument(0);
            QueryDetailDto detailDto = queryDto.getParameters().get(0);
            assertEquals("SAL_SaleOrder", detailDto.getFormId());
            assertEquals("FModifyDate >='2026-06-01 00:00:00' and FModifyDate <='2026-06-30 23:59:59'",
                    detailDto.getFilterString());
            assertEquals("FModifyDate desc", detailDto.getOrderString());
            events.add("request");
            return erpSalesOrders;
        });
        when(salesOrderMapper.upsertBatch(any(List.class))).thenAnswer(invocation -> {
            events.add("upsert-batch");
            return 2;
        });
        when(financeMapper.upsertBatch(any(List.class))).thenAnswer(invocation -> {
            events.add("finance-upsert-batch");
            return 2;
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        ErpSalesOrderServiceImpl service = new ErpSalesOrderServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", salesOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);
        ReflectionTestUtils.setField(service, "financeMapper", financeMapper);

        List<ErpSalesOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpSalesOrders, result);
        ArgumentCaptor<List<ErpSalesOrderEntity>> salesOrders = ArgumentCaptor.forClass(List.class);
        verify(salesOrderMapper).upsertBatch(salesOrders.capture());
        assertEquals(List.of("1001", "1002"), salesOrders.getValue().stream()
                .map(ErpSalesOrderEntity::getFid).toList());
        verify(salesOrderMapper, never()).selectByIds(any(Collection.class));
        ArgumentCaptor<List<ErpSalesOrderFinanceEntity>> finances = ArgumentCaptor.forClass(List.class);
        verify(financeMapper).upsertBatch(finances.capture());
        assertEquals(List.of(9001L, 9002L), finances.getValue().stream()
                .map(ErpSalesOrderFinanceEntity::getEntryId).toList());
        assertEquals(List.of("1001", "1002"), finances.getValue().stream()
                .map(ErpSalesOrderFinanceEntity::getPid).toList());
        assertEquals(List.of("request", "transaction-begin", "upsert-batch",
                "finance-upsert-batch", "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpSalesOrderEntity.class));
    }

    @Test
    void queryByDateSkipsMissingOrUnidentifiableFinanceEntities() {
        ErpSalesOrderEntityMapper salesOrderMapper = mock(ErpSalesOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        ErpSalesOrderFinanceEntityMapper financeMapper = mock(ErpSalesOrderFinanceEntityMapper.class);
        ErpSalesOrderFinanceEntity financeWithoutId = finance(null);
        List<ErpSalesOrderEntity> erpSalesOrders = List.of(
                salesOrder("1001", null), salesOrder("1002", financeWithoutId));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpSalesOrderEntity.class)))
                .thenReturn(erpSalesOrders);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null));

        ErpSalesOrderServiceImpl service = new ErpSalesOrderServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", salesOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);
        ReflectionTestUtils.setField(service, "financeMapper", financeMapper);

        service.queryByDate("2026-06-01", "2026-06-30");

        verify(financeMapper, never()).upsertBatch(any(List.class));
    }

    @Test
    void queryByDateUpsertsSalesOrdersAndFinancesInChunks() {
        ErpSalesOrderEntityMapper salesOrderMapper = mock(ErpSalesOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        ErpSalesOrderFinanceEntityMapper financeMapper = mock(ErpSalesOrderFinanceEntityMapper.class);
        List<ErpSalesOrderEntity> erpSalesOrders = IntStream.rangeClosed(1, 201)
                .mapToObj(index -> salesOrder(String.valueOf(index), finance((long) index)))
                .toList();

        when(erpRequestService.request(any(QueryDto.class), eq(ErpSalesOrderEntity.class)))
                .thenReturn(erpSalesOrders);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null));

        ErpSalesOrderServiceImpl service = new ErpSalesOrderServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", salesOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);
        ReflectionTestUtils.setField(service, "financeMapper", financeMapper);

        service.queryByDate(null, null);

        ArgumentCaptor<List<ErpSalesOrderEntity>> chunks = ArgumentCaptor.forClass(List.class);
        verify(salesOrderMapper, times(2)).upsertBatch(chunks.capture());
        assertEquals(List.of(200, 1), chunks.getAllValues().stream().map(List::size).toList());
        verify(salesOrderMapper, never()).selectByIds(any(Collection.class));
        ArgumentCaptor<List<ErpSalesOrderFinanceEntity>> financeChunks = ArgumentCaptor.forClass(List.class);
        verify(financeMapper, times(2)).upsertBatch(financeChunks.capture());
        assertEquals(List.of(200, 1), financeChunks.getAllValues().stream().map(List::size).toList());
        assertEquals("201", financeChunks.getAllValues().get(1).get(0).getPid());
    }

    private static ErpSalesOrderEntity salesOrder(String fid) {
        return salesOrder(fid, null);
    }

    private static ErpSalesOrderEntity salesOrder(String fid, ErpSalesOrderFinanceEntity finance) {
        ErpSalesOrderEntity entity = new ErpSalesOrderEntity();
        entity.setFid(fid);
        entity.setFinanceEntity(finance);
        return entity;
    }

    private static ErpSalesOrderFinanceEntity finance(Long entryId) {
        ErpSalesOrderFinanceEntity entity = new ErpSalesOrderFinanceEntity();
        entity.setEntryId(entryId);
        return entity;
    }

}
