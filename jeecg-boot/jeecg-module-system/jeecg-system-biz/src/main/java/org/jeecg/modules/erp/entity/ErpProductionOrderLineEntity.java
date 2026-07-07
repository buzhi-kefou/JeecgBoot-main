package org.jeecg.modules.erp.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_production_order_line")
public class ErpProductionOrderLineEntity extends ErpCommonEntity {

    /** 实体主键 */
    @JsonProperty("FTreeEntity_FEntryId")
    @TableId
    private String fEntryId;

    /** 生产订单主表ID */
    @JsonProperty("FID")
    private String pId;

    /** 父级行主键 */
    @JsonProperty("FParentRowId")
    private String fParentRowId;

    /** 行展开类型 */
    @JsonProperty("FRowExpandType")
    private String fRowExpandType;

    /** 行标识 */
    @JsonProperty("FRowId")
    private String fRowId;

    /** 物料编码（必填） */
    @JsonProperty("FMaterialId")
    @NotNull
    private String fMaterialId;

    @JsonProperty("FMaterialId.fnumber")
    private String fMaterialNumber;

    /** 物料名称 */
    @JsonProperty("FMaterialName")
    private String fMaterialName;

    /** 规格型号 */
    @JsonProperty("FSpecification")
    private String fSpecification;

    /** 产品类型（必填） */
    @JsonProperty("FProductType.fcaption")
    @NotNull
    private String fProductType;

    /** 数量 */
    @JsonProperty("FQty")
    private BigDecimal fQty;

    /** 计划完工时间（必填） */
    @JsonProperty("FPlanFinishDate")
    @JSONField(format = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime fPlanFinishDate;

    /** 计划开工时间（必填） */
    @JsonProperty("FPlanStartDate")
    @JSONField(format = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime fPlanStartDate;

    /** 批号 */
    @JsonProperty("FLot")
    private String fLot;

    /** 计划跟踪号 */
    @JsonProperty("FMTONO")
    private String fMTONO;

    /** 项目编号 */
    @JsonProperty("FProjectNo")
    private String fProjectNo;

    /** 基本单位数量 */
    @JsonProperty("FBaseUnitQty")
    private BigDecimal fBaseUnitQty;

    /** 组别 */
    @JsonProperty("FGroup")
    private String fGroup;

    /** BOM版本 */
    @JsonProperty("FBomId.fnumber")
    private String fBomId;

    /** 工艺路线 */
    @JsonProperty("FRoutingId")
    private String fRoutingId;

    /** 成品率% */
    @JsonProperty("FYieldRate")
    private BigDecimal fYieldRate;

    /** 入库上限 */
    @JsonProperty("FStockInLimitH")
    private BigDecimal fStockInLimitH;

    /** 入库下限 */
    @JsonProperty("FStockInLimitL")
    private BigDecimal fStockInLimitL;

    /** 合格品入库选单数量 */
    @JsonProperty("FStockInQuaSelAuxQty")
    private BigDecimal fStockInQuaSelAuxQty;

    /** 基本单位合格品入库选单数量 */
    @JsonProperty("FStockInQuaSelQty")
    private BigDecimal fStockInQuaSelQty;

    /** 基本单位不合格品入库选单数量 */
    @JsonProperty("FStockInFailSelQty")
    private BigDecimal fStockInFailSelQty;

    /** 不合格品入库选单数量 */
    @JsonProperty("FStockInFailSelAuxQty")
    private BigDecimal fStockInFailSelAuxQty;

    /** 基本单位合格品入库数量 */
    @JsonProperty("FStockInQuaQty")
    private BigDecimal fStockInQuaQty;

    /** 合格品入库数量 */
    @JsonProperty("FStockInQuaAuxQty")
    private BigDecimal fStockInQuaAuxQty;

    /** 基本单位不合格品入库数量 */
    @JsonProperty("FStockInFailQty")
    private BigDecimal fStockInFailQty;

    /** 不合格品入库数量 */
    @JsonProperty("FStockInFailAuxQty")
    private BigDecimal fStockInFailAuxQty;

    /** 入库组织（必填） */
    @JsonProperty("FStockInOrgId")
    @NotNull
    private String fStockInOrgId;

    /** 仓库 */
    @JsonProperty("FStockId")
    private String fStockId;

    /** 仓位 */
    @JsonProperty("FStockLocId")
    private String fStockLocId;

    /** 仓位编号 */
//    @JsonProperty("FF100001")
//    private String fF100001;

    /** 仓位编码 */
//    @JsonProperty("FF100069")
//    private String fF100069;

    /** 产出工序 */
    @JsonProperty("FOperId")
    private String fOperId;

    /** 产出作业编码 */
    @JsonProperty("FProcessId")
    private String fProcessId;

    /** 成本权重 */
    @JsonProperty("FCostRate")
    private BigDecimal fCostRate;

    /** 备注 */
    @JsonProperty("FMemoItem")
    private String fMemoItem;

    /** 计划确认日期 */
    @JsonProperty("FPlanConfirmDate")
    private String fPlanConfirmDate;

    /** 排程日期 */
    @JsonProperty("FScheduleDate")
    private String fScheduleDate;

    /** 下达日期 */
    @JsonProperty("FConveyDate")
    private String fConveyDate;

    /** 开工日期 */
    @JsonProperty("FStartDate")
    private String fStartDate;

    /** 完工日期 */
    @JsonProperty("FFinishDate")
    private String fFinishDate;

    /** 结案日期 */
    @JsonProperty("FCloseDate")
    private String fCloseDate;

    /** 结算日期 */
    @JsonProperty("FCostDate")
    private String fCostDate;

    /** 生成方式（必填） */
    @JsonProperty("FCreateType")
    @NotNull
    private String fCreateType;

    /** 来源单据ID */
    @JsonProperty("FSrcBillId")
    private String fSrcBillId;

    /** 来源单据分录内码 */
    @JsonProperty("FSrcBillEntryId")
    private String fSrcBillEntryId;

    /** 销售订单ID */
    @JsonProperty("FSaleOrderId")
    private String fSaleOrderId;

    /** 需求单据 */
    @JsonProperty("FSaleOrderNo")
    private String fSaleOrderNo;

    /** 销售订单分录内码 */
    @JsonProperty("FSaleOrderEntryId")
    private String fSaleOrderEntryId;

    /** 需求组织 */
    @JsonProperty("FRequestOrgId")
    private String fRequestOrgId;

    /** 基本单位汇报选单数量 */
    @JsonProperty("FRepQuaSelQty")
    private BigDecimal fRepQuaSelQty;

    /** 基本单位不合格数量 */
    @JsonProperty("FRepFailQty")
    private BigDecimal fRepFailQty;

    /** 汇报选单数量 */
    @JsonProperty("FRepQuaSelAuxQty")
    private BigDecimal fRepQuaSelAuxQty;

    /** 合格数量 */
    @JsonProperty("FRepQuaAuxQty")
    private BigDecimal fRepQuaAuxQty;

    /** 不合格数量 */
    @JsonProperty("FRepFailAuxQty")
    private BigDecimal fRepFailAuxQty;

    /** 基本单位合格数量 */
    @JsonProperty("FRepQuaQty")
    private BigDecimal fRepQuaQty;

    /** 业务状态 */
    @JsonProperty("FStatus")
    private Integer fStatus;

    /** 业务状态 */
    @JsonProperty("FStatus.fcaption")
    private String fStatusLabel;

    /** 单位（必填） */
    @JsonProperty("FUnitId")
    @NotNull
    private String fUnitId;

    /** 基本单位 */
    @JsonProperty("FBaseUnitId")
    private String fBaseUnitId;

    /** 辅助属性 */
    @JsonProperty("FAuxPropId")
    private String fAuxPropId;

    /** 备注 */
//    @JsonProperty("FF100005")
//    private String fF100005;

    /** 技转前规格 */
//    @JsonProperty("FF100006")
//    private String fF100006;

    /** 技转前名称 */
//    @JsonProperty("FF100007")
//    private String fF100007;

    /** 流水号 */
//    @JsonProperty("FF100004")
//    private String fF100004;

    /** 物料颜色 */
//    @JsonProperty("FF100001")
//    private String fF100001Color;

    /** 物料组件 */
//    @JsonProperty("FF100002")
//    private String fF100002;

    /** 销售订单号辅助属性 */
//    @JsonProperty("FF100008")
//    private String fF100008;

    /** 基本单位入库上限 */
    @JsonProperty("FBaseStockInLimitH")
    private BigDecimal fBaseStockInLimitH;

    /** 基本单位入库下限 */
    @JsonProperty("FBaseStockInLimitL")
    private BigDecimal fBaseStockInLimitL;

    /** 入库上限比例 */
    @JsonProperty("FStockInUlRatio")
    private BigDecimal fStockInUlRatio;

    /** 源单类型 */
    @JsonProperty("FSrcBillType")
    private String fSrcBillType;

    /** 源单编号 */
    @JsonProperty("FSrcBillNo")
    private String fSrcBillNo;

    /** 源单分录行号 */
    @JsonProperty("FSrcBillEntrySeq")
    private String fSrcBillEntrySeq;

    /** 需求单据行号 */
    @JsonProperty("FSaleOrderEntrySeq")
    private String fSaleOrderEntrySeq;

    /** 基本单位成品数量 */
    @JsonProperty("FBaseYieldQty")
    private BigDecimal fBaseYieldQty;

    /** 联副产品分录内码 */
    @JsonProperty("FCopyEntryId")
    private String fCopyEntryId;

    /** 挂起状态 */
    @JsonProperty("FIsSuspend")
    private String fIsSuspend;

    /** 入库下限比例 */
    @JsonProperty("FStockInLlRatio")
    private BigDecimal fStockInLlRatio;

    /** 业务流程 */
    @JsonProperty("FBFLowId")
    private String fBFLowId;

    /** 基本单位汇报不合格选单数量 */
    @JsonProperty("FRepFailSelQty")
    private BigDecimal fRepFailSelQty;

    /** 汇报不合格选单数量 */
    @JsonProperty("FRepFailSelAuxQty")
    private BigDecimal fRepFailSelAuxQty;

    /** 生产车间（必填） */
    @JsonProperty("FWorkShopID.fname")
    @NotNull
    private String fWorkShopID;

    /** 需求类型（必填） */
    @JsonProperty("FReqType")
    @NotNull
    private String fReqType;

    /** 需求优先级 */
    @JsonProperty("FPriority")
    private String fPriority;

    /** 备料套数 */
    @JsonProperty("FSTOCKREADY")
    private BigDecimal fSTOCKREADY;

    /** 基本单位备料数量 */
    @JsonProperty("FBaseStockReady")
    private BigDecimal fBaseStockReady;

    /** 基本单位报废数量 */
    @JsonProperty("FBaseScrapQty")
    private BigDecimal fBaseScrapQty;

    /** 报废数量 */
    @JsonProperty("FScrapQty")
    private BigDecimal fScrapQty;

    /** 基本单位返修数量 */
    @JsonProperty("FBaseRepairQty")
    private BigDecimal fBaseRepairQty;

    /** 返修数量 */
    @JsonProperty("FRepairQty")
    private BigDecimal fRepairQty;

    /** 基本单位报废品入库选单数量 */
    @JsonProperty("FBaseStockInScrapSelQty")
    private BigDecimal fBaseStockInScrapSelQty;

    /** 报废品入库选单数量 */
    @JsonProperty("FStockInScrapSelQty")
    private BigDecimal fStockInScrapSelQty;

    /** 基本单位报废品入库数量 */
    @JsonProperty("FBaseStockInScrapQty")
    private BigDecimal fBaseStockInScrapQty;

    /** 报废品入库数量 */
    @JsonProperty("FStockInScrapQty")
    private BigDecimal fStockInScrapQty;

    /** 入库货主类型 */
    @JsonProperty("FInStockOwnerTypeId")
    private String fInStockOwnerTypeId;

    /** 入库货主 */
    @JsonProperty("FInStockOwnerId")
    private String fInStockOwnerId;

    /** 入库类型-推入库单用 */
    @JsonProperty("FInStockType")
    private String fInStockType;

    /** 产品检验 */
    @JsonProperty("FCheckProduct")
    private String fCheckProduct;

    /** 产出序列 */
    @JsonProperty("FOutPutOptQueue")
    private String fOutPutOptQueue;

    /** 基本单位汇报完成数量 */
    @JsonProperty("FBaseRptFinishQty")
    private BigDecimal fBaseRptFinishQty;

    /** 汇报完成数量 */
    @JsonProperty("FRptFinishQty")
    private BigDecimal fRptFinishQty;

    /** 紧急放行 */
    @JsonProperty("FQAIP")
    private String fQAIP;

    /** 成品数量 */
    @JsonProperty("FYieldQty")
    private BigDecimal fYieldQty;

    /** 倒冲领料 */
    @JsonProperty("FISBACKFLUSH")
    private String fISBACKFLUSH;

    /** 未入库数量 */
    @JsonProperty("FNoStockInQty")
    private BigDecimal fNoStockInQty;

    /** 基本单位未入库数量 */
    @JsonProperty("FBaseNoStockInQty")
    private BigDecimal fBaseNoStockInQty;

    /** 需求来源 */
    @JsonProperty("FReqSrc")
    private String fReqSrc;

    /** 结案人 */
    @JsonProperty("FForceCloserId")
    private String fForceCloserId;

    /** 产线 */
    @JsonProperty("FREMWorkShopId")
    private String fREMWorkShopId;

    /** 排产序号 */
    @JsonProperty("FScheduleSeq")
    private String fScheduleSeq;

    /** 排程开工时间 */
    @JsonProperty("FScheduleStartTime")
    private String fScheduleStartTime;

    /** 排程完工时间 */
    @JsonProperty("FScheduleFinishTime")
    private String fScheduleFinishTime;

    /** 结案类型 */
    @JsonProperty("FCloseType")
    private String fCloseType;

    /** 排程工序拆分数 */
    @JsonProperty("FScheduleProcSplit")
    private String fScheduleProcSplit;

    /** 序列号单位 */
    @JsonProperty("FSNUnitID")
    private String fSNUnitID;

    /** 序列号单位数量 */
    @JsonProperty("FSNQty")
    private BigDecimal fSNQty;

    /** 退库数量 */
    @JsonProperty("FReStkQty")
    private BigDecimal fReStkQty;

    /** 基本单位退库数量 */
    @JsonProperty("FBaseReStkQty")
    private BigDecimal fBaseReStkQty;

    /** 合格品退库数量 */
    @JsonProperty("FReStkQuaQty")
    private BigDecimal fReStkQuaQty;

    /** 基本合格品退库数量 */
    @JsonProperty("FBaseReStkQuaQty")
    private BigDecimal fBaseReStkQuaQty;

    /** 不合格品退库数量 */
    @JsonProperty("FReStkFailQty")
    private BigDecimal fReStkFailQty;

    /** 基本不合格品退库数量 */
    @JsonProperty("FBaseReStkFailQty")
    private BigDecimal fBaseReStkFailQty;

    /** 报废品退库数量 */
    @JsonProperty("FReStkScrapQty")
    private BigDecimal fReStkScrapQty;

    /** 基本报废品退库数量 */
    @JsonProperty("FBaseReStkScrapQty")
    private BigDecimal fBaseReStkScrapQty;

    /** 领补套数 */
    @JsonProperty("FPickMtlQty")
    private BigDecimal fPickMtlQty;

    /** 基本单位领补套数 */
    @JsonProperty("FBasePickMtlQty")
    private BigDecimal fBasePickMtlQty;

    /** 是否手工新增联副产品 */
    @JsonProperty("FISNEWLC")
    private String fISNEWLC;

    /** 领料状态 */
    @JsonProperty("FPickMtrlStatus")
    private String fPickMtrlStatus;

    /** 基本单位发料通知套数 */
    @JsonProperty("FBaseIssueQty")
    private BigDecimal fBaseIssueQty;

    /** 发料通知套数 */
    @JsonProperty("FIssueQty")
    private BigDecimal fIssueQty;

    /** 源拆分订单编号 */
    @JsonProperty("FSrcSplitBillNo")
    private String fSrcSplitBillNo;

    /** 源拆分订单行号 */
    @JsonProperty("FSrcSplitSeq")
    private String fSrcSplitSeq;

    /** 源拆分订单分录内码 */
    @JsonProperty("FSrcSplitEntryId")
    private String fSrcSplitEntryId;

    /** 源拆分生产订单内码 */
    @JsonProperty("FSrcSplitId")
    private String fSrcSplitId;

    /** 变更标志 */
    @JsonProperty("FMOChangeFlag")
    private String fMOChangeFlag;

    /** 上级订单BOM分录内码 */
    @JsonProperty("FSRCBOMENTRYID")
    private String fSRCBOMENTRYID;

    /** 返工品退库数量 */
    @JsonProperty("FReStkReMadeQty")
    private BigDecimal fReStkReMadeQty;

    /** 基本单位返工品退库数量 */
    @JsonProperty("FBaseReStkReMadeQty")
    private BigDecimal fBaseReStkReMadeQty;

    /** 基本单位返工数量 */
    @JsonProperty("FBaseReMadeQty")
    private BigDecimal fBaseReMadeQty;

    /** 返工数量 */
    @JsonProperty("FReMadeQty")
    private BigDecimal fReMadeQty;

    /** 基本单位返工品入库数量 */
    @JsonProperty("FBaseStockInReMadeQty")
    private BigDecimal fBaseStockInReMadeQty;

    /** 返工品入库数量 */
    @JsonProperty("FStockInReMadeQty")
    private BigDecimal fStockInReMadeQty;

    /** 基本单位返工品入库选单数量 */
    @JsonProperty("FBaseStockInReMadeSelQty")
    private BigDecimal fBaseStockInReMadeSelQty;

    /** 返工品入库选单数量 */
    @JsonProperty("FStockInReMadeSelQty")
    private BigDecimal fStockInReMadeSelQty;

    /** 强制结案原因 */
    @JsonProperty("FCloseReason")
    private String fCloseReason;

    /** 基本单位已排产数量 */
    @JsonProperty("FBaseScheduledQtySum")
    private BigDecimal fBaseScheduledQtySum;

    /** 已排产数量 */
    @JsonProperty("FScheduledQtySum")
    private BigDecimal fScheduledQtySum;

    /** 排产状态（必填） */
    @JsonProperty("FScheduleStatus.fcaption")
    @NotNull
    private String fScheduleStatus;

    /** 首检 */
    @JsonProperty("FIsFirstInspect")
    private String fIsFirstInspect;

    /** 首检状态（必填） */
    @JsonProperty("FFirstInspectStatus")
    @NotNull
    private String fFirstInspectStatus;

    /** 计划确认人 */
    @JsonProperty("FConfirmId")
    private String fConfirmId;

    /** 下达人 */
    @JsonProperty("FReleaseId")
    private String fReleaseId;

    /** 开工人 */
    @JsonProperty("FStartID")
    private String fStartID;

    /** 完工人 */
    @JsonProperty("FFinishId")
    private String fFinishId;

    /** 已预留 */
    @JsonProperty("FIsMRP")
    private String fIsMRP;

    /** 基本单位样本破坏数 */
    @JsonProperty("FBaseSampleDamageQty")
    private BigDecimal fBaseSampleDamageQty;

    /** 样本破坏数 */
    @JsonProperty("FSampleDamageQty")
    private BigDecimal fSampleDamageQty;

    /** 启用日排产 */
    @JsonProperty("FISENABLESCHEDULE")
    private String fISENABLESCHEDULE;

    /** BOM展开路径 */
    @JsonProperty("FPathEntryId")
    private String fPathEntryId;

    /** 用料清单分录内码 */
    @JsonProperty("FPPBOMENTRYID")
    private String fPPBOMENTRYID;

    /** BOM分录内码 */
    @JsonProperty("FBOMENTRYID")
    private String fBOMENTRYID;

    /** 用料清单类型 */
    @JsonProperty("FSrcFormID")
    private String fSrcFormID;

    /** 预计齐套数量 */
    @JsonProperty("FMatchQty")
    private BigDecimal fMatchQty;

    /** 库存齐套数 */
    @JsonProperty("FInvMatchQty")
    private BigDecimal fInvMatchQty;

    /** 齐套状况 */
    @JsonProperty("FCompleteCon")
    private String fCompleteCon;

    /** 跟进备注 */
    @JsonProperty("FRemarks")
    private String fRemarks;

    /** 预计齐套日期 */
    @JsonProperty("FMatchDate")
    private String fMatchDate;

    /** 联副产品备注 */
    @JsonProperty("FNOTECOBY")
    private String fNOTECOBY;

    /** 直送合格品入库数量 */
    @JsonProperty("FDirectStockInQuaQty")
    private BigDecimal fDirectStockInQuaQty;

    /** 直送领料选单数 */
    @JsonProperty("FDirectPickMtrlSelQty")
    private String fDirectPickMtrlSelQty;

    /** 直送基本合格品入库数量 */
    @JsonProperty("FBaseDirectStockInQuaQty")
    private BigDecimal fBaseDirectStockInQuaQty;

    /** 直送基本单位领料选单数 */
    @JsonProperty("FBaseDirectPickMtrlSelQty")
    private String fBaseDirectPickMtrlSelQty;

    /** 首检控制方式（必填） */
    @JsonProperty("FFirstQCControlType")
    @NotNull
    private String fFirstQCControlType;

    /** 已计划运算 */
    @JsonProperty("FISMRPCAL")
    private String fISMRPCAL;

    /** 生成下级订单标志 */
    @JsonProperty("FIsGenerateOrder")
    private String fIsGenerateOrder;

    /** 补单销售订单标志 */
    @JsonProperty("F_ZGHY_BDXSDDBZ")
    private String fZghyBdxsddbz;

    /** 已流转标志 */
    @JsonProperty("F_ZGHY_YLZ")
    private String fZghyYlz;

    /** 颜色 */
    @JsonProperty("F_ZGHY_YS")
    private String fZghyYs;

    /** 电器规格 */
    @JsonProperty("F_ZGHY_DQGG")
    private String fZghyDqgg;

    /** 插头类型 */
    @JsonProperty("F_ZGHY_CTLX")
    private String fZghyCtlx;

    /** 品牌 */
    @JsonProperty("F_ZGHY_PP")
    private String fZghyPp;

    /** 线别 */
    @JsonProperty("F_QFMH_XB")
    private String fQfmhXb;

    /** 运抵国 */
    @JsonProperty("F_ZGHY_FYDG")
    private String fZghyFydg;

    /** 销售员 */
    @JsonProperty("FSalerId")
    private String fSalerId;

    /** 物料默认仓库 */
    @JsonProperty("F_ZGHY_BaseProperty")
    private String fZghyBaseProperty;

    /** 物料存货类别 */
    @JsonProperty("F_ZGHY_BaseProperty1")
    private String fZghyBaseProperty1;

    /** 最晚入库日期 */
    @JsonProperty("F_UKPT_ZWRKRQ")
    private String fUkptZwrkrq;

    /** PLM旧物料 */
    @JsonProperty("F_UKPT_PLMJWL")
    private String fUkptPlmjwl;

    /** 老物料名称 */
    @JsonProperty("F_UKPT_LWLMC")
    private String fUkptLwlmc;

    /** 老规格型号 */
    @JsonProperty("F_UKPT_LGGXH")
    private String fUkptLggxh;
}
