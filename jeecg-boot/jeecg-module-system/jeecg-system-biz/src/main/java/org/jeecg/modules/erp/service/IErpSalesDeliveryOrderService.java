package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpSalesDeliveryOrderEntity;

import java.util.List;

public interface IErpSalesDeliveryOrderService extends IService<ErpSalesDeliveryOrderEntity> {

    List<ErpSalesDeliveryOrderEntity> queryByDate(String beginDateStr, String endDateStr);
}
