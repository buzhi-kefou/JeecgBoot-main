package org.jeecg.modules.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金蝶云·星空 销售出库单 DTO
 *
 * @author generated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("erp_sales_delivery_order")
public class ErpSalesDeliveryOrderEntity extends ErpCommonEntity {

    // ==================== 标准字段 ====================

    /**
     * 实体主键
     */
    @TableId
    @JsonProperty("FID")
    private String fid;

    /**
     * 单据编号
     */
    @JsonProperty("FBillNo")
    private String billNo;

    /**
     * 单据状态
     */
    @JsonProperty("FDocumentStatus")
    private String documentStatus;

    /**
     * 销售组织（必填项）
     */
    @JsonProperty("FSaleOrgId")
    private String saleOrgId;

    /**
     * 日期（必填项）
     */
    @JsonProperty("FDate")
    private String date;

    /**
     * 发货组织（必填项）
     */
    @JsonProperty("FStockOrgId")
    private String stockOrgId;

    /**
     * 客户（必填项）
     */
    @JsonProperty("FCustomerID")
    private String customerId;

    /**
     * 发货部门
     */
    @JsonProperty("FDeliveryDeptID")
    private String deliveryDeptId;

    /**
     * 销售部门
     */
    @JsonProperty("FSaleDeptID")
    private String saleDeptId;

    /**
     * 库存组
     */
    @JsonProperty("FStockerGroupID")
    private String stockerGroupId;

    /**
     * 仓管员
     */
    @JsonProperty("FStockerID")
    private String stockerId;

    /**
     * 销售组
     */
    @JsonProperty("FSalesGroupID")
    private String salesGroupId;

    /**
     * 销售员
     */
    @JsonProperty("FSalesManID")
    private String salesManId;

    /**
     * 承运商
     */
    @JsonProperty("FCarrierID")
    private String carrierId;

    /**
     * 运输单号
     */
    @JsonProperty("FCarriageNO")
    private String carriageNo;

    /**
     * 收货方
     */
    @JsonProperty("FReceiverID")
    private String receiverId;

    /**
     * 结算方
     */
    @JsonProperty("FSettleID")
    private String settleId;

    /**
     * 付款方
     */
    @JsonProperty("FPayerID")
    private String payerId;

    /**
     * 创建日期
     */
    @JsonProperty("FCreateDate")
    private String createDate;

    /**
     * 最后修改人
     */
    @JsonProperty("FModifierId")
    private String modifierId;

    /**
     * 最后修改日期
     */
    @JsonProperty("FModifyDate")
    private String modifyDate;

    /**
     * 创建人
     */
    @JsonProperty("FCreatorId")
    private String creatorId;

    /**
     * 审核人
     */
    @JsonProperty("FApproverID")
    private String approverId;

    /**
     * 审核日期
     */
    @JsonProperty("FApproveDate")
    private String approveDate;

    /**
     * 作废状态
     */
    @JsonProperty("FCancelStatus")
    private String cancelStatus;

    /**
     * 作废人
     */
    @JsonProperty("FCancellerID")
    private String cancellerId;

    /**
     * 作废日期
     */
    @JsonProperty("FCancelDate")
    private String cancelDate;

    /**
     * 单据类型（必填项）
     */
    @JsonProperty("FBillTypeID")
    private String billTypeId;

    /**
     * 货主类型
     */
    @JsonProperty("FOwnerTypeIdHead")
    private String ownerTypeIdHead;

    /**
     * 货主
     */
    @JsonProperty("FOwnerIdHead")
    private String ownerIdHead;

    /**
     * 业务类型
     */
    @JsonProperty("FBussinessType")
    private String bussinessType;

    /**
     * 收货方地址
     */
    @JsonProperty("FReceiveAddress")
    private String receiveAddress;

    /**
     * 交货地点
     */
    @JsonProperty("FHeadLocationId")
    private String headLocationId;

    /**
     * 信用检查结果
     */
    @JsonProperty("FCreditCheckResult")
    private String creditCheckResult;

    /**
     * 跨组织业务类型
     */
    @JsonProperty("FTransferBizType")
    private String transferBizType;

    /**
     * 对应组织
     */
    @JsonProperty("FCorrespondOrgId")
    private String correspondOrgId;

    /**
     * 收货方联系人
     */
    @JsonProperty("FReceiverContactID")
    private String receiverContactId;

    /**
     * 组织间结算跨法人标识
     */
    @JsonProperty("FIsInterLegalPerson")
    private String isInterLegalPerson;

    /**
     * 零售单日结生成
     */
    @JsonProperty("FGenFromPOS_CMK")
    private String genFromPosCmk;

    /**
     * 联系电话
     */
    @JsonProperty("FLinkPhone")
    private String linkPhone;

    /**
     * 收货人姓名
     */
    @JsonProperty("FLinkMan")
    private String linkMan;

    /**
     * 销售门店
     */
    @JsonProperty("FBranchId")
    private String branchId;

    /**
     * 序列号上传
     */
//    @JsonProperty("FScanBox")
//    private String scanBox;

    /**
     * 创建日期偏移单位
     */
//    @JsonProperty("FCDateOffsetUnit")
//    private String cDateOffsetUnit;

    /**
     * 创建日期偏移量
     */
//    @JsonProperty("FCDateOffsetValue")
//    private String cDateOffsetValue;

    /**
     * 交货明细执行地址(后台用)
     */
    @JsonProperty("FPlanRecAddress")
    private String planRecAddress;

    /**
     * 整单服务或费用
     */
    @JsonProperty("FIsTotalServiceOrCost")
    private String isTotalServiceOrCost;

    /**
     * 备注
     */
    @JsonProperty("FNote")
    private String note;

    /**
     * 拆单新单标识
     */
    @JsonProperty("FDisassemblyFlag")
    private String disassemblyFlag;

    /**
     * 网店编码
     */
    @JsonProperty("FSHOPNUMBER")
    private String shopNumber;

    /**
     * 管易发货日期
     */
    @JsonProperty("FGYDATE")
    private String gyDate;

    /**
     * 销售渠道
     */
    @JsonProperty("FSALECHANNEL")
    private String saleChannel;

    /**
     * 物流单号
     */
    @JsonProperty("FLogisticsNos")
    private String logisticsNos;

    /**
     * 预设基础资料字段2
     */
    @JsonProperty("FPRESETBASE2")
    private String presetBase2;

    /**
     * 预设基础资料字段1
     */
    @JsonProperty("FPRESETBASE1")
    private String presetBase1;

    /**
     * 预设辅助资料字段1
     */
    @JsonProperty("FPRESETASSISTANT1")
    private String presetAssistant1;

    /**
     * 预设辅助资料字段2
     */
    @JsonProperty("FPRESETASSISTANT2")
    private String presetAssistant2;

    /**
     * 关联应收状态
     */
    @JsonProperty("FARStatus")
    private String arStatus;

    // ==================== 自定义字段 ====================

    /**
     * 内外销（必填项）
     */
    @JsonProperty("F_ZGHY_NWX")
    private String zghyNwx;

    /**
     * 打印次数
     */
    @JsonProperty("F_ZGHY_PrintTimes")
    private String zghyPrintTimes;

    /**
     * 供应商二维码
     */
    @JsonProperty("F_ZGHY_Text")
    private String zghyText;

    /**
     * 销售订单日期
     */
    @JsonProperty("F_ZGHY_XSDDDate")
    private String zghyXsddDate;

    /**
     * 地址
     */
    @JsonProperty("F_ZGHY_DZ")
    private String zghyDz;

    /**
     * 交货地址
     */
    @JsonProperty("F_ZGHY_JHDZ")
    private String zghyJhdz;

    /**
     * 发货通知日期
     */
    @JsonProperty("F_ZGHY_Date")
    private String zghyDate;

    /**
     * 报关合同号
     */
    @JsonProperty("F_ZGHY_YSHTH")
    private String zghyYshth;

    /**
     * 运抵国
     */
    @JsonProperty("F_UKPT_YDG")
    private String ukptYdg;

    /**
     * 目的国
     */
    @JsonProperty("F_UKPT_MDG")
    private String ukptMdg;

    /**
     * 运抵洲
     */
    @JsonProperty("F_UKPT_YDZ")
    private String ukptYdz;

    /**
     * 预计出口日期
     */
//    @JsonProperty("F_ZPDX_YJCKDAT")
//    private String zpdxYjckdat;
}
