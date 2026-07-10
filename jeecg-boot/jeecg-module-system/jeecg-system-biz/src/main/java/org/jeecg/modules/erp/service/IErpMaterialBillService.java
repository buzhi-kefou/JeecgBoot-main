package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;

import java.util.List;

public interface IErpMaterialBillService extends IService<ErpMaterialBillEntity> {

    List<ErpMaterialBillEntity> queryByDate(String beginDateStr, String endDateStr);

    void saveOrUpdateMaterialBills(List<ErpMaterialBillEntity> materialBills);
}
