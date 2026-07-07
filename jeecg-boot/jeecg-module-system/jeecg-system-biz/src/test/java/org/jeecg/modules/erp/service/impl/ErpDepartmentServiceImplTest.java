package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpDepartmentEntity;
import org.jeecg.modules.erp.mapper.ErpDepartmentEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpDepartmentServiceImplTest {

    @Test
    void queryByDateRequestsDepartmentsAndSavesInsideTransaction() {
        ErpDepartmentEntityMapper departmentMapper = mock(ErpDepartmentEntityMapper.class);
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        List<String> events = new ArrayList<>();
        List<ErpDepartmentEntity> departments = List.of(department("1001"), department("1002"));

        when(erpRequestService.request(any(QueryDto.class), eq(ErpDepartmentEntity.class))).thenAnswer(invocation -> {
            QueryDto queryDto = invocation.getArgument(0);
            QueryDetailDto detailDto = queryDto.getParameters().get(0);
            assertEquals("BD_Department", detailDto.getFormId());
            assertEquals("FModifyDate >='2026-06-01 00:00:00' and FModifyDate <='2026-06-30 23:59:59'",
                    detailDto.getFilterString());
            assertEquals("FModifyDate desc", detailDto.getOrderString());
            events.add("request");
            return departments;
        });
        when(departmentMapper.selectByIds(any(Collection.class))).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            assertEquals(Set.of("1001", "1002"), Set.copyOf(ids));
            events.add("select-existing");
            return List.of(department("1002"));
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            events.add("transaction-begin");
            Object result = ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null);
            events.add("transaction-end");
            return result;
        });

        RecordingErpDepartmentServiceImpl service = new RecordingErpDepartmentServiceImpl(events);
        ReflectionTestUtils.setField(service, "baseMapper", departmentMapper);
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        List<ErpDepartmentEntity> result = service.queryByDate("2026-06-01", "2026-06-30");

        assertSame(departments, result);
        assertEquals(List.of("1001"), service.inserted.stream().map(ErpDepartmentEntity::getFDEPTID).toList());
        assertEquals(List.of("1002"), service.updated.stream().map(ErpDepartmentEntity::getFDEPTID).toList());
        assertEquals(List.of("request", "transaction-begin", "select-existing", "save", "update", "transaction-end"),
                events);
        verify(erpRequestService).request(any(QueryDto.class), eq(ErpDepartmentEntity.class));
    }

    private static ErpDepartmentEntity department(String id) {
        ErpDepartmentEntity entity = new ErpDepartmentEntity();
        entity.setFDEPTID(id);
        return entity;
    }

    private static class RecordingErpDepartmentServiceImpl extends ErpDepartmentServiceImpl {
        private final List<ErpDepartmentEntity> inserted = new ArrayList<>();
        private final List<ErpDepartmentEntity> updated = new ArrayList<>();
        private final List<String> events;

        private RecordingErpDepartmentServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public boolean saveBatch(Collection<ErpDepartmentEntity> entityList) {
            events.add("save");
            inserted.addAll(entityList);
            return true;
        }

        @Override
        public boolean updateBatchById(Collection<ErpDepartmentEntity> entityList) {
            events.add("update");
            updated.addAll(entityList);
            return true;
        }
    }
}
