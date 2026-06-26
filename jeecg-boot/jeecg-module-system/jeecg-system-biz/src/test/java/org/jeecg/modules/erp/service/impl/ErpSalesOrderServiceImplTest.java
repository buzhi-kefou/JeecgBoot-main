package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpSalesOrderEntity;
import org.jeecg.modules.erp.mapper.ErpSalesOrderEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpSalesOrderServiceImplTest {

    @Test
    void queryByDateRequestsSaleOrdersAndSavesInsideTransaction() {
        ErpSalesOrderEntityMapper salesOrderMapper = mock(ErpSalesOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        List<String> events = new ArrayList<>();
        List<ErpSalesOrderEntity> erpSalesOrders = List.of(salesOrder("1001"), salesOrder("1002"));

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
        when(salesOrderMapper.selectById("1001")).thenAnswer(invocation -> {
            events.add("select-1001");
            return null;
        });
        when(salesOrderMapper.selectById("1002")).thenAnswer(invocation -> {
            events.add("select-1002");
            return salesOrder("1002");
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        RecordingErpSalesOrderServiceImpl service = new RecordingErpSalesOrderServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", salesOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpSalesOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpSalesOrders, result);
        assertEquals(List.of("1001"), service.inserted.stream().map(ErpSalesOrderEntity::getFid).toList());
        assertEquals(List.of("1002"), service.updated.stream().map(ErpSalesOrderEntity::getFid).toList());
        assertEquals(List.of("request", "transaction-begin", "select-1001", "select-1002", "save", "update",
                "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpSalesOrderEntity.class));
    }

    private static ErpSalesOrderEntity salesOrder(String fid) {
        ErpSalesOrderEntity entity = new ErpSalesOrderEntity();
        entity.setFid(fid);
        return entity;
    }

    private static class RecordingErpSalesOrderServiceImpl extends ErpSalesOrderServiceImpl {
        private final List<ErpSalesOrderEntity> inserted = new ArrayList<>();
        private final List<ErpSalesOrderEntity> updated = new ArrayList<>();
        private final List<String> events;

        private RecordingErpSalesOrderServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public boolean saveBatch(Collection<ErpSalesOrderEntity> entityList) {
            events.add("save");
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpSalesOrderEntity> entityList) {
            events.add("update");
            updated.addAll(entityList);
            return true;
        }
    }
}
