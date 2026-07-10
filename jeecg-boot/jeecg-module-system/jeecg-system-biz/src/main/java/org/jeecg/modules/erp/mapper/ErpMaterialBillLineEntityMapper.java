package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.entity.ErpMaterialBillLineEntity;

import java.util.List;

public interface ErpMaterialBillLineEntityMapper extends BaseMapper<ErpMaterialBillLineEntity> {

    int upsertBatch(@Param("list") List<ErpMaterialBillLineEntity> list);
}
