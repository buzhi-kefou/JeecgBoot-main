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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        when(productionOrderMapper.upsertBatch(any())).thenAnswer(invocation -> {
            events.add("upsert");
            return 2;
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
        assertEquals(List.of("request", "transaction-begin", "upsert", "transaction-end"), events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpProductionOrderEntity.class));
        verify(productionOrderMapper).upsertBatch(erpProductionOrders);
        verifyNoMoreInteractions(productionOrderMapper);
    }

    private static ErpProductionOrderEntity productionOrder(String fid) {
        ErpProductionOrderEntity entity = new ErpProductionOrderEntity();
        entity.setFid(fid);
        return entity;
    }

    private static class RecordingErpProductionOrderServiceImpl extends ErpProductionOrderServiceImpl {
        private final List<String> events;

        private RecordingErpProductionOrderServiceImpl(List<String> events) {
            this.events = events;
        }
    }
}
