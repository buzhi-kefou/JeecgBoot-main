package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("erp_purchase_adjustment")
public class ErpPurchaseAdjustmentEntity extends ErpCommonEntity {

    /**
     * 实体主键
     */
    @JsonProperty("FID")
    @TableId
    private Long id;

    /**
     * 单据编号
     */
    @JsonProperty("FBillNo")
    private String billNo;

    /**
     * 状态
     */
    @JsonProperty("FDocumentStatus")
    private String documentStatus;

    /**
     * 日期（必填项）
     */
    @JsonProperty("FDate")
    private LocalDate date;

    /**
     * 采购组织（必填项）
     */
    @JsonProperty("FPurchaseOrgId")
    private String purchaseOrgId;

    /**
     * 名称（必填项）
     */
    @JsonProperty("FName")
    private String name;

    /**
     * 描述
     */
    @JsonProperty("FDescription")
    private String description;

    /**
     * 调价原因（必填项）
     */
    @JsonProperty("FPaReason")
    private String paReason;

    /**
     * 使用组织
     */
    @JsonProperty("FUseOrgId")
    private String useOrgId;

    /**
     * 禁用
     */
    @JsonProperty("FForbidStatus")
    private String forbidStatus;

    /**
     * 创建人
     */
    @JsonProperty("FCreatorId")
    private String creatorId;

    /**
     * 创建日期
     */
    @JsonProperty("FCreateDate")
    private LocalDateTime createDate;

    /**
     * 最后修改人
     */
    @JsonProperty("FModifierId")
    private String modifierId;

    /**
     * 最后修改日期
     */
    @JsonProperty("FModifyDate")
    private LocalDateTime modifyDate;

    /**
     * 审核人
     */
    @JsonProperty("FApproverId")
    private String approverId;

    /**
     * 审核日期
     */
    @JsonProperty("FApproveDate")
    private LocalDateTime approveDate;

    /**
     * 禁用人
     */
    @JsonProperty("FForbiderId")
    private String forbiderId;

    /**
     * 禁用日期
     */
    @JsonProperty("FForbidDate")
    private LocalDateTime forbidDate;

    /**
     * 生效状态
     */
    @JsonProperty("FEffectiveStatus")
    private String effectiveStatus;

    /**
     * 生效人
     */
    @JsonProperty("FEffectiveUserId")
    private String effectiveUserId;

    /**
     * 生效日期
     */
    @JsonProperty("FEffectiveDateHead")
    private LocalDateTime effectiveDateHead;

    /**
     * 价格类型（必填项）
     */
    @JsonProperty("F_ZGHY_Combo1")
    private String combo1;

    /**
     * 一审人
     */
    @JsonProperty("F_ZGHY_UserId")
    private String userId;

    /**
     * 二审人
     */
    @JsonProperty("F_ZGHY_UserId1")
    private String userId1;

    /**
     * 一审日期
     */
    @JsonProperty("F_ZGHY_Date")
    private LocalDateTime date1;

    /**
     * 二审日期
     */
    @JsonProperty("F_ZGHY_Date1")
    private LocalDateTime date2;

    /**
     * 审批意见
     */
    @JsonProperty("F_ZGHY_Text")
    private String text;

    /**
     * 附件数
     */
    @JsonProperty("F_UKPT_AttachmentCount")
    private Integer attachmentCount;

    @TableField(exist = false)
    private List<ErpPurchaseAdjustmentLineEntity> entries;
}
