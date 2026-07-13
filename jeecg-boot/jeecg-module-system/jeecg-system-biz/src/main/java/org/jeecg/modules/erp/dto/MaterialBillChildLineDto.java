package org.jeecg.modules.erp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.erp.entity.ErpMaterialBillLineEntity;

import java.util.List;

/**
 * 物料清单下级明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialBillChildLineDto extends ErpMaterialBillLineEntity {

    /** 层级，从 1 开始。 */
    private Integer levelNo;

    /** 当前明细所属的 BOM 头表主键。 */
    private Long parentBillId;

    /** 当前明细所属 BOM 的父项物料编码。 */
    private String parentMaterialCode;

    /** 当前明细所属 BOM 头表的物料属性。 */
    private String itemProperty;

    /** 下级 BOM 明细。 */
    private List<MaterialBillChildLineDto> children;
}
