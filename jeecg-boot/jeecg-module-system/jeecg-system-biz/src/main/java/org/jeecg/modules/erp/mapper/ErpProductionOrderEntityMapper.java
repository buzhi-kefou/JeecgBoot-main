package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.dto.ProductionOrderQuery;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.vo.ProductionOrderListVo;

import java.util.List;

public interface ErpProductionOrderEntityMapper extends BaseMapper<ErpProductionOrderEntity> {

    int upsertBatch(@Param("list") List<ErpProductionOrderEntity> list);


    Page<ProductionOrderListVo> customizeSelectPage(IPage<ProductionOrderListVo> page,
                                                    @Param("query") ProductionOrderQuery query);
}
