package org.jeecg.modules.erp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物料清单层级查询响应。
 */
@Data
public class MaterialBillChildLineVo {

    private Long id;

    private Long billId;

    private Integer levelNo;

    private Long parentBillId;

    private String parentMaterialCode;

    private String itemProperty;

    private String materialCodeChild;

    private String materialNameChild;

    private String materialModelChild;

    private BigDecimal numerator;

    private BigDecimal denominator;

    private Long bomId;

    private String childBomVersion;

    private List<MaterialBillChildLineVo> children;
}
