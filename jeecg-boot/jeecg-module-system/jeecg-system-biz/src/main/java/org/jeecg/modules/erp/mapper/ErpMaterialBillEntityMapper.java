package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;

import java.util.List;

public interface ErpMaterialBillEntityMapper extends BaseMapper<ErpMaterialBillEntity> {

    int upsertBatch(@Param("list") List<ErpMaterialBillEntity> list);

    /**
     * 查询指定物料在使用组织下的全部下级 BOM 明细。
     */
    List<MaterialBillChildLineDto> selectAllChildLines(@Param("materialCode") String materialCode,
                                                        @Param("useOrgId") Long useOrgId);
}
