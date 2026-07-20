package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("erp_sales_order_finance")
public class ErpSalesOrderFinanceEntity extends ErpCommonEntity {

    /** 实体主键 */
    @JsonProperty("FSaleOrderFinance_fEntryId")
    @TableId
    private Long entryId;

    /**
     * 销售单id
     */
    private String pid;

    /** 本位币 */
    @JsonProperty("fLocalCurrId.fname")
    private String localCurr;

    /** 汇率类型 */
    @JsonProperty("fExchangeTypeId.fname")
    private String exchangeType;

    /** 汇率 */
    @JsonProperty("fExchangeRate")
    private BigDecimal exchangeRate;

    /** 预收比例% */
    @JsonProperty("fPayAdvanceRate")
    private BigDecimal payAdvanceRate;

    /** 预收金额 */
    @JsonProperty("fPayAdvanceAmount")
    private BigDecimal payAdvanceAmount;

    /** 折扣表 */
    @JsonProperty("fdiscountListId.fname")
    private String discountList;

    /** 结算币别（必填项） */
    @JsonProperty("fSettleCurrId.fname")
    private String settleCurr;

    /** 税额（本位币） */
    @JsonProperty("fBillTaxAmount_LC")
    private BigDecimal billTaxAmountLc;

    /** 金额（本位币） */
    @JsonProperty("fBillAmount_LC")
    private BigDecimal billAmountLc;

    /** 价税合计 */
    @JsonProperty("fBillAllAmount")
    private BigDecimal billAllAmount;

    /** 价税合计（本位币） */
    @JsonProperty("fBillAllAmount_LC")
    private BigDecimal billAllAmountLc;

    /** 价目表 */
    @JsonProperty("fPriceListId.fname")
    private String priceList;

    /** 税额 */
    @JsonProperty("fBillTaxAmount")
    private BigDecimal billTaxAmount;

    /** 金额 */
    @JsonProperty("fBillAmount")
    private BigDecimal billAmount;

    /** 是否含税 */
    @JsonProperty("fIsIncludedTax")
    private Boolean isIncludedTax;

    /** 需要预收 */
    @JsonProperty("fNeedPayAdvance")
    private Boolean needPayAdvance;

    /** 收款单号 */
    @JsonProperty("fRecBillId.fnumber")
    private String recBillId;

    /** 收款条件（必填项） */
    @JsonProperty("fRecConditionId.fname")
    private String recCondition;

    /** 结算方式 */
    @JsonProperty("fSettleModeId.fname")
    private String settleMode;

    /** 关联应收金额（出库） */
    @JsonProperty("fJoinStockAmount")
    private BigDecimal joinStockAmount;

    /** 关联应收金额（订单） */
    @JsonProperty("fJoinOrderAmount")
    private BigDecimal joinOrderAmount;

    /** 工作流信用检查状态 */
    @JsonProperty("fCreChkStatus")
    private String creChkStatus;

    /** 工作流信用超标天数 */
    @JsonProperty("fCreChkDays")
    private Integer creChkDays;

    /** 工作流信用超标金额 */
    @JsonProperty("fCreChkAmount")
    private BigDecimal creChkAmount;

    /** 审批流信用压批月结检查 */
    @JsonProperty("fCrePreBatAndMonStatus")
    private String crePreBatAndMonStatus;

    /** 信用压批超标 */
    @JsonProperty("fCrePreBatchOver")
    private String crePreBatchOver;

    /** 信用月结超标 */
    @JsonProperty("fCreMonControlOver")
    private String creMonControlOver;

    /** 价外税 */
    @JsonProperty("fIsPriceExcludeTax")
    private Boolean isPriceExcludeTax;

    /** 保证金比例（%） */
    @JsonProperty("fMarginLevel")
    private BigDecimal marginLevel;

    /** 关联保证金 */
    @JsonProperty("fAssociateMargin")
    private BigDecimal associateMargin;

    /** 保证金 */
    @JsonProperty("fMargin")
    private BigDecimal margin;

    /** 关联退款保证金 */
    @JsonProperty("fAssRefundMargin")
    private BigDecimal assRefundMargin;

    /** 工作流信用逾期超标额度 */
    @JsonProperty("fCreChkOverAmount")
    private BigDecimal creChkOverAmount;

    /** 寄售生成跨组织调拨 */
    @JsonProperty("fOverOrgTransDirect")
    private String overOrgTransDirect;

    /** 收款通知单号 */
    @JsonProperty("fRecNoticeNo")
    private String recNoticeNo;

    /** 收款二维码链接 */
    @JsonProperty("fRecBarcodeLink")
    private String recBarcodeLink;

    /** 整单折扣额 */
    @JsonProperty("fAllDisCount")
    private BigDecimal allDisCount;

    /** 变更单主键 */
    @JsonProperty("fXPKID_F")
    private String xpkidF;
}
