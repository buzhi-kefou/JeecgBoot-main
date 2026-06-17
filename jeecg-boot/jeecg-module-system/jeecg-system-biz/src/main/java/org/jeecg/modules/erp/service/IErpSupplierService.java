package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpSupplierEntity;

import java.util.List;

public interface IErpSupplierService extends IService<ErpSupplierEntity> {

    List<ErpSupplierEntity> queryByDate(String beginDateStr, String endDateStr);
}
