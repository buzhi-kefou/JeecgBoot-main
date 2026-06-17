package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("erp_purchase_adjustment_line")
public class ErpPurchaseAdjustmentLineEntity extends ErpCommonEntity {


    @TableId
    private String id;

    @JsonProperty("FID")
    private String pId;

    /**
     * 实体主键
     */
    @JsonProperty("fpur_patentry_fseq")
    private Long entryId;

    /**
     * 物料编码
     */
    @JsonProperty("FMaterialId")
    private String materialId;

    /**
     * 物料名称
     */
    @JsonProperty("FMaterialName")
    private String materialName;

    /**
     * 规格型号
     */
    @JsonProperty("FUOM_01")
    private String uom01;

    /**
     * 至
     */
    @JsonProperty("FToQty")
    private BigDecimal toQty;

    /**
     * 调前单价
     */
    @JsonProperty("FBeforePrice")
    private BigDecimal beforePrice;

    /**
     * 调后单价
     */
    @JsonProperty("FAfterPrice")
    private BigDecimal afterPrice;

    /**
     * 调价幅度%
     */
    @JsonProperty("FAdjustRange")
    private BigDecimal adjustRange;

    /**
     * 价格上限
     */
    @JsonProperty("FUpPrice")
    private BigDecimal upPrice;

    /**
     * 价格下限
     */
    @JsonProperty("FDownPrice")
    private BigDecimal downPrice;

    /**
     * 生效日期（必填项）
     */
    @JsonProperty("FEffectiveDate")
    private LocalDate effectiveDate;

    /**
     * 失效日期（必填项）
     */
    @JsonProperty("FExpiryDate")
    private LocalDate expiryDate;

    /**
     * 价目表（必填项）
     */
    @JsonProperty("FPriceListId")
    private String priceListId;

    /**
     * 供应商
     */
    @JsonProperty("FSupplierId")
    private String supplierId;

    /**
     * 币别
     */
    @JsonProperty("FCurrencyId")
    private String currencyId;

    /**
     * 辅助属性
     */
    @JsonProperty("FAuxpropId")
    private String auxpropId;

    /**
     * 备注
     */
    @JsonProperty("fnote")
    private String f100005;

    /** 技转前规格 */
    // @JsonProperty("FF100006")
    // private String f100006;

    /** 技转前名称 */
    // @JsonProperty("FF100007")
    // private String f100007;

    /** 流水号 */
    // @JsonProperty("FF100004")
    // private String f100004;

    /** 物料颜色 */
    // @JsonProperty("FF100001")
    // private String f100001;

    /** 物料组件 */
    // @JsonProperty("FF100002")
    // private String f100002;

    /** 销售订单号辅助属性 */
    // @JsonProperty("FF100008")
    // private String f100008;

    /**
     * 物料类别
     */
    @JsonProperty("FMATERIALTYPEID")
    private String materialTypeId;

    /**
     * 价目表下推
     */
    @JsonProperty("FIsPriceListPush")
    private Boolean isPriceListPush;

    /**
     * 计价单位（必填项）
     */
    @JsonProperty("FUnitID")
    private String unitId;

    /**
     * 调价唯一性标示
     */
    @JsonProperty("FPATIdentity")
    private String patIdentity;

    /**
     * 调价对象
     */
    @JsonProperty("FPriceListObject")
    private String priceListObject;

    /**
     * 调价类型
     */
    @JsonProperty("FAdjustType")
    private String adjustType;

    /**
     * 调前税率
     */
    @JsonProperty("FBeforeTaxRate")
    private BigDecimal beforeTaxRate;

    /**
     * 调后税率
     */
    @JsonProperty("FAfterTaxRate")
    private BigDecimal afterTaxRate;

    /**
     * 调前含税单价
     */
    @JsonProperty("FBeforeTaxPrice")
    private BigDecimal beforeTaxPrice;

    /**
     * 调后含税单价
     */
    @JsonProperty("FAfterTaxPrice")
    private BigDecimal afterTaxPrice;

    /**
     * 含税
     */
    @JsonProperty("FIsIncludedTax")
    private Boolean isIncludedTax;

    /**
     * 需求组织
     */
    @JsonProperty("FProcessOrgId")
    private String processOrgId;

    /**
     * 作业
     */
    @JsonProperty("FPROCESSID")
    private String processId;

    /**
     * 价目表分录内码
     */
    @JsonProperty("FSrcEntryID")
    private Long srcEntryId;

    /**
     * 备注
     */
    @JsonProperty("FNote")
    private String note;

    /**
     * 调前价格系数
     */
    @JsonProperty("FBeforePriceCoefficient")
    private BigDecimal beforePriceCoefficient;

    /**
     * 调后价格系数
     */
    @JsonProperty("FAfterPriceCoefficient")
    private BigDecimal afterPriceCoefficient;

    /**
     * 从
     */
    @JsonProperty("FFROMQTY")
    private BigDecimal fromQty;

    /**
     * 自定义基础资料1
     */
    @JsonProperty("FDefBaseDataO")
    private String defBaseDataO;

    /**
     * 自定义基础资料2
     */
    @JsonProperty("FDefBaseDataT")
    private String defBaseDataT;

    /**
     * 价格类型（必填项）
     */
    @JsonProperty("FDefAssistantO")
    private String defAssistantO;

    /**
     * 自定义辅助资料2
     */
    @JsonProperty("FDefAssistantT")
    private String defAssistantT;

    /**
     * 班产
     */
    @JsonProperty("FDefTextO")
    private String defTextO;

    /**
     * 模数
     */
    @JsonProperty("FDefTextT")
    private String defTextT;

    /**
     * 自定义价格1
     */
    @JsonProperty("FDefaultPriceO")
    private BigDecimal defaultPriceO;

    /**
     * 自定义价格2
     */
    @JsonProperty("FDefaultPriceT")
    private BigDecimal defaultPriceT;

    /**
     * 价外税
     */
    @JsonProperty("FIsPriceExcludeTax")
    private Boolean isPriceExcludeTax;

    /**
     * 物料分组编码
     */
    @JsonProperty("FMaterialGroupId")
    private String materialGroupId;

    /**
     * 物料分组名称
     */
    @JsonProperty("FMATERIALGROUPNAME")
    private String materialGroupName;

    /**
     * 类型
     */
    @JsonProperty("F_ZGHY_Combo")
    private String combo;

    /**
     * 单重g
     */
    @JsonProperty("F_ZGHY_Decimal")
    private BigDecimal decimalValue;

    /**
     * 单重含税单价
     */
    @JsonProperty("F_ZGHY_Price")
    private BigDecimal price;

    /**
     * 调前价格类型(N)
     */
    @JsonProperty("F_UKPT_DQJL")
    private String dqjl;

    /**
     * 调前价格类型
     */
    @JsonProperty("F_UKPT_DQLX")
    private String dqlx;

    /**
     * PLM旧物料
     */
    @JsonProperty("F_UKPT_PLMJWL")
    private String plmjwl;

    /**
     * 老规格型号
     */
    @JsonProperty("F_UKPT_LGGXH")
    private String lggxh;

    /**
     * 老物料名称
     */
    @JsonProperty("F_UKPT_LWLMC")
    private String lwmc;
}
