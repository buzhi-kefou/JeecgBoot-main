package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.OrgQuery;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.erp.vo.OrgVo;

import java.util.List;

public interface IErpOrgService extends IService<ErpOrgEntity> {

    List<ErpOrgEntity> queryByDate(String beginDateStr, String endDateStr);

    Page<OrgVo> getOrgList(OrgQuery query);
}
