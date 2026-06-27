package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;

import java.util.List;

public interface ErpProductionOrderEntityMapper extends BaseMapper<ErpProductionOrderEntity> {

    int upsertBatch(@Param("list") List<ErpProductionOrderEntity> list);
}
