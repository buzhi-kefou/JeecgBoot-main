package org.jeecg.modules.erp.controller;

import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.vo.MaterialBillChildLineVo;
import org.jeecg.modules.erp.vo.MaterialBillPurchaseUsageVo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpMaterialBillControllerTest {

    @Test
    void toChildLineVoConvertsMaterialAttributeCodeToLabel() {
        MaterialBillChildLineDto source = new MaterialBillChildLineDto();
        source.setItemProperty("2");

        MaterialBillChildLineVo result = ReflectionTestUtils.invokeMethod(
                ErpMaterialBillController.class, "toChildLineVo", source);

        assertEquals("自制", result.getItemProperty());
    }

    @Test
    void toPurchaseUsagesConvertsMaterialAttributeCodeToLabel() {
        MaterialBillChildLineDto source = new MaterialBillChildLineDto();
        source.setItemProperty("2");

        @SuppressWarnings("unchecked")
        List<MaterialBillPurchaseUsageVo> result = ReflectionTestUtils.invokeMethod(
                ErpMaterialBillController.class, "toPurchaseUsages", List.of(source));

        assertEquals("自制", result.get(0).getItemProperty());
    }
}
