package org.jeecg.modules.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MaterialSupplierPriceLineRow {

    private String materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String useOrgId;

    private String supplierId;

    private String supplierCode;

    private String supplierName;

    private String supplierShortName;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private BigDecimal afterTaxPrice;

    private LocalDateTime approveDate;
}
