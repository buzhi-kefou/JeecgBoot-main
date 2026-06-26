package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金蝶云·星空 销售订单 DTO
 *
 * @author generated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("erp_sales_order")
public class ErpSalesOrderEntity extends ErpCommonEntity {

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

    /** 销售组织（必填项） */
    @JsonProperty("FSaleOrgId")
    private String saleOrgId;

    /** 日期（必填项） */
    @JsonProperty("FDate")
    private String date;

    /** 客户（必填项） */
    @JsonProperty("FCustId")
    private String custId;

    /** 销售部门 */
    @JsonProperty("FSaleDeptId")
    private String saleDeptId;

    /** 销售组 */
    @JsonProperty("FSaleGroupId")
    private String saleGroupId;

    /** 销售员（必填项） */
    @JsonProperty("FSalerId")
    private String salerId;

    /** 收货方 */
    @JsonProperty("FReceiveId")
    private String receiveId;

    /** 结算方 */
    @JsonProperty("FSettleId")
    private String settleId;

    /** 结算方地址 */
    @JsonProperty("FSettleAddress")
    private String settleAddress;

    /** 付款方 */
    @JsonProperty("FChargeId")
    private String chargeId;

    /** 创建人 */
    @JsonProperty("FCreatorId")
    private String creatorId;

    /** 创建日期 */
    @JsonProperty("FCreateDate")
    private String createDate;

    /** 最后修改人 */
    @JsonProperty("FModifierId")
    private String modifierId;

    /** 最后修改日期 */
    @JsonProperty("FModifyDate")
    private String modifyDate;

    /** 审核人 */
    @JsonProperty("FApproverId")
    private String approverId;

    /** 审核日期 */
    @JsonProperty("FApproveDate")
    private String approveDate;

    /** 关闭状态 */
    @JsonProperty("FCloseStatus")
    private String closeStatus;

    /** 关闭人 */
    @JsonProperty("FCloserId")
    private String closerId;

    /** 关闭日期 */
    @JsonProperty("FCloseDate")
    private String closeDate;

    /** 作废状态 */
    @JsonProperty("FCancelStatus")
    private String cancelStatus;

    /** 作废人 */
    @JsonProperty("FCancellerId")
    private String cancellerId;

    /** 作废日期 */
    @JsonProperty("FCancelDate")
    private String cancelDate;

    /** 版本号 */
    @JsonProperty("FVersionNo")
    private String versionNo;

    /** 变更人 */
    @JsonProperty("FChangerId")
    private String changerId;

    /** 变更日期 */
    @JsonProperty("FChangeDate")
    private String changeDate;

    /** 变更原因 */
    @JsonProperty("FChangeReason")
    private String changeReason;

    /** 单据类型（必填项） */
    @JsonProperty("FBillTypeID")
    private String billTypeId;

    /** 业务类型 */
    @JsonProperty("FBusinessType")
    private String businessType;

    /** 交货方式 */
    @JsonProperty("FHeadDeliveryWay")
    private String headDeliveryWay;

    /** 收货方地址 */
    @JsonProperty("FReceiveAddress")
    private String receiveAddress;

    /** 交货地点 */
    @JsonProperty("FHEADLOCID")
    private String headLocId;

    /** 信用检查结果 */
    @JsonProperty("FCreditCheckResult")
    private String creditCheckResult;

    /** 对应组织 */
    @JsonProperty("FCorrespondOrgId")
    private String correspondOrgId;

    /** 收货方联系人 */
    @JsonProperty("FReceiveContact")
    private String receiveContact;

    /** 移动销售订单编号 */
    @JsonProperty("FNetOrderBillNo")
    private String netOrderBillNo;

    /** 移动销售订单ID */
    @JsonProperty("FNetOrderBillId")
    private String netOrderBillId;

    /** 商机内码 */
    @JsonProperty("FOppID")
    private String oppId;

    /** 销售阶段 */
    @JsonProperty("FSalePhaseID")
    private String salePhaseId;

    /** 是否期初单据 */
    @JsonProperty("FISINIT")
    private String isInit;

    /** 备注 */
    @JsonProperty("FNote")
    private String note;

    /** 来自移动（弃用） */
    @JsonProperty("FIsMobile")
    private String isMobile;

    /** 签收状态 */
    @JsonProperty("FSignStatus")
    private String signStatus;

    /** 是否直接变更过程中（不存储） */
//    @JsonProperty("FIsDirectChange")
//    private String isDirectChange;

    /** 是否手工关闭 */
    @JsonProperty("FManualClose")
    private String manualClose;

    /** 收货人姓名 */
    @JsonProperty("FLinkMan")
    private String linkMan;

    /** 联系电话 */
    @JsonProperty("FLinkPhone")
    private String linkPhone;

    /** 订单来源 */
    @JsonProperty("FSOFrom")
    private String soFrom;

    /** 合同类型 */
    @JsonProperty("FContractType")
    private String contractType;

    /** 销售合同内码 */
    @JsonProperty("FContractId")
    private String contractId;

    /** 生成受托加工材料清单 */
    @JsonProperty("FIsUseOEMBomPush")
    private String isUseOemBomPush;

    /** 变更单主键 */
    @JsonProperty("FXPKID_H")
    private String xpkIdH;

    /** 关闭原因 */
    @JsonProperty("FCloseReason")
    private String closeReason;

    /** 生成分销采购订单 */
    @JsonProperty("FIsUseDrpSalePOPush")
    private String isUseDrpSalePoPush;

    /** 生成直运出入库 */
    @JsonProperty("FIsCreateStraightOutIN")
    private String isCreateStraightOutIn;

    /** 预设基础资料字段1 */
    @JsonProperty("FPRESETBASE1")
    private String presetBase1;

    /** 预设辅助资料字段1 */
    @JsonProperty("FPRESETASSISTANT1")
    private String presetAssistant1;

    /** 预设辅助资料字段2 */
    @JsonProperty("FPRESETASSISTANT2")
    private String presetAssistant2;

    /** 预设基础资料字段2 */
    @JsonProperty("FPRESETBASE2")
    private String presetBase2;

    // ==================== 自定义字段 ====================

    /** 订单类型 */
    @JsonProperty("F_ZGHY_DDLX")
    private String zghyDdlx;

    /** 内外销 */
    @JsonProperty("F_ZGHY_NWX")
    private String zghyNwx;

    /** 客户订单号 */
    @JsonProperty("F_ZGHY_FKHDDH")
    private String zghyFkhddh;

    /** 中间客户 */
    @JsonProperty("F_ZGHY_ZJKH")
    private String zghyZjkH;

    /** 销售方式 */
    @JsonProperty("F_ZGHY_XSFS")
    private String zghyXsfs;

    /** 每箱数量 */
    @JsonProperty("F_ZGHY_MXSL")
    private String zghyMxsl;

    /** 跟单人 */
    @JsonProperty("F_ZGHY_GDR")
    private String zghyGdr;

    /** 其他备注 */
    @JsonProperty("F_ZGHY_QTBZ")
    private String zghyQtbz;

    /** 中信保投保 */
    @JsonProperty("F_ZGHY_ZXBTB1")
    private String zghyZxbtb1;

    /** 国内委托方 */
    @JsonProperty("F_ZGHY_GNWTF")
    private String zghyGnwtf;

    /** 价格术语 */
    @JsonProperty("F_ZGHY_JGSY")
    private String zghyJgsy;

    /** 付款期限 */
    @JsonProperty("F_ZGHY_FKQX")
    private String zghyFkqx;

    /** 运输方式 */
    @JsonProperty("F_ZGHY_YSFS")
    private String zghyYsfs;

    /** 出口港 */
    @JsonProperty("F_OUTGK")
    private String outGk;

    /** 装运期限 */
    @JsonProperty("F_ZGHY_ZYQX")
    private String zghyZyqx;

    /** 市场返还(Rebate) */
    @JsonProperty("F_ZGHY_SCFH")
    private String zghyScfh;

    /** 市场佣金(Marketing Fund) */
    @JsonProperty("F_ZGHY_SCYJ")
    private String zghyScyj;

    /** 价格术语描述 */
    @JsonProperty("F_ZGHY_JGSYMS")
    private String zghyJgsyms;

    /** 付款期限描述 */
    @JsonProperty("F_ZGHY_FKQXMS")
    private String zghyFkqxms;

    /** 承运方 */
    @JsonProperty("F_ZGHY_CYF")
    private String zghyCyf;

    /** 中转港 */
    @JsonProperty("F_ZGHY_ZZG")
    private String zghyZzg;

    /** 装运期限描述 */
    @JsonProperty("F_ZGHY_ZYQXMS")
    private String zghyZyqxms;

    /** 溢短装率% */
    @JsonProperty("F_ZGHY_YDZL")
    private String zghyYdzl;

    /** 运抵国 */
    @JsonProperty("F_ZGHY_FYDG")
    private String zghyFydg;

    /** 目的国 */
    @JsonProperty("F_ZGHY_FMDG")
    private String zghyFmdg;

    /** 目的港口 */
    @JsonProperty("F_ZGHY_TMDGK")
    private String zghyTmdgk;

    /** 运转 */
    @JsonProperty("F_ZGHY_FYZ")
    private String zghyFyz;

    /** 分批 */
    @JsonProperty("F_ZGHY_FFP")
    private String zghyFfp;

    /** 物料描述 */
    @JsonProperty("F_ZGHY_WLMS")
    private String zghyWlms;

    /** 唛头 */
    @JsonProperty("F_ZGHY_FMT")
    private String zghyFmt;

    /** 条款备注 */
    @JsonProperty("F_ZGHY_TKBZ")
    private String zghyTkbz;

    /** 保险险种 */
    @JsonProperty("F_ZGHY_BXXZ")
    private String zghyBxxz;

    /** 保险加成率% */
    @JsonProperty("F_ZGHY_BXJCL")
    private String zghyBxjcl;

    /** 客户名称 */
    @JsonProperty("F_ZGHY_KHMC")
    private String zghyKhmc;

    /** 客户地址 */
    @JsonProperty("F_ZGHY_KHDZ")
    private String zghyKhdZ;

    /** 电子邮件 */
    @JsonProperty("F_ZGHY_DZYJ")
    private String zghyDzyj;

    /** 联系人 */
    @JsonProperty("F_ZGHY_LXR")
    private String zghyLxr;

    /** 公司名称 */
    @JsonProperty("F_ZGHY_GSMC")
    private String zghyGsmc;

    /** 公司地址 */
    @JsonProperty("F_ZGHY_GSDZ")
    private String zghyGsdz;

    /** 电子邮件 */
    @JsonProperty("F_ZGHY_GSDZYJ")
    private String zghyGsdzyj;

    /** 柜型 */
    @JsonProperty("F_ZGHY_GX1")
    private String zghyGx1;

    /** 每箱数量 */
    @JsonProperty("F_ZGHY_FMXSL")
    private String zghyFmxsl;

    /** 英文名称 */
    @JsonProperty("F_ZGHY_YWMC")
    private String zghyYwmc;

    /** 英文地址 */
    @JsonProperty("F_ZGHY_YWDZ")
    private String zghyYwdz;

    /** 客户电话 */
    @JsonProperty("F_ZGHY_KHDH")
    private String zghyKhdh;

    /** 英文名称 */
    @JsonProperty("F_ZGHY_GSYWDZ")
    private String zghyGsywdz;

    /** 英文地址 */
    @JsonProperty("F_ZGHY_FYWDZ")
    private String zghyFywdz;

    /** 公司电话 */
    @JsonProperty("F_ZGHY_GSDH")
    private String zghyGsdh;

    /** 客户信息 */
    @JsonProperty("F_ZGHY_KHXX")
    private String zghyKhxx;

    /** 客户邮编 */
    @JsonProperty("F_ZGHY_KHYB")
    private String zghyKhyb;

    /** 客户简称 */
    @JsonProperty("F_ZGHY_KHJC")
    private String zghyKhjc;

    /** 公司简称 */
    @JsonProperty("F_ZGHY_GSJC")
    private String zghyGsjc;

    /** 公司传真 */
    @JsonProperty("F_ZGHY_GSCZ")
    private String zghyGscz;

    /** 公司邮编 */
    @JsonProperty("F_ZGHY_GSYB")
    private String zghyGsyb;

    /** 图片(文件服务器) */
    @JsonProperty("F_ZGHY_ImageFileServer")
    private String zghyImageFileServer;

    /** 图片(文件服务器) */
    @JsonProperty("F_ZGHY_ImageFileServer1")
    private String zghyImageFileServer1;

    /** 图片(文件服务器) */
    @JsonProperty("F_ZGHY_ImageFileServer2")
    private String zghyImageFileServer2;

    /** 图片(文件服务器) */
    @JsonProperty("F_ZGHY_ImageFileServer3")
    private String zghyImageFileServer3;

    /** 结算标志 */
    @JsonProperty("F_ZGHY_JSBZ")
    private String zghyJsbz;

    /** 结算日期 */
    @JsonProperty("F_ZGHY_JSRQ")
    private String zghyJsrq;

    /** BOM维护标志 */
    @JsonProperty("F_ZGHY_BOMWH")
    private String zghyBomwh;

    /** 结算说明 */
    @JsonProperty("F_ZGHY_JSSM")
    private String zghyJssm;

    /** 中信保代码 */
    @JsonProperty("F_ZGHY_BaseProperty")
    private String zghyBaseProperty;

    /** 刷已审核用 */
    @JsonProperty("F_ZGHY_Text")
    private String zghyText;

    /** 洲别 */
    @JsonProperty("F_ZGHY_BaseProperty1")
    private String zghyBaseProperty1;

    /** 地址 */
    @JsonProperty("F_ZGHY_DZ")
    private String zghyDz;

    /** 运抵洲 */
    @JsonProperty("F_UKPT_YDZ")
    private String ukptYdz;

    /** 独立项目 */
    @JsonProperty("F_UKPT_DLXM")
    private String ukptDlxm;

    /** 一审 */
    @JsonProperty("F_UKPT_YS")
    private String ukptYs;

    /** 二审 */
    @JsonProperty("F_UKPT_ES")
    private String ukptEs;

    /** 三审 */
    @JsonProperty("F_UKPT_SS")
    private String ukptSs;

    /** 中信保投保 */
    @JsonProperty("F_FYP_ZXBTB")
    private String fypZxbtb;

    /** 交货地点 */
    @JsonProperty("F_CY_JHDD")
    private String cyJhdd;

    /** 反关闭日期 */
    @JsonProperty("FAntiCloseDate")
    private String antiCloseDate;

    /** 终端客户 */
    @JsonProperty("F_UKPT_Text_qtr")
    private String ukptTextQtr;

    /** 触发状态 */
    @JsonProperty("F_LC_CFZT")
    private String lcCfzt;
}
