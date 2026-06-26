package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金蝶云·星空 生产订单 DTO
 *
 * @author generated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("erp_production_order")
public class ErpProductionOrderEntity extends ErpCommonEntity {

    // ==================== 标准字段 ====================

    /** 实体主键 */
    @TableId
    @JsonProperty("FID")
    private String fid;

    /** 单据编号 */
    @JsonProperty("FBillNo")
    private String billNo;

    /** 单据状态 */
    @JsonProperty("FDocumentStatus")
    private String documentStatus;

    /** 审核人 */
    @JsonProperty("FApproverId")
    private String approverId;

    /** 审核日期 */
    @JsonProperty("FApproveDate")
    private String approveDate;

    /** 修改人 */
    @JsonProperty("FModifierId")
    private String modifierId;

    /** 创建日期 */
    @JsonProperty("FCreateDate")
    private String createDate;

    /** 创建人 */
    @JsonProperty("FCreatorId")
    private String creatorId;

    /** 修改日期 */
    @JsonProperty("FModifyDate")
    private String modifyDate;

    /** 作废日期 */
    @JsonProperty("FCancelDate")
    private String cancelDate;

    /** 作废人 */
    @JsonProperty("FCanceler")
    private String canceler;

    /** 作废状态 */
    @JsonProperty("FCancelStatus")
    private String cancelStatus;

    /** 备注 */
    @JsonProperty("FDescription")
    private String description;

    /** 单据类型（必填项） */
    @JsonProperty("FBillType")
    private String billType;

    /** 受托 */
    @JsonProperty("FTrustteed")
    private String trustteed;

    /** 生产车间 */
    @JsonProperty("FWorkShopID0")
    private String workShopId0;

    /** 生产组织（必填项） */
    @JsonProperty("FPrdOrgId")
    private String prdOrgId;

    /** 计划员 */
    @JsonProperty("FPlannerID")
    private String plannerId;

    /** 单据日期（必填项） */
    @JsonProperty("FDate")
    private String date;

    /** 货主类型（必填项） */
    @JsonProperty("FOwnerTypeId")
    private String ownerTypeId;

    /** 货主 */
    @JsonProperty("FOwnerId")
    private String ownerId;

    /** 计划组 */
    @JsonProperty("FWorkGroupId")
    private String workGroupId;

    /** 销售业务类型 */
    @JsonProperty("FBusinessType")
    private String businessType;

    /** 是否返工 */
    @JsonProperty("FIsRework")
    private String isRework;

    /** 组织受托加工 */
    @JsonProperty("FIsEntrust")
    private String isEntrust;

    /** 委托组织 */
    @JsonProperty("FEnTrustOrgId")
    private String enTrustOrgId;

    /** 用料清单展开（必填项） */
    @JsonProperty("FPPBOMType")
    private String ppbomType;

    /** 生产发料 */
    @JsonProperty("FIssueMtrl")
    private String issueMtrl;

    /** 期初生产订单 */
    @JsonProperty("FIsQCMO")
    private String isQcmo;

    /** 序列号上传 */
//    @JsonProperty("FScanBox")
//    private String scanBox;

    // ==================== 自定义字段 ====================

    /** 打印次数 */
    @JsonProperty("F_ZGHY_PrintTimes")
    private String zghyPrintTimes;

    /** 最后入库日期 */
    @JsonProperty("F_UKPT_ZHRKRQ")
    private String ukptZhrkrq;

    /** 最早领料日期 */
    @JsonProperty("F_UKPT_ZZLLRQ")
    private String ukptZzllrq;

    /** 创建方式 */
    @JsonProperty("F_UKPT_CJFS")
    private String ukptCjfs;

    /** 独立项目 */
    @JsonProperty("F_UKPT_DLXM")
    private String ukptDlxm;

    /** 已领料天数 */
    @JsonProperty("F_XGBW_LLTS")
    private String xgbwLlts;
}
