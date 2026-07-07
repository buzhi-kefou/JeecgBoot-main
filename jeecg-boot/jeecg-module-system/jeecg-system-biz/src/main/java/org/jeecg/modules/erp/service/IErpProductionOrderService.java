package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.ProductionOrderQuery;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.vo.ProductionOrderListVo;

import java.util.List;

public interface IErpProductionOrderService extends IService<ErpProductionOrderEntity> {

    List<ErpProductionOrderEntity> queryByDate(String beginDateStr, String endDateStr);

    Page<ProductionOrderListVo> queryProductionOrderPage(ProductionOrderQuery query);
}
