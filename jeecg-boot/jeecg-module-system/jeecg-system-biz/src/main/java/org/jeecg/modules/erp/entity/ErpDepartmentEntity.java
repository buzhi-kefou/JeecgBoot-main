package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("erp_department")
public class ErpDepartmentEntity extends ErpCommonEntity {

    /** 实体主键 */
    @JsonProperty("FDEPTID")
    @TableId("f_deptid")
    private String fDEPTID;

    /** 数据状态 */
    @JsonProperty("FDocumentStatus")
    private String fDocumentStatus;

    /** 禁用状态 */
    @JsonProperty("FForbidStatus")
    private String fForbidStatus;

    /** 名称（必填） */
    @JsonProperty("FName")
    @NotNull
    private String fName;

    /** 编码 */
    @JsonProperty("FNumber")
    private String fNumber;

    /** 描述 */
    @JsonProperty("FDescription")
    private String fDescription;

    /** 创建组织（必填） */
    @JsonProperty("FCreateOrgId")
    @NotNull
    private String fCreateOrgId;

    /** 使用组织（必填） */
    @JsonProperty("FUseOrgId")
    @NotNull
    private String fUseOrgId;

    /** 创建人 */
    @JsonProperty("FCreatorId")
    private String fCreatorId;

    /** 修改人 */
    @JsonProperty("FModifierId")
    private String fModifierId;

    /** 创建日期 */
    @JsonProperty("FCreateDate")
    private String fCreateDate;

    /** 修改日期 */
    @JsonProperty("FModifyDate")
    private String fModifyDate;

    /** 助记码 */
    @JsonProperty("FHelpCode")
    private String fHelpCode;

    /** 部门全称 */
    @JsonProperty("FFullName")
    private String fFullName;

    /** 生效日期 */
    @JsonProperty("FEffectDate")
    private String fEffectDate;

    /** 失效日期 */
    @JsonProperty("FLapseDate")
    private String fLapseDate;

    /** 审核人 */
    @JsonProperty("FAuditorID")
    private String fAuditorId;

    /** 审核日期 */
    @JsonProperty("FAuditDate")
    private String fAuditDate;

    /** 上级部门 */
    @JsonProperty("FParentID")
    private String fParentId;

    /** 禁用人 */
    @JsonProperty("FForbidderID")
    private String fForbidderId;

    /** 禁用日期 */
    @JsonProperty("FForbidDate")
    private String fForbidDate;

    /** HR部门 */
    @JsonProperty("FIsHRDept")
    private String fIsHrdept;

    /** 对应业务组织 */
//    @JsonProperty("FOrg")
//    private String fOrg;

    /** 业务组织本部 */
//    @JsonProperty("FIsOrg")
//    private String fIsOrg;

    /** 车间 */
    @JsonProperty("FIsStock")
    private String fIsStock;

    /** WIP仓库 */
    @JsonProperty("FWIPStockID")
    private String fWipstockId;

    /** WIP仓位 */
    @JsonProperty("FWIPLocationID")
    private String fWiplocationId;

    /** 仓位编号 */
//    @JsonProperty("FF100001")
//    private String fF100001;

    /** 仓位编码 */
//    @JsonProperty("FF100069")
//    private String fF100069;

    /** 层级码 */
    @JsonProperty("FLevelCode")
    private String fLevelCode;

    /** 深度 */
    @JsonProperty("FDepth")
    private String fDepth;

    /** 是否根节点 */
    @JsonProperty("FIsRoot")
    private String fIsRoot;

    /** 部门属性 */
    @JsonProperty("FDeptProperty")
    private String fDeptProperty;

    /** 部门分组 */
    @JsonProperty("FGroup")
    private String fGroup;

    /** 来源于s-HR */
    @JsonProperty("FIsSHR")
    private String fIsShr;

    /** 副产品倒冲 */
    @JsonProperty("FIsCopyFlush")
    private String fIsCopyFlush;

    /** 更新已排 */
    @JsonProperty("FFinishQtyDepend")
    private String fFinishQtyDepend;

    /** 是否明细部门 */
    @JsonProperty("FIsDetailDpt")
    private String fIsDetailDpt;
}
