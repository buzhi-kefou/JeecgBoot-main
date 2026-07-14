package org.jeecg.modules.erp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 最底层外购件的累计用量。
 */
@Data
public class MaterialBillPurchaseUsageVo {

    private Long id;

    private String itemProperty;

    private String materialCodeChild;

    private String materialNameChild;

    private String materialModelChild;

    private BigDecimal numerator;

    private BigDecimal denominator;

    /**
     * 累计用量，等于累计分子除以累计分母。
     */
    private BigDecimal usageAmount;
}
