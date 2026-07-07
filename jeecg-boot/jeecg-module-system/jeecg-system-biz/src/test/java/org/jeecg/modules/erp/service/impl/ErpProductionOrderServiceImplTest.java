package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.entity.ErpProductionOrderLineEntity;
import org.jeecg.modules.erp.mapper.ErpProductionOrderEntityMapper;
import org.jeecg.modules.erp.mapper.ErpProductionOrderLineEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ErpProductionOrderServiceImplTest {

    private ErpProductionOrderEntityMapper productionOrderMapper;
    private ErpProductionOrderLineEntityMapper productionOrderLineMapper;
    private ErpRequestService erpRequestService;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        productionOrderMapper = mock(ErpProductionOrderEntityMapper.class);
        productionOrderLineMapper = mock(ErpProductionOrderLineEntityMapper.class);
        erpRequestService = mock(ErpRequestService.class);
        transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(invocation -> ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null))
                .when(transactionTemplate).execute(any());
    }

    @Test
    void queryByDateRequestsProductionOrdersAndSavesInsideTransaction() {
        List<String> events = new ArrayList<>();
        List<ErpProductionOrderEntity> erpProductionOrders = List.of(productionOrder("2001"), productionOrder("2002"));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpProductionOrderEntity.class))).thenAnswer(invocation -> {
            QueryDto queryDto = invocation.getArgument(0);
            QueryDetailDto detailDto = queryDto.getParameters().get(0);
            assertEquals("PRD_MO", detailDto.getFormId());
            assertEquals(" FDocumentStatus = 'C' and FModifyDate >='2026-06-01 00:00:00' and FModifyDate <='2026-06-30 23:59:59'",
                    detailDto.getFilterString());
            assertEquals("FModifyDate desc", detailDto.getOrderString());
            events.add("request");
            return erpProductionOrders;
        });
        when(productionOrderMapper.upsertBatch(any())).thenAnswer(invocation -> {
            events.add("upsert");
            return 2;
        });
        doAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        }).when(transactionTemplate).execute(any());

        RecordingErpProductionOrderServiceImpl service = new RecordingErpProductionOrderServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", productionOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpProductionOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpProductionOrders, result);
        assertEquals(List.of("request", "transaction-begin", "upsert", "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpProductionOrderEntity.class));
        verify(productionOrderMapper).upsertBatch(erpProductionOrders);
        verifyNoMoreInteractions(productionOrderMapper);
        verifyNoInteractions(productionOrderLineMapper);
    }

    @Test
    void queryByDateMergesDuplicateProductionOrdersBeforeUpsert() {
        ErpProductionOrderLineEntity firstLine = productionOrderLine("10");
        ErpProductionOrderLineEntity secondLine = productionOrderLine("20");
        ErpProductionOrderEntity firstOrder = productionOrder("2001");
        firstOrder.setEntries(List.of(firstLine));
        ErpProductionOrderEntity secondOrder = productionOrder("2001");
        secondOrder.setEntries(List.of(secondLine));
        ErpProductionOrderEntity anotherOrder = productionOrder("2002");
        List<ErpProductionOrderEntity> erpProductionOrders = List.of(firstOrder, secondOrder, anotherOrder);
        when(erpRequestService.request(any(QueryDto.class), eq(ErpProductionOrderEntity.class)))
                .thenReturn(erpProductionOrders);
        when(productionOrderMapper.upsertBatch(any())).thenReturn(2);
        RecordingErpProductionOrderServiceImpl service = newService(new ArrayList<>());

        List<ErpProductionOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpProductionOrders, result);
        ArgumentCaptor<List<ErpProductionOrderEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(productionOrderMapper).upsertBatch(captor.capture());
        List<ErpProductionOrderEntity> upsertedOrders = captor.getValue();
        assertEquals(List.of("2001", "2002"), upsertedOrders.stream().map(ErpProductionOrderEntity::getFid).toList());
        assertEquals(List.of(firstLine, secondLine), upsertedOrders.get(0).getEntries());
    }

    @Test
    void queryByDateReplacesProductionOrderLinesInsideSameTransaction() {
        List<String> events = new ArrayList<>();
        ErpProductionOrderLineEntity firstLine = productionOrderLine("10");
        ErpProductionOrderLineEntity secondLine = productionOrderLine("20");
        ErpProductionOrderEntity order = productionOrder("2001");
        order.setEntries(List.of(firstLine, secondLine));
        List<ErpProductionOrderEntity> erpProductionOrders = List.of(order);
        when(erpRequestService.request(any(QueryDto.class), eq(ErpProductionOrderEntity.class)))
                .thenReturn(erpProductionOrders);
        when(productionOrderMapper.upsertBatch(any())).thenAnswer(invocation -> {
            events.add("upsert-main");
            return 1;
        });
        when(productionOrderLineMapper.deleteBatchIds(any())).thenAnswer(invocation -> {
            events.add("delete-lines");
            return 2;
        });
        when(productionOrderLineMapper.insert(any(ErpProductionOrderLineEntity.class))).thenAnswer(invocation -> {
            events.add("insert-line");
            return 1;
        });
        doAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        }).when(transactionTemplate).execute(any());
        RecordingErpProductionOrderServiceImpl service = newService(events);

        service.queryByDate("2026-06-01", "2026-06-30");

        verify(productionOrderMapper).upsertBatch(erpProductionOrders);
        verify(productionOrderLineMapper).deleteBatchIds(List.of("10", "20"));
        verify(productionOrderLineMapper).insert(firstLine);
        verify(productionOrderLineMapper).insert(secondLine);
        assertEquals("2001", firstLine.getPId());
        assertEquals("2001", secondLine.getPId());
        assertEquals(List.of("transaction-begin", "upsert-main", "delete-lines", "insert-line", "insert-line", "transaction-end"),
                events);
    }

    private static ErpProductionOrderEntity productionOrder(String fid) {
        ErpProductionOrderEntity entity = new ErpProductionOrderEntity();
        entity.setFid(fid);
        return entity;
    }

    private static ErpProductionOrderLineEntity productionOrderLine(String entryId) {
        ErpProductionOrderLineEntity entity = new ErpProductionOrderLineEntity();
        entity.setFEntryId(entryId);
        return entity;
    }

    private RecordingErpProductionOrderServiceImpl newService(List<String> events) {
        RecordingErpProductionOrderServiceImpl service = new RecordingErpProductionOrderServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", productionOrderMapper);
        ReflectionTestUtils.setField(service, "entryMapper", productionOrderLineMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);
        return service;
    }

    private static class RecordingErpProductionOrderServiceImpl extends ErpProductionOrderServiceImpl {
        private final List<String> events;

        private RecordingErpProductionOrderServiceImpl(List<String> events) {
            this.events = events;
        }
    }
}
