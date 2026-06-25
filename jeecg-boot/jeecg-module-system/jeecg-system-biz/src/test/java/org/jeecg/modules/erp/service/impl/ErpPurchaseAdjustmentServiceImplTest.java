package org.jeecg.modules.erp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceLineRow;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceQuery;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentEntityMapper;
import org.jeecg.modules.erp.vo.MaterialSupplierPriceVo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class ErpPurchaseAdjustmentServiceImplTest {

    @Test
    void queryMaterialSupplierPriceGroupsByMaterialAndSupplierAndFiltersByHeadApproveDate() throws Exception {
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);

        MaterialSupplierPriceLineRow januaryPrice = row("1001", "MAT-001", "物料A", "规格A",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30), "10.20",
                LocalDateTime.of(2026, 1, 1, 9, 0));
        MaterialSupplierPriceLineRow mayPrice = row("1001", "MAT-001", "物料A", "规格A",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 31), "11.50",
                LocalDateTime.of(2026, 5, 1, 9, 0));
        MaterialSupplierPriceLineRow julyPrice = row("1001", "MAT-001", "物料A", "规格A",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), "12.80",
                LocalDateTime.of(2026, 7, 1, 9, 0));
        MaterialSupplierPriceLineRow secondMaterialPrice = row("1002", "MAT-001", "物料A-替代", "规格B",
                "使用组织二", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 31), "21.00",
                LocalDateTime.of(2026, 5, 1, 9, 0));

        when(adjustmentMapper.countMaterialSupplierPriceGroups(any(), any(), any(), any(), any())).thenReturn(2L);
        when(adjustmentMapper.selectMaterialSupplierPriceGroupPage(any(), any(), any(), any(), any(), any(Long.class), any(Integer.class)))
                .thenReturn(List.of(januaryPrice, secondMaterialPrice));
        when(adjustmentMapper.selectMaterialSupplierPriceRowsByGroups(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(januaryPrice, mayPrice, julyPrice, secondMaterialPrice));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", adjustmentMapper);

        MaterialSupplierPriceQuery query = new MaterialSupplierPriceQuery();
        query.setMaterialCode("MAT-001");
        query.setUseOrgId("10");
        query.setYear(2026);
        query.setPageNo(1);
        query.setPageSize(10);

        Page<MaterialSupplierPriceVo> result = service.queryMaterialSupplierPrice(query);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());

        MaterialSupplierPriceVo material1001Price = findByMaterialId(result.getRecords(), "1001");
        assertEquals("MAT-001", readField(material1001Price, "materialCode"));
        assertEquals("物料A", readField(material1001Price, "materialName"));
        assertEquals("使用组织一", readField(material1001Price, "useOrgId"));
        assertEquals("SUP-001", readField(material1001Price, "supplierCode"));
        assertEquals("供应商一", material1001Price.getSupplierName());
        assertEquals(new BigDecimal("10.20"), material1001Price.getMonthlyPrices().get(1));
        assertEquals(new BigDecimal("10.20"), material1001Price.getMonthlyPrices().get(2));
        assertEquals(new BigDecimal("10.20"), material1001Price.getMonthlyPrices().get(3));
        assertEquals(new BigDecimal("10.20"), material1001Price.getMonthlyPrices().get(4));
        assertEquals(new BigDecimal("11.50"), material1001Price.getMonthlyPrices().get(5));
        assertEquals(new BigDecimal("11.50"), material1001Price.getMonthlyPrices().get(6));
        assertEquals(new BigDecimal("12.80"), material1001Price.getMonthlyPrices().get(7));
        assertEquals(new BigDecimal("12.80"), material1001Price.getMonthlyPrices().get(12));
        assertEquals(3, material1001Price.getRecordCount());

        MaterialSupplierPriceVo material1002Price = findByMaterialId(result.getRecords(), "1002");
        assertEquals(new BigDecimal("21.00"), material1002Price.getMonthlyPrices().get(5));
        assertEquals(1, material1002Price.getRecordCount());

        assertTrue(result.getRecords().stream()
                .noneMatch(vo -> vo.getMonthlyPrices().containsValue(new BigDecimal("99.00"))));
    }

    @Test
    void queryMaterialSupplierPriceFetchesRowsOnlyForPagedGroups() {
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);

        MaterialSupplierPriceLineRow firstGroup = row("1001", "MAT-001", "物料A", "规格A",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "10.20",
                LocalDateTime.of(2026, 1, 1, 9, 0));
        MaterialSupplierPriceLineRow secondGroup = row("1002", "MAT-002", "物料B", "规格B",
                "使用组织一", "SUP-2", "SUP-002", "供应商二",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 31), "20.00",
                LocalDateTime.of(2026, 2, 1, 9, 0));

        when(adjustmentMapper.countMaterialSupplierPriceGroups(
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2027, 1, 1)),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(50L);
        when(adjustmentMapper.selectMaterialSupplierPriceGroupPage(
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2027, 1, 1)),
                eq(null),
                eq(null),
                eq(null),
                eq(10L),
                eq(10)
        )).thenReturn(List.of(firstGroup, secondGroup));
        when(adjustmentMapper.selectMaterialSupplierPriceRowsByGroups(
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2027, 1, 1)),
                eq(null),
                eq(null),
                eq(null),
                eq(List.of(firstGroup, secondGroup))
        )).thenReturn(List.of(firstGroup, secondGroup));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", adjustmentMapper);

        MaterialSupplierPriceQuery query = new MaterialSupplierPriceQuery();
        query.setYear(2026);
        query.setPageNo(2);
        query.setPageSize(10);

        Page<MaterialSupplierPriceVo> result = service.queryMaterialSupplierPrice(query);

        assertEquals(50, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("1001", result.getRecords().get(0).getMaterialId());
        assertEquals("1002", result.getRecords().get(1).getMaterialId());
        verify(adjustmentMapper, never()).selectMaterialSupplierPriceRows(any(), any(), any(), any(), any());
    }

    @Test
    void queryMaterialSupplierPriceUsesLaterApprovedAdjustmentFromItsApprovalMonth() {
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);

        MaterialSupplierPriceLineRow septemberPrice = row("1128", "05.02.006.01128", "测试物料", "规格",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2022, 9, 29), LocalDate.of(2100, 1, 1), "0.4750440000",
                LocalDateTime.of(2022, 9, 29, 16, 2, 30));
        MaterialSupplierPriceLineRow novemberPrice = row("1128", "05.02.006.01128", "测试物料", "规格",
                "使用组织一", "SUP-1", "SUP-001", "供应商一",
                LocalDate.of(2022, 9, 29), LocalDate.of(2100, 1, 1), "0.3192920000",
                LocalDateTime.of(2022, 11, 19, 16, 37, 16));

        when(adjustmentMapper.countMaterialSupplierPriceGroups(any(), any(), any(), any(), any())).thenReturn(1L);
        when(adjustmentMapper.selectMaterialSupplierPriceGroupPage(any(), any(), any(), any(), any(), any(Long.class), any(Integer.class)))
                .thenReturn(List.of(septemberPrice));
        when(adjustmentMapper.selectMaterialSupplierPriceRowsByGroups(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(septemberPrice, novemberPrice));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", adjustmentMapper);

        MaterialSupplierPriceQuery query = new MaterialSupplierPriceQuery();
        query.setMaterialCode("05.02.006.01128");
        query.setYear(2022);
        query.setPageNo(1);
        query.setPageSize(10);

        Page<MaterialSupplierPriceVo> result = service.queryMaterialSupplierPrice(query);

        Map<Integer, BigDecimal> monthlyPrices = result.getRecords().get(0).getMonthlyPrices();
        assertEquals(BigDecimal.ZERO, monthlyPrices.get(8));
        assertEquals(new BigDecimal("0.4750440000"), monthlyPrices.get(9));
        assertEquals(new BigDecimal("0.4750440000"), monthlyPrices.get(10));
        assertEquals(new BigDecimal("0.3192920000"), monthlyPrices.get(11));
        assertEquals(new BigDecimal("0.3192920000"), monthlyPrices.get(12));
    }

    @Test
    void queryMaterialSupplierPriceReturnsEmptyPageWhenNoPriceGroup() {
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);
        when(adjustmentMapper.countMaterialSupplierPriceGroups(any(), any(), any(), any(), any())).thenReturn(0L);

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", adjustmentMapper);

        MaterialSupplierPriceQuery query = new MaterialSupplierPriceQuery();
        query.setYear(2026);
        query.setPageNo(2);
        query.setPageSize(10);

        Page<MaterialSupplierPriceVo> result = service.queryMaterialSupplierPrice(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    private static MaterialSupplierPriceLineRow row(String materialId, String materialCode, String materialName,
                                                   String specification, String useOrgId, String supplierId,
                                                   String supplierCode, String supplierName, LocalDate effectiveDate,
                                                   LocalDate expiryDate, String afterTaxPrice,
                                                   LocalDateTime approveDate) {
        MaterialSupplierPriceLineRow row = new MaterialSupplierPriceLineRow();
        row.setMaterialId(materialId);
        row.setMaterialCode(materialCode);
        row.setMaterialName(materialName);
        row.setSpecification(specification);
        row.setUseOrgId(useOrgId);
        row.setSupplierId(supplierId);
        row.setSupplierCode(supplierCode);
        row.setSupplierName(supplierName);
        row.setEffectiveDate(effectiveDate);
        row.setExpiryDate(expiryDate);
        row.setAfterTaxPrice(new BigDecimal(afterTaxPrice));
        row.setApproveDate(approveDate);
        return row;
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static MaterialSupplierPriceVo findByMaterialId(List<MaterialSupplierPriceVo> result, String materialId) throws Exception {
        for (MaterialSupplierPriceVo vo : result) {
            if (materialId.equals(readField(vo, "materialId"))) {
                return vo;
            }
        }
        throw new AssertionError("Expected materialId " + materialId + " in result");
    }
}
