package org.jeecg.modules.erp.vo;



import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 物料供应商价格查询结果
 * @author jeecg-boot
 */
@Data
public class MaterialSupplierPriceVo {

    private String materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String useOrgId;

    private String supplierId;

    private String supplierCode;

    private String supplierName;

    private String supplierShortName;

    private Map<Integer, BigDecimal> monthlyPrices;

    private BigDecimal avgPrice;

    private BigDecimal changeRate;

    private Integer recordCount;

}
