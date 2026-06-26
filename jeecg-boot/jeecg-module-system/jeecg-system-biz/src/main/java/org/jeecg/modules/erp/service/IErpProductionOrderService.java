package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;

import java.util.List;

public interface IErpProductionOrderService extends IService<ErpProductionOrderEntity> {

    List<ErpProductionOrderEntity> queryByDate(String beginDateStr, String endDateStr);
}
