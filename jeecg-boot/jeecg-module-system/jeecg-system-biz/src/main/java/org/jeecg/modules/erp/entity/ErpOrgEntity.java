package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_org")
public class ErpOrgEntity extends ErpCommonEntity {

    /**
     * 实体主键
     */
    @TableId
    @JsonProperty("FOrgID")
    private Long orgId;

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
     * 编码（必填项）
     */
    @JsonProperty("FNumber")
    private String number;

    /**
     * 修改人
     */
    @JsonProperty("FModifierId")
    private String modifierId;

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
     * 修改日期
     */
    @JsonProperty("FModifyDate")
    private LocalDateTime modifyDate;

    /**
     * 描述
     */
    @JsonProperty("FDescription")
    private String description;

    /**
     * 联系人
     */
    @JsonProperty("FContact")
    private String contact;

    /**
     * 形态（必填项）
     */
    @JsonProperty("FOrgFormID")
    private String orgFormId;

    /**
     * 地址
     */
    @JsonProperty("FADDRESS")
    private String address;

    /**
     * 联系电话
     */
    @JsonProperty("FTel")
    private String tel;

    /**
     * 核算组织类型
     */
    @JsonProperty("FAcctOrgType")
    private String acctOrgType;

    /**
     * 利润中心
     */
//    @JsonProperty("FIsProfileCenter")
//    private Boolean isProfileCenter;

    /**
     * 法人
     */
//    @JsonProperty("FIsCorp")
//    private Boolean isCorp;

    /**
     * 所属法人
     */
    @JsonProperty("FParentID")
    private String parentId;

    /**
     * 禁用人
     */
    @JsonProperty("FFORBIDORID")
    private String forbidderId;

    /**
     * 禁用日期
     */
    @JsonProperty("FForbidDate")
    private LocalDateTime forbidDate;

    /**
     * 业务组织
     */
    @JsonProperty("FIsBusinessOrg")
    private Boolean isBusinessOrg;

    /**
     * 核算组织
     */
    @JsonProperty("FIsAccountOrg")
    private Boolean isAccountOrg;

    /**
     * 审核日期
     */
    @JsonProperty("FAUDITDATE")
    private LocalDateTime auditDate;

    /**
     * 审核人
     */
    @JsonProperty("FAUDITORID")
    private String auditorId;

    /**
     * 邮编
     */
    @JsonProperty("FPostCode")
    private String postCode;

    /**
     * 销售职能
     */
//    @JsonProperty("FSaleBox")
//    private Boolean saleBox;

    /**
     * 采购职能
     */
//    @JsonProperty("FPurchaseBox")
//    private Boolean purchaseBox;

    /**
     * 库存职能
     */
//    @JsonProperty("FStockBox")
//    private Boolean stockBox;

    /**
     * 工厂职能
     */
//    @JsonProperty("FFactoryBox")
//    private Boolean factoryBox;

    /**
     * HR职能
     */
//    @JsonProperty("FHRBox")
//    private Boolean hrBox;

    /**
     * 结算职能
     */
//    @JsonProperty("FClearingBox")
//    private Boolean clearingBox;

    /**
     * 资产职能
     */
//    @JsonProperty("FAssetBox")
//    private Boolean assetBox;

    /**
     * 收付职能
     */
//    @JsonProperty("FReceiptAndPayBox")
//    private Boolean receiptAndPayBox;

    /**
     * 质检职能
     */
//    @JsonProperty("FQualityBox")
//    private Boolean qualityBox;

    /**
     * 资金职能
     */
//    @JsonProperty("FCapitalBox")
//    private Boolean capitalBox;

    /**
     * 营销职能
     */
//    @JsonProperty("FMarketing")
//    private Boolean marketing;

    /**
     * 组织职能
     */
    @JsonProperty("FOrgFunctions")
    private String orgFunctions;

    /**
     * 服务职能
     */
//    @JsonProperty("FService")
//    private Boolean service;

    /**
     * 共享中心
     */
//    @JsonProperty("FShareCenter")
//    private Boolean shareCenter;

    /**
     * 同步管易货主
     */
    @JsonProperty("FIsSynCERPOwer")
    private Boolean isSynCerpOwner;

    /**
     * 时区
     */
    @JsonProperty("FTimeZone")
    private String timeZone;

    /**
     * 研发职能
     */
//    @JsonProperty("FDevelopmentBox")
//    private Boolean developmentBox;

}
