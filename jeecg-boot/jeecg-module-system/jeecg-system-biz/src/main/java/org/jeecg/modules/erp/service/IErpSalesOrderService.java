package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpSalesOrderEntity;

import java.util.List;

public interface IErpSalesOrderService extends IService<ErpSalesOrderEntity> {

    List<ErpSalesOrderEntity> queryByDate(String beginDateStr, String endDateStr);
}
