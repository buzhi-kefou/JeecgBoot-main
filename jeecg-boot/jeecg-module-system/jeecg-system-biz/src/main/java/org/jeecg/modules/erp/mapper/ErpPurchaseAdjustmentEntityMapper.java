package org.jeecg.modules.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceLineRow;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;

import java.time.LocalDate;
import java.util.List;

public interface ErpPurchaseAdjustmentEntityMapper extends BaseMapper<ErpPurchaseAdjustmentEntity> {

    List<MaterialSupplierPriceLineRow> selectMaterialSupplierPriceRows(@Param("yearStart") LocalDate yearStart,
                                                                       @Param("nextYearStart") LocalDate nextYearStart,
                                                                       @Param("materialCode") String materialCode,
                                                                       @Param("supplierId") String supplierId,
                                                                       @Param("useOrgId") String useOrgId);

    Long countMaterialSupplierPriceGroups(@Param("yearStart") LocalDate yearStart,
                                          @Param("nextYearStart") LocalDate nextYearStart,
                                          @Param("materialCode") String materialCode,
                                          @Param("supplierId") String supplierId,
                                          @Param("useOrgId") String useOrgId);

    List<MaterialSupplierPriceLineRow> selectMaterialSupplierPriceGroupPage(@Param("yearStart") LocalDate yearStart,
                                                                            @Param("nextYearStart") LocalDate nextYearStart,
                                                                            @Param("materialCode") String materialCode,
                                                                            @Param("supplierId") String supplierId,
                                                                            @Param("useOrgId") String useOrgId,
                                                                            @Param("offset") long offset,
                                                                            @Param("pageSize") int pageSize);

    List<MaterialSupplierPriceLineRow> selectMaterialSupplierPriceRowsByGroups(@Param("yearStart") LocalDate yearStart,
                                                                               @Param("nextYearStart") LocalDate nextYearStart,
                                                                               @Param("materialCode") String materialCode,
                                                                               @Param("supplierId") String supplierId,
                                                                               @Param("useOrgId") String useOrgId,
                                                                               @Param("groups") List<MaterialSupplierPriceLineRow> groups);
}
