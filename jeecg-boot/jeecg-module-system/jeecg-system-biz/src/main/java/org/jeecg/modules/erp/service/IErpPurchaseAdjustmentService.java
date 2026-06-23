package org.jeecg.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.MaterialQuery;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceQuery;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;
import org.jeecg.modules.erp.vo.MaterialSupplierPriceVo;
import org.jeecg.modules.erp.vo.MaterialVo;

import java.util.List;

public interface IErpPurchaseAdjustmentService extends IService<ErpPurchaseAdjustmentEntity> {

    List<ErpPurchaseAdjustmentEntity> queryByDate(String beginDateStr,String endDateStr);

    /**
     * 查询物料供应商月度价格
     * @param query 查询条件
     * @return 价格列表
     */
    Page<MaterialSupplierPriceVo> queryMaterialSupplierPrice(MaterialSupplierPriceQuery query);

    /**
     * 根据物料名称或物料编码查询物料
     * @param query 查询条件
     * @return 物料列表
     */
    List<MaterialVo> getMaterialCodeList(MaterialQuery query);
}
