package org.jeecg.modules.erp.mapper;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpMaterialBillEntityMapperTest {

    @Test
    void selectAllChildLinesDeclaresMaterialAndOrganizationParameters() throws NoSuchMethodException {
        Method method = ErpMaterialBillEntityMapper.class.getMethod(
                "selectAllChildLines", String.class, Long.class);

        assertEquals(List.class, method.getReturnType());
        assertEquals(MaterialBillChildLineDto.class,
                ((java.lang.reflect.ParameterizedType) method.getGenericReturnType())
                        .getActualTypeArguments()[0]);
        assertEquals("materialCode", method.getParameters()[0].getAnnotation(Param.class).value());
        assertEquals("useOrgId", method.getParameters()[1].getAnnotation(Param.class).value());
    }
}
