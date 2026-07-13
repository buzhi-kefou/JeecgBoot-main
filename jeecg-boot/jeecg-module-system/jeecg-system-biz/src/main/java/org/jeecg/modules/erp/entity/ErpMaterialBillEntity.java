package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("erp_material_bill")
public class ErpMaterialBillEntity extends ErpCommonEntity {


    /**
     * 实体主键
     */
    @JsonProperty("FID")
    @TableId
    private Long id;

    /**
     * 数据状态
     */
    @JsonProperty("FDocumentStatus")
    private String documentStatus;

    /**
     * 禁用状态
     */
    @JsonProperty("FForbidStatus")
    private String forbidStatus;

    /**
     * BOM简称
     */
    @JsonProperty("FName")
    private String name;

    /**
     * BOM版本
     */
    @JsonProperty("FNumber")
    private String number;

    /**
     * 描述
     */
    @JsonProperty("FDescription")
    private String description;

    /**
     * 创建组织（必填项）
     */
    @JsonProperty("FCreateOrgId")
    private Long createOrgId;

    /**
     * 使用组织（必填项）
     */
    @JsonProperty("FUseOrgId")
    private Long useOrgId;

    /**
     * 创建人
     */
    @JsonProperty("FCreatorId")
    private Long creatorId;

    /**
     * 修改人
     */
    @JsonProperty("FModifierId")
    private Long modifierId;

    /**
     * 创建日期
     */
    @JsonProperty("FCreateDate")
    private LocalDateTime createDate;

    /**
     * 修改日期
     */
    @JsonProperty("FModifyDate")
    private LocalDateTime modifyDate;

    /**
     * 禁用人
     */
    @JsonProperty("FForbidderId")
    private Long forbidderId;

    /**
     * 审核人
     */
    @JsonProperty("FApproverId")
    private Long approverId;

    /**
     * 禁用日期
     */
    @JsonProperty("FForbidDate")
    private LocalDateTime forbidDate;

    /**
     * 审核日期
     */
    @JsonProperty("FApproveDate")
    private LocalDateTime approveDate;

    /**
     * BOM分类（必填项）
     */
    @JsonProperty("FBOMCATEGORY")
    private String bomCategory;

    /**
     * BOM用途（必填项）
     */
    @JsonProperty("FBOMUSE")
    private String bomUse;

    /**
     * 父项物料编码（必填项）
     */
    @JsonProperty("FMATERIALID")
    private String materialId;

    @JsonProperty("FMATERIALID.fnumber")
    private String materialCode;

    /**
     * 物料名称
     */
    @JsonProperty("FITEMNAME")
    private String itemName;

    /**
     * 规格型号
     */
    @JsonProperty("FITEMMODEL")
    private String itemModel;

    /**
     * 物料属性
     */
    @JsonProperty("FITEMPPROPERTY")
    private String itemProperty;

    /**
     * 成品率%
     */
    @JsonProperty("FYIELDRATE")
    private BigDecimal yieldRate;

    /**
     * 单据类型（必填项）
     */
    @JsonProperty("FBILLTYPE")
    private String billType;

    /**
     * 父项物料单位（必填项）
     */
    @JsonProperty("FUNITID")
    private Long unitId;

    /**
     * 父项基本单位
     */
    @JsonProperty("FBaseUnitId")
    private Long baseUnitId;

    /**
     * 是否新版本
     */
    @JsonProperty("FISDEFAULT")
    private Boolean isDefault;

    /**
     * 配置BOM
     */
    @JsonProperty("FCfgBomId")
    private Long cfgBomId;

    /**
     * 批量
     */
    @JsonProperty("FQty")
    private BigDecimal qty;

    /**
     * 基本单位批量
     */
    @JsonProperty("FBaseQty")
    private BigDecimal baseQty;

    /**
     * BOM分组
     */
    @JsonProperty("FGroup")
    private String bomGroup;

    /**
     * PLMBOM内码
     */
    @JsonProperty("FPLMBOMId")
    private Long plmBomId;

    /**
     * BOM来源
     */
    @JsonProperty("FBOMSRC")
    private String bomSrc;

    /**
     * 校验
     */
//    @JsonProperty("FIsValidate")
//    private Boolean isValidate;

    /**
     * 辅助属性
     */
    @JsonProperty("FParentAuxPropId")
    private Long parentAuxPropId;

    /**
     * 备注
     */
//    @JsonProperty("FF100005")
//    private String remark;

    /**
     * 技转前规格
     */
//    @JsonProperty("FF100006")
//    private String preTransferSpec;

    /**
     * 技转前名称
     */
//    @JsonProperty("FF100007")
//    private String preTransferName;

    /**
     * 流水号
     */
//    @JsonProperty("FF100004")
//    private String serialNumber;

    /**
     * 物料颜色
     */
//    @JsonProperty("FF100001")
//    private String materialColor;

    /**
     * 物料组件
     */
//    @JsonProperty("FF100002")
//    private String materialComponent;

    /**
     * 销售订单号辅助属性
     */
//    @JsonProperty("FF100008")
//    private String saleOrderAuxProp;

    /**
     * 产品模型
     */
    @JsonProperty("FMDLID")
    private Long mdlId;

    /**
     * 扩展版本变量
     */
//    @JsonProperty("FExtVar")
//    private String extVar;

    /**
     * 禁用原因
     */
    @JsonProperty("FForbidReson")
    private String forbidReason;

    /**
     * 是否变更中
     */
    @JsonProperty("FIsChange")
    private Boolean isChange;

    /**
     * 生产车间
     */
    @JsonProperty("F_ZGHY_sccj")
    private String productionWorkshop;

    /**
     * 父项PLM图号
     */
    @JsonProperty("F_UKPT_FPLMTH")
    private String parentPlmDrawingNo;

    @TableField(exist = false)
    private List<ErpMaterialBillLineEntity> entries;
}
