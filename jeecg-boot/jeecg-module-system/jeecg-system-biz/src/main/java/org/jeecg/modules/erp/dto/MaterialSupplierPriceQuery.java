package org.jeecg.modules.erp.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物料供应商价格查询条件
 * @author jeecg-boot
 */
@Data
public class MaterialSupplierPriceQuery {

//    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    private String supplierId;

    private String useOrgId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Integer pageNo = 1;

    private Integer pageSize = 10;

}
