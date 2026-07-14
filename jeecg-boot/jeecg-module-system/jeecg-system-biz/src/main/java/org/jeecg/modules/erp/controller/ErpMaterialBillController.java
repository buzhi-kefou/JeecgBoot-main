package org.jeecg.modules.erp.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.dto.MaterialBillTreeQuery;
import org.jeecg.modules.erp.dto.MaterialQuery;
import org.jeecg.modules.erp.enums.MaterialAttribute;
import org.jeecg.modules.erp.service.IErpMaterialBillService;
import org.jeecg.modules.erp.vo.MaterialBillChildLineVo;
import org.jeecg.modules.erp.vo.MaterialBillChildLineResultVo;
import org.jeecg.modules.erp.vo.MaterialBillPurchaseUsageVo;
import org.jeecg.modules.erp.vo.MaterialVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ERP 物料清单层级查询。
 */
@RestController
@RequestMapping("/erp/material-bill")
public class ErpMaterialBillController {

    @Resource
    private IErpMaterialBillService erpMaterialBillService;

    @PostMapping("/child-lines")
    public Result<MaterialBillChildLineResultVo> queryChildLines(@RequestBody @Valid MaterialBillTreeQuery query) {
        List<MaterialBillChildLineDto> childLineTree = erpMaterialBillService
                .queryChildLineTree(query.getMaterialCode(), query.getUseOrgId());
        MaterialBillChildLineResultVo result = new MaterialBillChildLineResultVo();
        result.setChildLines(childLineTree
                .stream()
                .map(ErpMaterialBillController::toChildLineVo)
                .toList());
        result.setPurchaseUsages(toPurchaseUsages(childLineTree));
        return Result.ok(result);
    }

    @PostMapping("/material-options")
    public Result<List<MaterialVo>> getMaterialOptions(@RequestBody MaterialQuery query) {
        return Result.ok(erpMaterialBillService.getMaterialBillMaterialCodeList(query));
    }

    private static MaterialBillChildLineVo toChildLineVo(MaterialBillChildLineDto source) {
        MaterialBillChildLineVo target = new MaterialBillChildLineVo();
        target.setId(source.getId());
        target.setBillId(source.getBillId());
        target.setLevelNo(source.getLevelNo());
        target.setParentBillId(source.getParentBillId());
        target.setParentMaterialCode(source.getParentMaterialCode());
        target.setItemProperty(convertMaterialAttribute(source.getItemProperty()));
        target.setMaterialCodeChild(source.getMaterialCodeChild());
        target.setMaterialNameChild(source.getMaterialNameChild());
        target.setMaterialModelChild(source.getMaterialModelChild());
        target.setNumerator(source.getNumerator());
        target.setDenominator(source.getDenominator());
        target.setBomId(source.getBomId());
        target.setChildBomVersion(source.getChildBomVersion());
        target.setChildren(source.getChildren() == null ? List.of()
                : source.getChildren().stream().map(ErpMaterialBillController::toChildLineVo).toList());
        return target;
    }

    private static List<MaterialBillPurchaseUsageVo> toPurchaseUsages(List<MaterialBillChildLineDto> roots) {
        List<MaterialBillPurchaseUsageVo> purchaseUsages = new ArrayList<>();
        for (MaterialBillChildLineDto root : roots) {
            collectPurchaseUsage(root, BigDecimal.ONE, BigDecimal.ONE, purchaseUsages);
        }
        return mergePurchaseUsages(purchaseUsages);
    }

    private static void collectPurchaseUsage(MaterialBillChildLineDto line, BigDecimal parentNumerator,
                                             BigDecimal parentDenominator,
                                             List<MaterialBillPurchaseUsageVo> purchaseUsages) {
        BigDecimal numerator = valueOrOne(line.getNumerator()).multiply(parentNumerator);
        BigDecimal denominator = valueOrOne(line.getDenominator()).multiply(parentDenominator);
        if (line.getChildren() != null && !line.getChildren().isEmpty()) {
            for (MaterialBillChildLineDto child : line.getChildren()) {
                collectPurchaseUsage(child, numerator, denominator, purchaseUsages);
            }
            return;
        }
        if (!"1".equals(line.getItemProperty()) && !"3".equals(line.getItemProperty())) {
            return;
        }
        MaterialBillPurchaseUsageVo usage = new MaterialBillPurchaseUsageVo();
        usage.setId(line.getId());
        usage.setItemProperty(convertMaterialAttribute(line.getItemProperty()));
        usage.setMaterialCodeChild(line.getMaterialCodeChild());
        usage.setMaterialNameChild(line.getMaterialNameChild());
        usage.setMaterialModelChild(line.getMaterialModelChild());
        usage.setNumerator(numerator);
        usage.setDenominator(denominator);
        purchaseUsages.add(usage);
    }

    private static List<MaterialBillPurchaseUsageVo> mergePurchaseUsages(
            List<MaterialBillPurchaseUsageVo> purchaseUsages) {
        Map<String, MaterialBillPurchaseUsageVo> usagesByMaterialCode = new LinkedHashMap<>();
        for (MaterialBillPurchaseUsageVo usage : purchaseUsages) {
            MaterialBillPurchaseUsageVo mergedUsage = usagesByMaterialCode.get(usage.getMaterialCodeChild());
            if (mergedUsage == null) {
                usagesByMaterialCode.put(usage.getMaterialCodeChild(), usage);
                continue;
            }
            BigDecimal mergedNumerator = valueOrZero(mergedUsage.getNumerator());
            BigDecimal mergedDenominator = valueOrOne(mergedUsage.getDenominator());
            BigDecimal numerator = valueOrZero(usage.getNumerator());
            BigDecimal denominator = valueOrOne(usage.getDenominator());
            mergedUsage.setNumerator(mergedNumerator.multiply(denominator)
                    .add(numerator.multiply(mergedDenominator)));
            mergedUsage.setDenominator(mergedDenominator.multiply(denominator));
        }
        List<MaterialBillPurchaseUsageVo> mergedUsages = new ArrayList<>(usagesByMaterialCode.values());
        for (MaterialBillPurchaseUsageVo mergedUsage : mergedUsages) {
            mergedUsage.setUsageAmount(calculateUsageAmount(mergedUsage));
        }
        return mergedUsages;
    }

    private static BigDecimal calculateUsageAmount(MaterialBillPurchaseUsageVo usage) {
        BigDecimal denominator = valueOrOne(usage.getDenominator());
        if (BigDecimal.ZERO.compareTo(denominator) == 0) {
            return null;
        }
        return valueOrZero(usage.getNumerator())
                .divide(denominator, 6, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    private static BigDecimal valueOrOne(BigDecimal value) {
        return value == null ? BigDecimal.ONE : value;
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String convertMaterialAttribute(String itemProperty) {
        return MaterialAttribute.fromValue(Integer.valueOf(itemProperty)).getLabel();
    }
}
