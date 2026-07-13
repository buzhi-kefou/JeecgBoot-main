package org.jeecg.modules.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * 物料清单层级及外购件用量查询响应。
 */
@Data
public class MaterialBillChildLineResultVo {

    private List<MaterialBillChildLineVo> childLines;

    private List<MaterialBillPurchaseUsageVo> purchaseUsages;
}
