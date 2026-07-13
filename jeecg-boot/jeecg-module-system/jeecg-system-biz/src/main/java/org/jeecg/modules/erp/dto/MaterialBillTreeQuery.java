package org.jeecg.modules.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物料清单层级查询条件。
 */
@Data
public class MaterialBillTreeQuery {

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotNull(message = "使用组织不能为空")
    private Long useOrgId;
}
