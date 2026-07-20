package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.entity.ErpSalesOrderEntity;

import java.util.List;

public interface ErpSalesOrderEntityMapper extends BaseMapper<ErpSalesOrderEntity> {

    int upsertBatch(@Param("list") List<ErpSalesOrderEntity> list);
}
