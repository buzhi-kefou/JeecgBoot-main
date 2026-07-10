package org.jeecg.modules.erp.service.impl;

import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;
import org.jeecg.modules.erp.entity.ErpMaterialBillLineEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialBillEntityMapper;
import org.jeecg.modules.erp.mapper.ErpMaterialBillLineEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpMaterialBillServiceImplTest {

    @Test
    void queryByDateRequestsBomFormWithMaterialBillFields() {
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        when(erpRequestService.request(any(QueryDto.class), eq(ErpMaterialBillEntity.class))).thenReturn(List.of());

        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null));

        ErpMaterialBillServiceImpl service = new ErpMaterialBillServiceImpl();
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);

        service.queryByDate(null, null);

        ArgumentCaptor<QueryDto> queryCaptor = ArgumentCaptor.forClass(QueryDto.class);
        verify(erpRequestService).request(queryCaptor.capture(), eq(ErpMaterialBillEntity.class));
        QueryDetailDto detail = queryCaptor.getValue().getParameters().get(0);
        assertEquals("ENG_BOM", detail.getFormId());
        assertEquals("FModifyDate desc", detail.getOrderString());
        assertNotNull(detail.getFieldKeys());
        assertEquals(true, detail.getFieldKeys().contains("FTreeEntity_FENTRYID"));
    }

    @Test
    void queryByDateUpsertsDeduplicatedMaterialBillsAndLines() {
        ErpRequestService erpRequestService = mock(ErpRequestService.class);
        ErpMaterialBillEntity firstRow = materialBill(1L, line(10L));
        ErpMaterialBillEntity secondRow = materialBill(1L, line(11L));
        when(erpRequestService.request(any(QueryDto.class), eq(ErpMaterialBillEntity.class))).thenReturn(List.of(firstRow, secondRow));

        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null));
        ErpMaterialBillEntityMapper materialBillMapper = mock(ErpMaterialBillEntityMapper.class);
        ErpMaterialBillLineEntityMapper lineMapper = mock(ErpMaterialBillLineEntityMapper.class);

        ErpMaterialBillServiceImpl service = new ErpMaterialBillServiceImpl();
        ReflectionTestUtils.setField(service, "erpRequestService", erpRequestService);
        ReflectionTestUtils.setField(service, "transactionTemplate", transactionTemplate);
        ReflectionTestUtils.setField(service, "baseMapper", materialBillMapper);
        ReflectionTestUtils.setField(service, "materialBillLineEntityMapper", lineMapper);

        service.queryByDate(null, null);

        ArgumentCaptor<List<ErpMaterialBillEntity>> billCaptor = ArgumentCaptor.forClass(List.class);
        verify(materialBillMapper).upsertBatch(billCaptor.capture());
        assertEquals(1, billCaptor.getValue().size());
        assertEquals(1L, billCaptor.getValue().get(0).getId());

        ArgumentCaptor<List<ErpMaterialBillLineEntity>> lineCaptor = ArgumentCaptor.forClass(List.class);
        verify(lineMapper).upsertBatch(lineCaptor.capture());
        assertEquals(2, lineCaptor.getValue().size());
        assertEquals(1L, lineCaptor.getValue().get(0).getBillId());
        assertEquals(10L, lineCaptor.getValue().get(0).getId());
        assertEquals(1L, lineCaptor.getValue().get(1).getBillId());
        assertEquals(11L, lineCaptor.getValue().get(1).getId());
    }

    private static ErpMaterialBillEntity materialBill(Long id, ErpMaterialBillLineEntity line) {
        ErpMaterialBillEntity entity = new ErpMaterialBillEntity();
        entity.setId(id);
        entity.setEntries(List.of(line));
        return entity;
    }

    private static ErpMaterialBillLineEntity line(Long id) {
        ErpMaterialBillLineEntity line = new ErpMaterialBillLineEntity();
        line.setId(id);
        return line;
    }
}
