package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.dto.MaterialQuery;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;
import org.jeecg.modules.erp.vo.MaterialVo;

import java.util.List;

public interface IErpMaterialBillService extends IService<ErpMaterialBillEntity> {

    List<ErpMaterialBillEntity> queryByDate(String beginDateStr, String endDateStr);

    void saveOrUpdateMaterialBills(List<ErpMaterialBillEntity> materialBills);

    List<MaterialBillChildLineDto> queryChildLineTree(String materialCode, Long useOrgId);

    List<MaterialVo> getMaterialBillMaterialCodeList(MaterialQuery query);
}
