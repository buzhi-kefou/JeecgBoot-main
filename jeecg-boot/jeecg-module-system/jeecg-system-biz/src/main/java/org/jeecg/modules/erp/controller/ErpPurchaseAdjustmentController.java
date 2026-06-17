package org.jeecg.modules.erp.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;
import org.jeecg.modules.erp.entity.ErpSupplierEntity;
import org.jeecg.modules.erp.service.IErpAuthService;
import org.jeecg.modules.erp.service.IErpMaterialService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.jeecg.modules.erp.service.IErpSupplierService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/erp/purchase")
public class ErpPurchaseAdjustmentController {

    @Resource
    private IErpAuthService erpAuthService;

    @Resource
    private IErpPurchaseAdjustmentService erpPurchaseAdjustmentService;

    @Resource
    private IErpMaterialService erpMaterialService;

    @Resource
    private IErpSupplierService erpSupplierService;

    @PostMapping("/testAuth")
    public Result<String> testAuth() {
        return Result.ok(erpAuthService.login());
    }

    @PostMapping("/queryPurchase")
    public Result<Integer> queryPurchaseAdjustment(@RequestParam(value = "beginStr", required = false) String beginStr,
                                                   @RequestParam(value = "endStr", required = false) String endStr) {
        return Result.ok(erpPurchaseAdjustmentService.queryByDate(beginStr, endStr).size());
    }

    @PostMapping("/queryMaterial")
    public Result<Integer> queryMaterial(@RequestParam(value = "beginStr", required = false) String beginStr,
                                         @RequestParam(value = "endStr", required = false) String endStr) {
        return Result.ok(erpMaterialService.queryByDate(beginStr, endStr).size());
    }

    @PostMapping("/querySupplier")
    public Result<Integer> querySupplier(@RequestParam(value = "beginStr", required = false) String beginStr,
                                         @RequestParam(value = "endStr", required = false) String endStr) {
        return Result.ok(erpSupplierService.queryByDate(beginStr, endStr).size());
    }
}
