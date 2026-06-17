package org.jeecg.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_material")
public class ErpMaterialEntity extends ErpCommonEntity {

    /**
     * 实体主键
     */
    @TableId
    @JsonProperty("FMATERIALID")
    private Long materialId;

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
    private String createOrgId;

    /**
     * 使用组织（必填项）
     */
    @JsonProperty("FUseOrgId")
    private String useOrgId;

    /**
     * 创建人
     */
    @JsonProperty("FCreatorId")
    private String creatorId;

    /**
     * 修改人
     */
    @JsonProperty("FModifierId")
    private String modifierId;

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
     * 助记码
     */
    @JsonProperty("FMnemonicCode")
    private String mnemonicCode;

    /**
     * 规格型号
     */
    @JsonProperty("FSpecification")
    private String specification;

    /**
     * 禁用人
     */
    @JsonProperty("FForbidderId")
    private String forbidderId;

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
     * 审核人
     */
    @JsonProperty("FApproverId")
    private String approverId;

    /**
     * 图片(数据库)
     */
    @JsonProperty("FImage1")
    private String image1;

    /**
     * 旧物料编码
     */
    @JsonProperty("FOldNumber")
    private String oldNumber;

    /**
     * 物料分组
     */
    @JsonProperty("FMaterialGroup")
    private String materialGroup;

    /**
     * 物料分组编码
     */
    @JsonProperty("FBaseProperty")
    private String baseProperty;

    /**
     * PLM物料内码
     */
    @JsonProperty("FPLMMaterialId")
    private String plmMaterialId;

    /**
     * 物料来源
     */
    @JsonProperty("FMaterialSRC")
    private String materialSrc;

    /**
     * 图片(文件服务器)
     */
    @JsonProperty("FImageFileServer")
    private String imageFileServer;

    /**
     * 图片存储类型
     */
    @JsonProperty("FImgStorageType")
    private String imgStorageType;


    /**
     * 是否网销
     */
    @JsonProperty("FIsSalseByNet")
    private Boolean isSalseByNet;

    /**
     * 自动分配
     */
    @JsonProperty("FIsAutoAllocate")
    private Boolean isAutoAllocate;

    /**
     * SPU信息
     */
    @JsonProperty("FSPUID")
    private String spuid;

    /**
     * 拼音
     */
    @JsonProperty("FPinYin")
    private String pinYin;

    /**
     * 按批号匹配供需
     */
    @JsonProperty("FDSMatchByLot")
    private Boolean dsMatchByLot;

    /**
     * 禁用原因
     */
    @JsonProperty("FForbidReson")
    private String forbidReson;

    /**
     * 已使用
     */
    @JsonProperty("FRefStatus")
    private String refStatus;

    /**
     * 可手工预留
     */
    @JsonProperty("FIsHandleReserve")
    private Boolean isHandleReserve;

    /**
     * 销售出库替代物料
     */
    @JsonProperty("FMulReplaceMaterialId")
    private String mulReplaceMaterialId;

    /**
     * 老物料名称
     */
    @JsonProperty("F_ZGHY_Text")
    private String text;

    /**
     * 老规格型号**
     */
    @JsonProperty("F_ZGHY_Text1")
    private String text1;

    /**
     * 老规格型号
     */
    @JsonProperty("F_ZGHY_LGG")
    private String lgg;

    /**
     * 辅助字段
     */
    @JsonProperty("F_ZGHY_Text2")
    private String text2;

    /**
     * 基础资料
     */
    @JsonProperty("F_ZGHY_Base")
    private String base;

    /**
     * 苏泊尔编码
     */
    @JsonProperty("F_ZGHY_SBE")
    private String sbe;

    /**
     * 苏泊尔描述
     */
    @JsonProperty("F_ZGHY_SBEMS")
    private String sbems;

    /**
     * 红心物料编码
     */
    @JsonProperty("F_ZGHY_HXWLBM")
    private String hxwlbm;

    /**
     * 红心标志
     */
    @JsonProperty("F_ZGHY_HXBZ")
    private String hxbz;

    /**
     * 模号
     */
    @JsonProperty("F_ZGHY_FMH")
    private String fmh;

    /**
     * 模穴数
     */
    @JsonProperty("F_ZGHY_FMXS")
    private String fmxs;

    /**
     * 用料类型
     */
    @JsonProperty("F_ZGHY_YLLX")
    private String yllx;

    /**
     * 模具类型
     */
    @JsonProperty("F_ZGHY_MJLX")
    private String mjlx;

    /**
     * 模具材料
     */
    @JsonProperty("F_ZGHY_FMJCL")
    private String fmjcl;

    /**
     * 模具编号
     */
    @JsonProperty("F_ZGHY_FMJNUMBER")
    private String fmjnumber;

    /**
     * 流转每箱数量
     */
    @JsonProperty("F_ZGHY_LZMXSL")
    private Integer lzmxsl;

    /**
     * 是否跳层
     */
    @JsonProperty("F_ZGHY_SFTC")
    private Boolean sftc;

    /**
     * 老K3编码
     */
    @JsonProperty("F_ZGHY_LK3BM")
    private String lk3bm;

    /**
     * 产品分类
     */
    @JsonProperty("F_UKPT_CPFL")
    private String cpfl;

    /**
     * 费用项目
     */
    @JsonProperty("F_UKPT_FY")
    private String fy;

    /**
     * PLM旧物料
     */
    @JsonProperty("F_UKPT_PLMJWL")
    private String plmjwl;

    /**
     * PLM图号
     */
    @JsonProperty("F_UKPT_PLMTH")
    private String plmth;

    /**
     * 名称规格型号
     */
    @JsonProperty("F_UKPT_Text_83g")
    private String text83g;

    /**
     * 23年物料编码
     */
    @JsonProperty("F_UKPT_23WLBM")
    private String materialCode2023;

    /**
     * 23年物料名称
     */
    @JsonProperty("F_UKPT_23WLMC")
    private String materialName2023;

    /**
     * 23年规格型号
     */
    @JsonProperty("F_UKPT_23GGXH")
    private String specification2023;

    /**
     * 是否启用批号
     */
    @JsonProperty("F_XGBW_SFQYPH")
    private Boolean sfqyph;

    /**
     * 费用项目
     */
    @JsonProperty("F_UKPT_FYXM")
    private String fyxm;

    /**
     * 是否录入工价
     */
    @JsonProperty("F_UKPT_SFLCQWL")
    private Boolean sflcqwl;

    /**
     * PLM创建人
     */
    @JsonProperty("F_UKPT_PLMCJR")
    private String plmcjr;


}