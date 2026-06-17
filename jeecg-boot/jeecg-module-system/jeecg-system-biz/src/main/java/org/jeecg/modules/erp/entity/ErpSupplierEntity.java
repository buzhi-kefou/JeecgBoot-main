package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_supplier")
public class ErpSupplierEntity extends ErpCommonEntity {

    /**
     * 实体主键
     */
    @JsonProperty("FSupplierId")
    @TableId
    private Long supplierId;

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
     * 名称（必填项）
     */
    @JsonProperty("FName")
    private String name;

    /**
     * 编码
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
     * 简称
     */
    @JsonProperty("FShortName")
    private String shortName;

    /**
     * 禁用人
     */
    @JsonProperty("FForbiderId")
    private Long forbiderId;

    /**
     * 禁用日期
     */
    @JsonProperty("FForbidDate")
    private LocalDateTime forbidDate;

    /**
     * 审核日期
     */
    @JsonProperty("FAuditDate")
    private LocalDateTime auditDate;

    /**
     * 审核人
     */
    @JsonProperty("FAuditorId")
    private Long auditorId;

    /**
     * 供应商分组（必填项）
     */
    @JsonProperty("FGroup")
    private Long groupId;

    /**
     * 对应组织
     */
    @JsonProperty("FCorrespondOrgId")
    private Long correspondOrgId;

    /**
     * 同步货主状态(GY)
     */
    @JsonProperty("FSYNCGYOWNERSTATUS")
    private String syncGyOwnerStatus;

    /**
     * 注册编码
     */
    @JsonProperty("FRegNumber")
    private String regNumber;

    /**
     * 对应集团供应商
     */
    @JsonProperty("FGROUPSUPPLYID")
    private Long groupSupplyId;

    /**
     * 集团供应商
     */
    @JsonProperty("FISGROUP")
    private Boolean isGroup;

    /**
     * 一审人
     */
    @JsonProperty("F_ZGHY_UserId")
    private Long userId;

    /**
     * 对账时间
     */
    @JsonProperty("F_ZGHY_Text")
    private String text;
}
