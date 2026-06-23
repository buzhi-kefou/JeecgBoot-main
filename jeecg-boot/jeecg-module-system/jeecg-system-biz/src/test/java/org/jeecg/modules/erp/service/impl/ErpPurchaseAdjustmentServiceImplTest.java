package org.jeecg.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceQuery;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentLineEntity;
import org.jeecg.modules.erp.entity.ErpSupplierEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialEntityMapper;
import org.jeecg.modules.erp.mapper.ErpOrgEntityMapper;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentEntityMapper;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentLineEntityMapper;
import org.jeecg.modules.erp.mapper.ErpSupplierEntityMapper;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErpPurchaseAdjustmentServiceImplTest {

    @Test
    void queryMaterialSupplierPriceGroupsByMaterialAndSupplierAndFiltersByHeadApproveDate() throws Exception {
        ErpMaterialEntityMapper materialMapper = mock(ErpMaterialEntityMapper.class);
        ErpPurchaseAdjustmentLineEntityMapper lineMapper = mock(ErpPurchaseAdjustmentLineEntityMapper.class);
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);
        ErpSupplierEntityMapper supplierMapper = mock(ErpSupplierEntityMapper.class);
        ErpOrgEntityMapper orgMapper = mock(ErpOrgEntityMapper.class);

        ErpMaterialEntity material1001 = material(1001L, "MAT-001", "物料A", "规格A", "10");
        ErpMaterialEntity material1002 = material(1002L, "MAT-001", "物料A-替代", "规格B", "20");
        when(materialMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(materialPage(1, 10, 2, List.of(material1001, material1002)));
        when(orgMapper.selectByIds(any())).thenReturn(List.of(org(10L, "使用组织一"), org(20L, "使用组织二")));

        ErpPurchaseAdjustmentLineEntity januaryPrice = line("20260101", "1001", "SUP-1",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30), "10.20");
        ErpPurchaseAdjustmentLineEntity mayPrice = line("20260501", "1001", "SUP-1",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 31), "11.50");
        ErpPurchaseAdjustmentLineEntity julyPrice = line("20260701", "1001", "SUP-1",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), "12.80");
        ErpPurchaseAdjustmentLineEntity secondMaterialPrice = line("20260501", "1002", "SUP-1",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 31), "21.00");
        ErpPurchaseAdjustmentLineEntity futureApprovedPrice = line("20270101", "1001", "SUP-1",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "99.00");
        when(lineMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(januaryPrice, mayPrice, julyPrice, secondMaterialPrice, futureApprovedPrice));

        when(adjustmentMapper.selectByIds(any())).thenReturn(List.of(
                head(20260101L, LocalDateTime.of(2026, 1, 1, 9, 0)),
                head(20260501L, LocalDateTime.of(2026, 5, 1, 9, 0)),
                head(20260701L, LocalDateTime.of(2026, 7, 1, 9, 0)),
                head(20270101L, LocalDateTime.of(2027, 1, 1, 9, 0))
        ));
        when(supplierMapper.selectByIds(any())).thenReturn(List.of(supplier("SUP-1", "SUP-001", "供应商一")));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "materialMapper", materialMapper);
        ReflectionTestUtils.setField(service, "entryMapper", lineMapper);
        ReflectionTestUtils.setField(service, "supplierMapper", supplierMapper);
        ReflectionTestUtils.setField(service, "orgMapper", orgMapper);
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
    void queryMaterialSupplierPriceUsesLaterApprovedAdjustmentFromItsApprovalMonth() {
        ErpMaterialEntityMapper materialMapper = mock(ErpMaterialEntityMapper.class);
        ErpPurchaseAdjustmentLineEntityMapper lineMapper = mock(ErpPurchaseAdjustmentLineEntityMapper.class);
        ErpPurchaseAdjustmentEntityMapper adjustmentMapper = mock(ErpPurchaseAdjustmentEntityMapper.class);
        ErpSupplierEntityMapper supplierMapper = mock(ErpSupplierEntityMapper.class);
        ErpOrgEntityMapper orgMapper = mock(ErpOrgEntityMapper.class);

        when(materialMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(materialPage(1, 10, 1, List.of(material(1128L, "05.02.006.01128", "测试物料", "规格", "10"))));
        when(orgMapper.selectByIds(any())).thenReturn(List.of(org(10L, "使用组织一")));
        when(lineMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                line("20220929", "1128", "SUP-1",
                        LocalDate.of(2022, 9, 29), LocalDate.of(2100, 1, 1), "0.4750440000"),
                line("20221119", "1128", "SUP-1",
                        LocalDate.of(2022, 9, 29), LocalDate.of(2100, 1, 1), "0.3192920000")
        ));
        when(adjustmentMapper.selectByIds(any())).thenReturn(List.of(
                head(20220929L, LocalDateTime.of(2022, 9, 29, 16, 2, 30)),
                head(20221119L, LocalDateTime.of(2022, 11, 19, 16, 37, 16))
        ));
        when(supplierMapper.selectByIds(any())).thenReturn(List.of(supplier("SUP-1", "SUP-001", "供应商一")));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "materialMapper", materialMapper);
        ReflectionTestUtils.setField(service, "entryMapper", lineMapper);
        ReflectionTestUtils.setField(service, "supplierMapper", supplierMapper);
        ReflectionTestUtils.setField(service, "orgMapper", orgMapper);
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
    void queryMaterialSupplierPriceReturnsMaterialInfoWhenPagedMaterialHasNoPrice() throws Exception {
        ErpMaterialEntityMapper materialMapper = mock(ErpMaterialEntityMapper.class);
        ErpPurchaseAdjustmentLineEntityMapper lineMapper = mock(ErpPurchaseAdjustmentLineEntityMapper.class);
        ErpOrgEntityMapper orgMapper = mock(ErpOrgEntityMapper.class);

        when(materialMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(materialPage(2, 10, 21, List.of(material(2001L, "MAT-NO-PRICE", "无价格物料", "规格X", "30"))));
        when(lineMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orgMapper.selectByIds(any())).thenReturn(List.of(org(30L, "无价格组织")));

        ErpPurchaseAdjustmentServiceImpl service = new ErpPurchaseAdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "materialMapper", materialMapper);
        ReflectionTestUtils.setField(service, "entryMapper", lineMapper);
        ReflectionTestUtils.setField(service, "orgMapper", orgMapper);

        MaterialSupplierPriceQuery query = new MaterialSupplierPriceQuery();
        query.setYear(2026);
        query.setPageNo(2);
        query.setPageSize(10);

        Page<MaterialSupplierPriceVo> result = service.queryMaterialSupplierPrice(query);

        assertEquals(21, result.getTotal());
        assertEquals(1, result.getRecords().size());
        MaterialSupplierPriceVo noPriceMaterial = result.getRecords().get(0);
        assertEquals("2001", readField(noPriceMaterial, "materialId"));
        assertEquals("MAT-NO-PRICE", readField(noPriceMaterial, "materialCode"));
        assertEquals("无价格物料", readField(noPriceMaterial, "materialName"));
        assertEquals("规格X", readField(noPriceMaterial, "specification"));
        assertEquals("无价格组织", readField(noPriceMaterial, "useOrgId"));
        assertEquals(0, noPriceMaterial.getRecordCount());
        assertEquals(BigDecimal.ZERO, noPriceMaterial.getAvgPrice());
        assertTrue(noPriceMaterial.getMonthlyPrices().values().stream().allMatch(BigDecimal.ZERO::equals));
    }

    private static ErpMaterialEntity material(Long materialId, String number, String name, String specification, String useOrgId) {
        ErpMaterialEntity entity = new ErpMaterialEntity();
        entity.setMaterialId(materialId);
        entity.setNumber(number);
        entity.setName(name);
        entity.setSpecification(specification);
        entity.setUseOrgId(useOrgId);
        return entity;
    }

    private static Page<ErpMaterialEntity> materialPage(long current, long size, long total, List<ErpMaterialEntity> records) {
        Page<ErpMaterialEntity> page = new Page<>(current, size, total);
        page.setRecords(records);
        return page;
    }

    private static ErpPurchaseAdjustmentLineEntity line(String pId, String materialId, String supplierId,
                                                        LocalDate effectiveDate, LocalDate expiryDate, String afterPrice) {
        ErpPurchaseAdjustmentLineEntity entity = new ErpPurchaseAdjustmentLineEntity();
        entity.setPId(pId);
        entity.setMaterialId(materialId);
        entity.setSupplierId(supplierId);
        entity.setEffectiveDate(effectiveDate);
        entity.setExpiryDate(expiryDate);
        entity.setAfterPrice(new BigDecimal(afterPrice));
        return entity;
    }

    private static ErpPurchaseAdjustmentEntity head(Long id, LocalDateTime approveDate) {
        ErpPurchaseAdjustmentEntity entity = new ErpPurchaseAdjustmentEntity();
        entity.setId(id);
        entity.setApproveDate(approveDate);
        return entity;
    }

    private static ErpSupplierEntity supplier(String supplierId, String number, String name) {
        ErpSupplierEntity entity = new ErpSupplierEntity();
        entity.setSupplierId(supplierId);
        entity.setNumber(number);
        entity.setName(name);
        return entity;
    }

    private static ErpOrgEntity org(Long orgId, String name) {
        ErpOrgEntity entity = new ErpOrgEntity();
        entity.setOrgId(orgId);
        entity.setName(name);
        return entity;
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
