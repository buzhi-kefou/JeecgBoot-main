package org.jeecg.modules.erp.dto;

import lombok.Data;
import org.jeecg.modules.erp.entity.ErpCommonEntity;

@Data
public class QueryDetailDto {

    private String formId;

    private ErpCommonEntity fieldKeys;

    private String filterString;

    private String orderString;

    private Integer topRowCount = 0;

    private Integer startRow = 0;

    private Integer limit = 2000;

    private String subSystemId = "";


    public String getFieldKeys() {
        if (this.fieldKeys == null) {
            return null;
        }
        return ErpCommonEntity.getJsonPropertyNames(this.fieldKeys.getClass());
    }


}