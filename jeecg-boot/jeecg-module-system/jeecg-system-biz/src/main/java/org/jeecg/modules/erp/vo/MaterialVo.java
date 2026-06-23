package org.jeecg.modules.erp.vo;

import lombok.Data;

/**
 * 物料查询结果
 * @author jeecg-boot
 */
@Data
public class MaterialVo {

    /**
     * 物料ID
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 助记码
     */
    private String mnemonicCode;

    /**
     * 描述
     */
    private String description;
}