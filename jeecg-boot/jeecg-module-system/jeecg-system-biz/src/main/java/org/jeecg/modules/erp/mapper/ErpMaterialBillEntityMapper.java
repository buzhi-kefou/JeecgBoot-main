package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;

import java.util.List;

public interface ErpMaterialBillEntityMapper extends BaseMapper<ErpMaterialBillEntity> {

    int upsertBatch(@Param("list") List<ErpMaterialBillEntity> list);
}
