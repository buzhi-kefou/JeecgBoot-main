package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialEntityMapper;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpMaterialServiceImplTest {

    @Test
    void queryByDateLoadsExistingMaterialsInOneBatch() {
        ErpMaterialEntityMapper materialMapper = mock(ErpMaterialEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        List<ErpMaterialEntity> erpMaterials = List.of(material(1L), material(2L), material(3L));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpMaterialEntity.class))).thenReturn(erpMaterials);
        when(materialMapper.selectByIds(anyCollection())).thenReturn(List.of(material(2L)));

        RecordingErpMaterialServiceImpl service = new RecordingErpMaterialServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", materialMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", immediateTransactionTemplate());

        List<ErpMaterialEntity> result = service.queryByDate(null, null);

        assertSame(erpMaterials, result);
        assertEquals(List.of(1L, 3L), service.inserted.stream().map(ErpMaterialEntity::getMaterialId).toList());
        assertEquals(List.of(2L), service.updated.stream().map(ErpMaterialEntity::getMaterialId).toList());
        verify(materialMapper).selectByIds(argThat(ids -> ids.size() == 3 && ids.containsAll(List.of(1L, 2L, 3L))));
        verify(materialMapper, never()).selectById(any());
    }

    @Test
    void queryByDateRunsOnlyDatabaseWorkInsideTransactionTemplate() {
        ErpMaterialEntityMapper materialMapper = mock(ErpMaterialEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        List<String> events = new ArrayList<>();
        List<ErpMaterialEntity> erpMaterials = List.of(material(1L));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpMaterialEntity.class))).thenAnswer(invocation -> {
            events.add("request");
            return erpMaterials;
        });
        when(materialMapper.selectByIds(anyCollection())).thenAnswer(invocation -> {
            events.add("select");
            return List.of();
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        RecordingErpMaterialServiceImpl service = new RecordingErpMaterialServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", materialMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpMaterialEntity> result = service.queryByDate(null, null);

        assertSame(erpMaterials, result);
        assertEquals(List.of("request", "transaction-begin", "select", "save", "transaction-end"), events);
    }

    private static TransactionTemplate immediateTransactionTemplate() {
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null));
        return transactionTemplate;
    }

    private static ErpMaterialEntity material(Long materialId) {
        ErpMaterialEntity entity = new ErpMaterialEntity();
        entity.setMaterialId(materialId);
        return entity;
    }

    private static class RecordingErpMaterialServiceImpl extends ErpMaterialServiceImpl {
        private final List<ErpMaterialEntity> inserted = new ArrayList<>();
        private final List<ErpMaterialEntity> updated = new ArrayList<>();
        private final List<String> events;

        private RecordingErpMaterialServiceImpl() {
            this(null);
        }

        private RecordingErpMaterialServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public boolean saveBatch(Collection<ErpMaterialEntity> entityList) {
            if (events != null) {
                events.add("save");
            }
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpMaterialEntity> entityList) {
            if (events != null) {
                events.add("update");
            }
            updated.addAll(entityList);
            return true;
        }
    }
}
