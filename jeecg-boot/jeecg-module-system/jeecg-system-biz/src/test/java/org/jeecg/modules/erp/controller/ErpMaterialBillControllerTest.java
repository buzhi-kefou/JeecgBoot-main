package org.jeecg.modules.erp.controller;

import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.vo.MaterialBillChildLineVo;
import org.jeecg.modules.erp.vo.MaterialBillPurchaseUsageVo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpMaterialBillControllerTest {

    @Test
    void toChildLineVoConvertsMaterialAttributeCodeToLabel() {
        MaterialBillChildLineDto source = new MaterialBillChildLineDto();
        source.setItemProperty("2");
        source.setChildBomVersion("V1.0");

        MaterialBillChildLineVo result = ReflectionTestUtils.invokeMethod(
                ErpMaterialBillController.class, "toChildLineVo", source);

        assertEquals("自制", result.getItemProperty());
        assertEquals("V1.0", result.getChildBomVersion());
    }

    @Test
    void toPurchaseUsagesConvertsMaterialAttributeCodeToLabel() {
        MaterialBillChildLineDto source = new MaterialBillChildLineDto();
        source.setItemProperty("1");

        @SuppressWarnings("unchecked")
        List<MaterialBillPurchaseUsageVo> result = ReflectionTestUtils.invokeMethod(
                ErpMaterialBillController.class, "toPurchaseUsages", List.of(source));

        assertEquals("外购", result.get(0).getItemProperty());
    }

    @Test
    void toPurchaseUsagesMergesSameMaterialCodeByAddingUsageFractions() {
        MaterialBillChildLineDto first = purchaseLine("M001", "1", "2", "3");
        MaterialBillChildLineDto second = purchaseLine("M001", "3", "5", "7");

        @SuppressWarnings("unchecked")
        List<MaterialBillPurchaseUsageVo> result = ReflectionTestUtils.invokeMethod(
                ErpMaterialBillController.class, "toPurchaseUsages", List.of(first, second));

        assertEquals(1, result.size());
        assertEquals("M001", result.get(0).getMaterialCodeChild());
        assertEquals(new BigDecimal("29"), result.get(0).getNumerator());
        assertEquals(new BigDecimal("21"), result.get(0).getDenominator());
        assertEquals(new BigDecimal("1.380952"), result.get(0).getUsageAmount());
    }

    private static MaterialBillChildLineDto purchaseLine(String materialCode, String itemProperty,
                                                         String numerator, String denominator) {
        MaterialBillChildLineDto line = new MaterialBillChildLineDto();
        line.setMaterialCodeChild(materialCode);
        line.setItemProperty(itemProperty);
        line.setNumerator(new BigDecimal(numerator));
        line.setDenominator(new BigDecimal(denominator));
        return line;
    }
}
