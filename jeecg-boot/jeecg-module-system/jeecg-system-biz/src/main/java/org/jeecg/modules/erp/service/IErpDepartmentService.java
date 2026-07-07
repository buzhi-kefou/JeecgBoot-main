package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.entity.ErpDepartmentEntity;

import java.util.List;

/**
 * ERP部门
 */
public interface IErpDepartmentService extends IService<ErpDepartmentEntity> {

    /**
     * 按修改时间查询并同步部门
     */
    List<ErpDepartmentEntity> queryByDate(String beginDateStr, String endDateStr);
}
