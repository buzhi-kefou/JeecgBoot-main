package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;

import java.util.List;

public interface IErpPurchaseAdjustmentService extends IService<ErpPurchaseAdjustmentEntity> {

    List<ErpPurchaseAdjustmentEntity> queryByDate(String beginDateStr,String endDateStr);
}
