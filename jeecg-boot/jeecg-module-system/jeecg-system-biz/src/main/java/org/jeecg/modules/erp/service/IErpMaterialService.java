package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;

import java.util.List;

public interface IErpMaterialService extends IService<ErpMaterialEntity> {



    List<ErpMaterialEntity> queryByDate(String beginDateStr,String endDateStr);
}