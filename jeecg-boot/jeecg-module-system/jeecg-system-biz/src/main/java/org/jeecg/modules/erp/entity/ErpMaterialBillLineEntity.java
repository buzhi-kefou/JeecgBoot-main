package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_material_bill_line")
public class ErpMaterialBillLineEntity extends ErpCommonEntity {

    /**
     * 头表主键
     */
    private Long billId;

    /**
     * 主键
     */
    @JsonProperty("FTreeEntity_FENTRYID")
    @TableId
    private Long id;

    /** 子项类型 */
    @JsonProperty("FMATERIALTYPE")
    private String materialType;

    /** 用量类型 */
    @JsonProperty("FDOSAGETYPE")
    private String dosageType;

    /** 作业 */
    @JsonProperty("FPROCESSID")
    private Long processId;

    /** 固定损耗 */
    @JsonProperty("FFIXSCRAPQTY")
    private BigDecimal fixScrapQty;

    /** 生效日期 */
    @JsonProperty("FEFFECTDATE")
    private LocalDateTime effectDate;

    /** 失效日期 */
    @JsonProperty("FEXPIREDATE")
    private LocalDateTime expireDate;

    /** 发料方式 */
    @JsonProperty("FISSUETYPE")
    private String issueType;

    /** 发料组织 */
    @JsonProperty("FSUPPLYORG")
    private Long supplyOrg;

    /** 默认发料仓库 */
    @JsonProperty("FSTOCKID")
    private Long stockId;

    /** 默认发料仓位 */
    @JsonProperty("FSTOCKLOCID")
    private Long stockLocId;

    /** 允许超发 */
    @JsonProperty("FALLOWOVER")
    private Boolean allowOver;

    /** 倒冲时机 */
    @JsonProperty("FBACKFLUSHTYPE")
    private String backFlushType;

    /** 时间单位 */
    @JsonProperty("FTIMEUNIT")
    private String timeUnit;

    /** 是否关键件 */
    @JsonProperty("FISKEYCOMPONENT")
    private Boolean isKeyComponent;

    /** 子项物料编码 */
    @JsonProperty("FMATERIALIDCHILD")
    private String materialIdChild;

    @JsonProperty("FMATERIALIDCHILD.fnumber")
    private String materialCodeChild;

    /**
     * 子项物料名称
     */
    @JsonProperty("FCHILDITEMNAME")
    private String materialNameChild;

    /**
     * 子项物料属性
     */
    @JsonProperty("FCHILDITEMPROPERTY")
    private String materialPropertyChild;

    /**
     * 子项物料型号
     */
    @JsonProperty("FCHILDITEMMODEL")
    private String materialModelChild;

    /** 位置号 */
    @JsonProperty("FPOSITIONNO")
    private String positionNo;

    /** 变动损耗率% */
    @JsonProperty("FSCRAPRATE")
    private BigDecimal scrapRate;

    /** 拆卸成本拆分比例 */
    @JsonProperty("FDISASSMBLERATE")
    private BigDecimal disAssmbleRate;

    /** 偏置时间 */
    @JsonProperty("FOFFSETTIME")
    private BigDecimal offsetTime;

    /** 是否发损耗 */
    @JsonProperty("FISGETSCRAP")
    private Boolean isGetScrap;

    /** 子项BOM版本 */
    @JsonProperty("FBOMID")
    private Long bomId;

    /** 用量:分子 */
    @JsonProperty("FNUMERATOR")
    private BigDecimal numerator;

    /** 用量:分母 */
    @JsonProperty("FDENOMINATOR")
    private BigDecimal denominator;

    /** 货主类型 */
    @JsonProperty("FOWNERTYPEID")
    private String ownerTypeId;

    /** 货主 */
    @JsonProperty("FOWNERID")
    private Long ownerId;

    /** 工序 */
    @JsonProperty("FOPERID")
    private Long operId;

    /** 辅助属性 */
    @JsonProperty("FAuxPropId")
    private Long auxPropId;

    /** 子项单位 */
    @JsonProperty("FCHILDUNITID")
    private Long childUnitId;

    /** 子项标识 */
    @JsonProperty("FEntryRowId")
    private String entryRowId;

    /** 子项基本单位 */
    @JsonProperty("FChildBaseUnitID")
    private Long childBaseUnitId;

    /** 基本单位分子 */
    @JsonProperty("FBaseNumerator")
    private BigDecimal baseNumerator;

    /** 基本单位固定损耗 */
    @JsonProperty("FBaseFixscrapQty")
    private BigDecimal baseFixscrapQty;

    /** 基本单位分母 */
    @JsonProperty("FBaseDenominator")
    private BigDecimal baseDenominator;

    /** 标准用量 */
    @JsonProperty("FQty2")
    private BigDecimal qty2;

    /** 实际用量 */
    @JsonProperty("FActualQty")
    private BigDecimal actualQty;

    /** 超发控制方式 */
    @JsonProperty("FOverControlMode")
    private String overControlMode;

    /** 项次 */
    @JsonProperty("FReplaceGroup")
    private Long replaceGroup;

    /** 供应组织 */
    @JsonProperty("FChildSupplyOrgId")
    private Long childSupplyOrgId;

    /** 工序序列 */
    @JsonProperty("FOptQueue")
    private String optQueue;

    /** 领料考虑最小发料批量 */
    @JsonProperty("FISMinIssueQty")
    private Boolean isMinIssueQty;

    /** 记录字段 */
    @JsonProperty("FRecordData")
    private String recordData;

    /** 供应类型 */
    @JsonProperty("FSupplyType")
    private String supplyType;

    /** 预留组织 */
    @JsonProperty("F_UKPT_YLZZ")
    private String ukptYlzz;

    /** 父级行主键 */
    @JsonProperty("FParentRowId")
    private String parentRowId;

    /** 行展开类型 */
    @JsonProperty("FRowExpandType")
    private String rowExpandType;

    /** 行标识 */
    @JsonProperty("FRowId")
    private String rowId;

    /** 替代策略 */
    @JsonProperty("FReplacePolicy")
    private String replacePolicy;

    /** 替代方式 */
    @JsonProperty("FReplaceType")
    private String replaceType;

    /** 替代优先级 */
    @JsonProperty("FReplacePriority")
    private Integer replacePriority;

    /** 替代主料 */
    @JsonProperty("FIskeyItem")
    private Boolean isKeyItem;

    /** 动态优先级 */
    @JsonProperty("FMRPPriority")
    private Integer mrpPriority;

    /** 可选择 */
    @JsonProperty("FIsCanChoose")
    private Boolean isCanChoose;

    /** 可修改 */
    @JsonProperty("FIsCanEdit")
    private Boolean isCanEdit;

    /** 可替换 */
    @JsonProperty("FIsCanReplace")
    private Boolean isCanReplace;

    /** 配置BOM分录内码 */
    @JsonProperty("FCFGBOMENTRYID")
    private Long cfgBomEntryId;

    /** 父项特征件分录内码 */
    @JsonProperty("FCfgFeatureEntryId")
    private Long cfgFeatureEntryId;

    /** PLMBOM分录内码 */
    @JsonProperty("FPLMBOMEntryId")
    private Long plmBomEntryId;

    /** BOM分录来源 */
    @JsonProperty("FBOMEntrySRC")
    private String bomEntrySrc;

    /** 跳层 */
    @JsonProperty("FISSkip")
    private Boolean isSkip;

    /** 子项明细Id备份(引入时与BOP关联) */
    @JsonProperty("FTreeEntryIdBak")
    private Long treeEntryIdBak;

    /** 变更类型 */
    @JsonProperty("FChangeType")
    private String changeType;

    /** 变更日期 */
    @JsonProperty("FChangeTime")
    private LocalDateTime changeTime;

    /** 变更单号 */
    @JsonProperty("FECNBillNo")
    private String ecnBillNo;

    /** 变更类型 */
    @JsonProperty("FECNChgType")
    private String ecnChgType;

    /** 变更日期 */
    @JsonProperty("FECNChgDate")
    private LocalDateTime ecnChgDate;

    /** 供料方式 */
    @JsonProperty("FSupplyMode")
    private String supplyMode;

    /** ECN行类型 */
    @JsonProperty("FECNRowType")
    private String ecnRowType;

    /** CloudPLMBOM分录内码 */
    @JsonProperty("FPLMBOMENTRYROWID")
    private String plmBomEntryRowId;

    /** MRP运算 */
    @JsonProperty("FIsMrpRun")
    private Boolean isMrpRun;

    /** 替代方案编码 */
    @JsonProperty("FSubstitutionId")
    private String substitutionId;

    /** 替代方案分录内码 */
    @JsonProperty("FSTEntryId")
    private Long stEntryId;

    /** 净需求比例(%) */
    @JsonProperty("FNETDEMANDRATE")
    private BigDecimal netDemandRate;

    /** 变更单分录内码 */
    @JsonProperty("FEcnEntryId")
    private Long ecnEntryId;
}
