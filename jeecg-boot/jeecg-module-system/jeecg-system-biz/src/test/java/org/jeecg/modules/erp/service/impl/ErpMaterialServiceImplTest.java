package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        when(materialMapper.selectBatchIds(anyCollection())).thenReturn(List.of(material(2L)));

        RecordingErpMaterialServiceImpl service = new RecordingErpMaterialServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", materialMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);

        List<ErpMaterialEntity> result = service.queryByDate(null, null);

        assertSame(erpMaterials, result);
        assertEquals(List.of(1L, 3L), service.inserted.stream().map(ErpMaterialEntity::getMaterialId).toList());
        assertEquals(List.of(2L), service.updated.stream().map(ErpMaterialEntity::getMaterialId).toList());
        verify(materialMapper).selectBatchIds(argThat(ids -> ids.size() == 3 && ids.containsAll(List.of(1L, 2L, 3L))));
        verify(materialMapper, never()).selectById(any());
    }

    private static ErpMaterialEntity material(Long materialId) {
        ErpMaterialEntity entity = new ErpMaterialEntity();
        entity.setMaterialId(materialId);
        return entity;
    }

    private static class RecordingErpMaterialServiceImpl extends ErpMaterialServiceImpl {
        private final List<ErpMaterialEntity> inserted = new ArrayList<>();
        private final List<ErpMaterialEntity> updated = new ArrayList<>();

        @Override
        public boolean saveBatch(Collection<ErpMaterialEntity> entityList) {
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpMaterialEntity> entityList) {
            updated.addAll(entityList);
            return true;
        }
    }
}
