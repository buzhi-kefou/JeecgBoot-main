package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpSalesDeliveryOrderEntity;
import org.jeecg.modules.erp.mapper.ErpSalesDeliveryOrderEntityMapper;
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

class ErpSalesDeliveryOrderServiceImplTest {

    @Test
    void queryByDateRequestsSalesDeliveryOrdersAndSavesInsideTransaction() {
        ErpSalesDeliveryOrderEntityMapper salesDeliveryOrderMapper = mock(ErpSalesDeliveryOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        List<String> events = new ArrayList<>();
        List<ErpSalesDeliveryOrderEntity> erpSalesDeliveryOrders =
                List.of(salesDeliveryOrder("3001"), salesDeliveryOrder("3002"));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpSalesDeliveryOrderEntity.class))).thenAnswer(invocation -> {
            QueryDto queryDto = invocation.getArgument(0);
            QueryDetailDto detailDto = queryDto.getParameters().get(0);
            assertEquals("SAL_DELIVERYNOTICE", detailDto.getFormId());
            assertEquals("FModifyDate >='2026-06-01 00:00:00' and FModifyDate <='2026-06-30 23:59:59'",
                    detailDto.getFilterString());
            assertEquals("FModifyDate desc", detailDto.getOrderString());
            events.add("request");
            return erpSalesDeliveryOrders;
        });
        when(salesDeliveryOrderMapper.selectById("3001")).thenAnswer(invocation -> {
            events.add("select-3001");
            return null;
        });
        when(salesDeliveryOrderMapper.selectById("3002")).thenAnswer(invocation -> {
            events.add("select-3002");
            return salesDeliveryOrder("3002");
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        RecordingErpSalesDeliveryOrderServiceImpl service = new RecordingErpSalesDeliveryOrderServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", salesDeliveryOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpSalesDeliveryOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpSalesDeliveryOrders, result);
        assertEquals(List.of("3001"), service.inserted.stream().map(ErpSalesDeliveryOrderEntity::getFid).toList());
        assertEquals(List.of("3002"), service.updated.stream().map(ErpSalesDeliveryOrderEntity::getFid).toList());
        assertEquals(List.of("request", "transaction-begin", "select-3001", "select-3002", "save", "update",
                "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpSalesDeliveryOrderEntity.class));
    }

    private static ErpSalesDeliveryOrderEntity salesDeliveryOrder(String fid) {
        ErpSalesDeliveryOrderEntity entity = new ErpSalesDeliveryOrderEntity();
        entity.setFid(fid);
        return entity;
    }

    private static class RecordingErpSalesDeliveryOrderServiceImpl extends ErpSalesDeliveryOrderServiceImpl {
        private final List<ErpSalesDeliveryOrderEntity> inserted = new ArrayList<>();
        private final List<ErpSalesDeliveryOrderEntity> updated = new ArrayList<>();
        private final List<String> events;

        private RecordingErpSalesDeliveryOrderServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public boolean saveBatch(Collection<ErpSalesDeliveryOrderEntity> entityList) {
            events.add("save");
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpSalesDeliveryOrderEntity> entityList) {
            events.add("update");
            updated.addAll(entityList);
            return true;
        }
    }
}
