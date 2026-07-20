package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.entity.ErpSalesOrderFinanceEntity;

import java.util.List;

public interface ErpSalesOrderFinanceEntityMapper extends BaseMapper<ErpSalesOrderFinanceEntity> {

    int upsertBatch(@Param("list") List<ErpSalesOrderFinanceEntity> list);
}
