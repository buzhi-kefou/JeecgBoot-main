package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.mapper.ErpProductionOrderEntityMapper;
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

class ErpProductionOrderServiceImplTest {

    @Test
    void queryByDateRequestsProductionOrdersAndSavesInsideTransaction() {
        ErpProductionOrderEntityMapper productionOrderMapper = mock(ErpProductionOrderEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        List<String> events = new ArrayList<>();
        List<ErpProductionOrderEntity> erpProductionOrders = List.of(productionOrder("2001"), productionOrder("2002"));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpProductionOrderEntity.class))).thenAnswer(invocation -> {
            QueryDto queryDto = invocation.getArgument(0);
            QueryDetailDto detailDto = queryDto.getParameters().get(0);
            assertEquals("PRD_MO", detailDto.getFormId());
            assertEquals("FModifyDate >='2026-06-01 00:00:00' and FModifyDate <='2026-06-30 23:59:59'",
                    detailDto.getFilterString());
            assertEquals("FModifyDate desc", detailDto.getOrderString());
            events.add("request");
            return erpProductionOrders;
        });
        when(productionOrderMapper.selectById("2001")).thenAnswer(invocation -> {
            events.add("select-2001");
            return null;
        });
        when(productionOrderMapper.selectById("2002")).thenAnswer(invocation -> {
            events.add("select-2002");
            return productionOrder("2002");
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        RecordingErpProductionOrderServiceImpl service = new RecordingErpProductionOrderServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", productionOrderMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpProductionOrderEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(erpProductionOrders, result);
        assertEquals(List.of("2001"), service.inserted.stream().map(ErpProductionOrderEntity::getFid).toList());
        assertEquals(List.of("2002"), service.updated.stream().map(ErpProductionOrderEntity::getFid).toList());
        assertEquals(List.of("request", "transaction-begin", "select-2001", "select-2002", "save", "update",
                "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpProductionOrderEntity.class));
    }

    private static ErpProductionOrderEntity productionOrder(String fid) {
        ErpProductionOrderEntity entity = new ErpProductionOrderEntity();
        entity.setFid(fid);
        return entity;
    }

    private static class RecordingErpProductionOrderServiceImpl extends ErpProductionOrderServiceImpl {
        private final List<ErpProductionOrderEntity> inserted = new ArrayList<>();
        private final List<ErpProductionOrderEntity> updated = new ArrayList<>();
        private final List<String> events;

        private RecordingErpProductionOrderServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public boolean saveBatch(Collection<ErpProductionOrderEntity> entityList) {
            events.add("save");
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpProductionOrderEntity> entityList) {
            events.add("update");
            updated.addAll(entityList);
            return true;
        }
    }
}
