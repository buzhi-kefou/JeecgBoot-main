package org.jeecg.modules.erp.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 物料查询条件
 * @author jeecg-boot
 */
@Data
public class MaterialQuery {

    /**
     * 物料编码或物料名称
     */
    @NotBlank(message = "查询关键词不能为空")
    private String keyword;
}