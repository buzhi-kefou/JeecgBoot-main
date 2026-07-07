package org.jeecg.modules.erp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.ProductionOrderQuery;
import org.jeecg.modules.erp.mapper.ErpProductionOrderEntityMapper;
import org.jeecg.modules.erp.vo.ProductionOrderListVo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpProductionOrderListQueryTest {

    @Test
    void queryProductionOrderPageUsesCustomizeSelectPageAndReturnsListVo() throws Exception {
        ErpProductionOrderEntityMapper mapper = mock(ErpProductionOrderEntityMapper.class);
        ArgumentCaptor<IPage<ProductionOrderListVo>> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        ArgumentCaptor<ProductionOrderQuery> queryCaptor = ArgumentCaptor.forClass(ProductionOrderQuery.class);
        Page<ProductionOrderListVo> dbPage = new Page<>(2, 20, 1);
        dbPage.setRecords(List.of(productionOrder("MO001")));
        when(mapper.customizeSelectPage(any(IPage.class), any(ProductionOrderQuery.class))).thenReturn(dbPage);

        ErpProductionOrderServiceImpl service = new ErpProductionOrderServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        ProductionOrderQuery query = new ProductionOrderQuery();
        query.setBillNo("MO");
        query.setMaterialId("MAT001");
        query.setPlanStartBegin("2026-06-01");
        query.setPlanStartEnd("2026-06-30");
        query.setPageNo(2);
        query.setPageSize(20);

        Page<ProductionOrderListVo> result = service.queryProductionOrderPage(query);

        assertEquals(2, result.getCurrent());
        assertEquals(20, result.getSize());
        assertEquals("MO001", result.getRecords().get(0).getBillNo());
        assertEquals("FID001", result.getRecords().get(0).getFid());
        assertEquals("ENTRY001", result.getRecords().get(0).getLineEntryId());
        assertEquals("MAT001", result.getRecords().get(0).getMaterialNumber());
        assertEquals("3", result.getRecords().get(0).getLineStatus());
        String json = new ObjectMapper().writeValueAsString(result.getRecords().get(0));
        assertTrue(json.contains("\"billNo\""));
        assertTrue(json.contains("\"fid\""));
        assertTrue(json.contains("\"lineEntryId\""));
        assertFalse(json.contains("\"FBillNo\""));
        assertFalse(json.contains("\"FID\""));
        verify(mapper).customizeSelectPage(pageCaptor.capture(), queryCaptor.capture());
        assertEquals(2, pageCaptor.getValue().getCurrent());
        assertEquals(20, pageCaptor.getValue().getSize());
        assertEquals("MO", queryCaptor.getValue().getBillNo());
        assertEquals("MAT001", queryCaptor.getValue().getMaterialId());
        assertEquals("2026-06-01", queryCaptor.getValue().getPlanStartBegin());
        assertEquals("2026-06-30", queryCaptor.getValue().getPlanStartEnd());
    }

    private static ProductionOrderListVo productionOrder(String billNo) {
        ProductionOrderListVo vo = new ProductionOrderListVo();
        vo.setFid("FID001");
        vo.setBillNo(billNo);
        vo.setLineEntryId("ENTRY001");
        vo.setMaterialNumber("MAT001");
        vo.setLineStatus("3");
        return vo;
    }
}
