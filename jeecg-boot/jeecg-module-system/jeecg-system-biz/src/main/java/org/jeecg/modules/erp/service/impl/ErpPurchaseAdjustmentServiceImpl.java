package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.MaterialQuery;
import org.jeecg.modules.erp.dto.MaterialSupplierPriceQuery;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentLineEntity;
import org.jeecg.modules.erp.entity.ErpSupplierEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialEntityMapper;
import org.jeecg.modules.erp.mapper.ErpOrgEntityMapper;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentEntityMapper;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentLineEntityMapper;
import org.jeecg.modules.erp.mapper.ErpSupplierEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.jeecg.modules.erp.vo.MaterialSupplierPriceVo;
import org.jeecg.modules.erp.vo.MaterialVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErpPurchaseAdjustmentServiceImpl extends ServiceImpl<ErpPurchaseAdjustmentEntityMapper, ErpPurchaseAdjustmentEntity> implements IErpPurchaseAdjustmentService {

    @Resource
    private ErpRequestService erpRequestService;


    @Resource
    private ErpPurchaseAdjustmentLineEntityMapper entryMapper;

    @Resource
    private ErpSupplierEntityMapper supplierMapper;

    @Resource
    private ErpMaterialEntityMapper materialMapper;

    @Resource
    private ErpOrgEntityMapper orgMapper;

    @Override
    public List<ErpPurchaseAdjustmentEntity> queryByDate(String beginDateStr, String endDateStr) {
        String filterString = "";
        if (StrUtil.isNotBlank(beginDateStr)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
            try {
                LocalDate.parse(beginDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            filterString += ("FModifyDate >='" + beginDateStr + " 00:00:00'");
        }

        if (StrUtil.isNotBlank(endDateStr)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
            try {
                LocalDate.parse(endDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            if (StrUtil.isNotBlank(filterString)) {
                filterString += " and ";
            }
            filterString += ("FModifyDate <='" + endDateStr + " 23:59:59'");
        }

        QueryDetailDto detailDto = new QueryDetailDto();
        detailDto.setFieldKeys(new ErpPurchaseAdjustmentEntity());
        detailDto.setFilterString(filterString);

        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("PUR_PAT");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpPurchaseAdjustmentEntity> request = erpRequestService.request(queryDto, ErpPurchaseAdjustmentEntity.class);

        List<ErpPurchaseAdjustmentEntity> insertList = new ArrayList<>();
        List<ErpPurchaseAdjustmentEntity> updateList = new ArrayList<>();
        ArrayList<ErpPurchaseAdjustmentLineEntity> insertLineList = new ArrayList<>();
        List<ErpPurchaseAdjustmentEntity> uniqueRequest = mergeDuplicateAdjustments(request);
        if (CollUtil.isNotEmpty(uniqueRequest)) {
            for (ErpPurchaseAdjustmentEntity entity : uniqueRequest) {
                ErpPurchaseAdjustmentEntity byId = baseMapper.selectById(entity.getId());
                if (CollUtil.isNotEmpty(entity.getEntries())) {
                    insertLineList.addAll(entity.getEntries());

                    if (byId != null) {
                        Set<Long> entrySet = entity.getEntries().stream().map(ErpPurchaseAdjustmentLineEntity::getEntryId).collect(Collectors.toSet());
                        LambdaUpdateWrapper<ErpPurchaseAdjustmentLineEntity> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(ErpPurchaseAdjustmentLineEntity::getPId, entity.getId());
                        updateWrapper.in(ErpPurchaseAdjustmentLineEntity::getEntryId, entrySet);
                        entryMapper.delete(updateWrapper);
                    }
                }
                if (byId == null) {
                    insertList.add(entity);
                } else {
                    updateList.add(entity);
                }
            }
        }

        if (CollUtil.isNotEmpty(insertList)) {
            this.saveBatch(insertList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }

        if (CollUtil.isNotEmpty(insertLineList)) {
            log.info("插入采购调整分录 {} 条", insertLineList.size());
            for (ErpPurchaseAdjustmentLineEntity line : insertLineList) {
                // 确保 decimalValue 字段有默认值，避免空值插入
                if (line.getDecimalValue() == null) {
                    line.setDecimalValue(BigDecimal.ZERO);
                }
                if (line.getPrice() == null) {
                    line.setPrice(BigDecimal.ZERO);
                }
                entryMapper.insert(line);
            }
        }

//        log.error("请求返回实体列表： {}", request);
        return request;
    }

    static List<ErpPurchaseAdjustmentEntity> mergeDuplicateAdjustments(List<ErpPurchaseAdjustmentEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return List.of();
        }

        Map<Long, ErpPurchaseAdjustmentEntity> adjustmentMap = new LinkedHashMap<>();
        List<ErpPurchaseAdjustmentEntity> entitiesWithoutId = new ArrayList<>();
        for (ErpPurchaseAdjustmentEntity entity : request) {
            if (entity == null) {
                continue;
            }
            Long id = entity.getId();
            if (id == null) {
                entitiesWithoutId.add(entity);
                continue;
            }
            ErpPurchaseAdjustmentEntity mergedEntity = adjustmentMap.get(id);
            if (mergedEntity == null) {
                adjustmentMap.put(id, entity);
                continue;
            }
            mergeEntries(mergedEntity, entity);
        }

        List<ErpPurchaseAdjustmentEntity> result = new ArrayList<>(adjustmentMap.values());
        result.addAll(entitiesWithoutId);
        return result;
    }

    private static void mergeEntries(ErpPurchaseAdjustmentEntity target, ErpPurchaseAdjustmentEntity source) {
        if (CollUtil.isEmpty(source.getEntries())) {
            return;
        }
        List<ErpPurchaseAdjustmentLineEntity> mergedEntries = new ArrayList<>();
        if (CollUtil.isNotEmpty(target.getEntries())) {
            mergedEntries.addAll(target.getEntries());
        }
        mergedEntries.addAll(source.getEntries());
        target.setEntries(mergedEntries);
    }

    @Override
    public Page<MaterialSupplierPriceVo> queryMaterialSupplierPrice(MaterialSupplierPriceQuery query) {
        log.info("查询物料供应商价格，物料编码：{}，年份：{}", query.getMaterialCode(), query.getYear());

        long pageNo = query.getPageNo() == null || query.getPageNo() < 1 ? 1 : query.getPageNo();
        long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        Page<MaterialSupplierPriceVo> resultPage = new Page<>(pageNo, pageSize);

        LocalDate yearStart = LocalDate.of(query.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(query.getYear() + 1, 1, 1);
        LocalDateTime approveEnd = yearEnd.atStartOfDay();

        LambdaQueryWrapper<ErpMaterialEntity> materialQueryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getMaterialCode())) {
            materialQueryWrapper.eq(ErpMaterialEntity::getNumber, query.getMaterialCode())
                    .orderByAsc(ErpMaterialEntity::getMaterialId);
        } else {
            materialQueryWrapper.orderByAsc(ErpMaterialEntity::getMaterialId);
        }
        if (StrUtil.isNotBlank(query.getUseOrgId())) {
            materialQueryWrapper.eq(ErpMaterialEntity::getUseOrgId, query.getUseOrgId());
        }
        Page<ErpMaterialEntity> materialPage = materialMapper.selectPage(new Page<>(pageNo, pageSize), materialQueryWrapper);
        resultPage.setTotal(materialPage.getTotal());
        List<ErpMaterialEntity> materialList = materialPage.getRecords();
        if (CollUtil.isEmpty(materialList)) {
            log.info("未查询到物料编码{}对应的物料", query.getMaterialCode());
            resultPage.setRecords(List.of());
            return resultPage;
        }

        Map<String, ErpMaterialEntity> materialMap = materialList.stream()
                .filter(material -> material != null && material.getMaterialId() != null)
                .collect(Collectors.toMap(
                        material -> String.valueOf(material.getMaterialId()),
                        material -> material,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        if (materialMap.isEmpty()) {
            resultPage.setRecords(List.of());
            return resultPage;
        }
        Map<String, String> useOrgNameMap = buildUseOrgNameMap(materialList);

        LambdaQueryWrapper<ErpPurchaseAdjustmentLineEntity> priceQueryWrapper = new LambdaQueryWrapper<>();
        priceQueryWrapper.in(ErpPurchaseAdjustmentLineEntity::getMaterialId, materialMap.keySet())
                .and(w -> w.and(
                                i -> i.gt(ErpPurchaseAdjustmentLineEntity::getExpiryDate, yearStart)
                                        .lt(ErpPurchaseAdjustmentLineEntity::getExpiryDate, yearEnd))
                        .or(i -> i.gt(ErpPurchaseAdjustmentLineEntity::getEffectiveDate, yearStart)
                                .lt(ErpPurchaseAdjustmentLineEntity::getEffectiveDate, yearEnd))
                        .or(i -> i.gt(ErpPurchaseAdjustmentLineEntity::getExpiryDate, yearEnd)
                                .lt(ErpPurchaseAdjustmentLineEntity::getEffectiveDate, yearStart)))

                .orderByAsc(ErpPurchaseAdjustmentLineEntity::getMaterialId)
                .orderByAsc(ErpPurchaseAdjustmentLineEntity::getSupplierId)
                .orderByAsc(ErpPurchaseAdjustmentLineEntity::getEffectiveDate);

        List<ErpPurchaseAdjustmentLineEntity> priceList = entryMapper.selectList(priceQueryWrapper);
        log.info("查询到{}条价格记录", priceList.size());

        if (CollUtil.isEmpty(priceList)) {
            resultPage.setRecords(buildMaterialOnlyRecords(materialList, useOrgNameMap));
            return resultPage;
        }

        Set<Long> headIds = priceList.stream()
                .map(ErpPurchaseAdjustmentLineEntity::getPId)
                .map(ErpPurchaseAdjustmentServiceImpl::parseLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, ErpPurchaseAdjustmentEntity> adjustmentMap = headIds.isEmpty()
                ? Map.of()
                : baseMapper.selectByIds(headIds).stream()
                .filter(adjustment -> adjustment != null && adjustment.getId() != null)
                .collect(Collectors.toMap(
                        adjustment -> String.valueOf(adjustment.getId()),
                        adjustment -> adjustment,
                        (left, right) -> left
                ));

        List<ErpPurchaseAdjustmentLineEntity> approvedPriceList = priceList.stream()
                .filter(price -> isApprovedBeforeYearEnd(price, adjustmentMap, approveEnd))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(approvedPriceList)) {
            resultPage.setRecords(buildMaterialOnlyRecords(materialList, useOrgNameMap));
            return resultPage;
        }

        Set<String> supplierIds = approvedPriceList.stream()
                .map(ErpPurchaseAdjustmentLineEntity::getSupplierId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpSupplierEntity> supplierMap = supplierIds.isEmpty()
                ? Map.of()
                : supplierMapper.selectByIds(supplierIds)
                .stream()
                .filter(supplier -> supplier != null && StrUtil.isNotBlank(supplier.getSupplierId()))
                .collect(Collectors.toMap(
                        ErpSupplierEntity::getSupplierId,
                        supplier -> supplier,
                        (left, right) -> left
                ));

        Map<String, List<ErpPurchaseAdjustmentLineEntity>> materialSupplierGroup = approvedPriceList.stream()
                .filter(price -> materialMap.containsKey(price.getMaterialId()))
                .filter(price -> StrUtil.isNotBlank(price.getSupplierId()))
                .collect(Collectors.groupingBy(
                        price -> buildMaterialSupplierKey(price.getMaterialId(), price.getSupplierId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MaterialSupplierPriceVo> result = new ArrayList<>();

        for (Map.Entry<String, List<ErpPurchaseAdjustmentLineEntity>> entry : materialSupplierGroup.entrySet()) {
            List<ErpPurchaseAdjustmentLineEntity> materialSupplierPrices = entry.getValue();
            ErpPurchaseAdjustmentLineEntity firstPrice = materialSupplierPrices.get(0);
            ErpMaterialEntity material = materialMap.get(firstPrice.getMaterialId());
            ErpSupplierEntity supplier = supplierMap.get(firstPrice.getSupplierId());
            if (supplier == null) {
                continue;
            }

            Map<Integer, BigDecimal> monthlyPrices = buildMonthlyPrices(materialSupplierPrices, adjustmentMap, query.getYear(), yearEnd);

            MaterialSupplierPriceVo vo = new MaterialSupplierPriceVo();
            vo.setMaterialId(firstPrice.getMaterialId());
            vo.setMaterialCode(material.getNumber());
            vo.setMaterialName(material.getName());
            vo.setSpecification(material.getSpecification());
            vo.setUseOrgId(resolveUseOrgName(material, useOrgNameMap));
            vo.setSupplierId(firstPrice.getSupplierId());
            vo.setSupplierCode(supplier.getNumber());
            vo.setSupplierName(supplier.getName());
            vo.setSupplierShortName(supplier.getShortName());
            vo.setMonthlyPrices(monthlyPrices);
            vo.setAvgPrice(calculateAveragePrice(monthlyPrices));
            vo.setRecordCount(materialSupplierPrices.size());

            result.add(vo);
        }

        Set<String> pricedMaterialIds = result.stream()
                .map(MaterialSupplierPriceVo::getMaterialId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        materialList.stream()
                .filter(material -> material != null && material.getMaterialId() != null)
                .filter(material -> !pricedMaterialIds.contains(String.valueOf(material.getMaterialId())))
                .map(material -> buildMaterialOnlyVo(material, useOrgNameMap))
                .forEach(result::add);

        log.info("处理完成，返回{}个物料供应商价格数据", result.size());
        resultPage.setRecords(result);
        return resultPage;
    }

    private Map<String, String> buildUseOrgNameMap(List<ErpMaterialEntity> materialList) {
        Set<Long> useOrgIds = materialList.stream()
                .map(ErpMaterialEntity::getUseOrgId)
                .map(ErpPurchaseAdjustmentServiceImpl::parseLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(useOrgIds)) {
            return Map.of();
        }
        return orgMapper.selectByIds(useOrgIds).stream()
                .filter(org -> org != null && org.getOrgId() != null && StrUtil.isNotBlank(org.getName()))
                .collect(Collectors.toMap(
                        org -> String.valueOf(org.getOrgId()),
                        ErpOrgEntity::getName,
                        (left, right) -> left
                ));
    }

    private static List<MaterialSupplierPriceVo> buildMaterialOnlyRecords(List<ErpMaterialEntity> materialList,
                                                                          Map<String, String> useOrgNameMap) {
        if (CollUtil.isEmpty(materialList)) {
            return List.of();
        }
        return materialList.stream()
                .filter(material -> material != null && material.getMaterialId() != null)
                .map(material -> buildMaterialOnlyVo(material, useOrgNameMap))
                .collect(Collectors.toList());
    }

    private static MaterialSupplierPriceVo buildMaterialOnlyVo(ErpMaterialEntity material,
                                                               Map<String, String> useOrgNameMap) {
        MaterialSupplierPriceVo vo = new MaterialSupplierPriceVo();
        vo.setMaterialId(String.valueOf(material.getMaterialId()));
        vo.setMaterialCode(material.getNumber());
        vo.setMaterialName(material.getName());
        vo.setSpecification(material.getSpecification());
        vo.setUseOrgId(resolveUseOrgName(material, useOrgNameMap));
        vo.setMonthlyPrices(initMonthlyPrices());
        vo.setAvgPrice(BigDecimal.ZERO);
        vo.setRecordCount(0);
        return vo;
    }

    private static String resolveUseOrgName(ErpMaterialEntity material, Map<String, String> useOrgNameMap) {
        if (material == null || StrUtil.isBlank(material.getUseOrgId())) {
            return null;
        }
        return useOrgNameMap.getOrDefault(material.getUseOrgId(), material.getUseOrgId());
    }

    private static Map<Integer, BigDecimal> initMonthlyPrices() {
        Map<Integer, BigDecimal> monthlyPrices = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyPrices.put(month, BigDecimal.ZERO);
        }
        return monthlyPrices;
    }

    private static boolean isEffectiveInMonth(ErpPurchaseAdjustmentLineEntity price, LocalDate monthStart, LocalDate monthEnd) {
        LocalDate effectiveDate = price.getEffectiveDate();
        LocalDate expiryDate = price.getExpiryDate();
        return effectiveDate != null
                && effectiveDate.isBefore(monthEnd)
                && (expiryDate == null || !expiryDate.isBefore(monthStart));
    }

    private static Map<Integer, BigDecimal> buildMonthlyPrices(List<ErpPurchaseAdjustmentLineEntity> materialSupplierPrices,
                                                               Map<String, ErpPurchaseAdjustmentEntity> adjustmentMap,
                                                               Integer year,
                                                               LocalDate yearEnd) {
        Map<Integer, BigDecimal> monthlyPrices = initMonthlyPrices();
        List<ErpPurchaseAdjustmentLineEntity> sortedPrices = materialSupplierPrices.stream()
                .filter(price -> price.getEffectiveDate() != null && price.getAfterPrice() != null)
                .sorted(Comparator.comparing(ErpPurchaseAdjustmentLineEntity::getEffectiveDate))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sortedPrices)) {
            return monthlyPrices;
        }

        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = month == 12 ? yearEnd : LocalDate.of(year, month + 1, 1);

            ErpPurchaseAdjustmentLineEntity monthPrice = sortedPrices.stream()
                    .filter(price -> isEffectiveInMonth(price, monthStart, monthEnd))
                    .filter(price -> isApprovedBeforeMonthEnd(price, adjustmentMap, monthEnd.atStartOfDay()))
                    .max(Comparator.comparing(ErpPurchaseAdjustmentLineEntity::getEffectiveDate)
                            .thenComparing(price -> getApproveDate(price, adjustmentMap), Comparator.nullsFirst(LocalDateTime::compareTo)))
                    .orElse(null);

            if (monthPrice != null) {
                monthlyPrices.put(month, monthPrice.getAfterPrice());
            }
        }
        return monthlyPrices;
    }

    private static boolean isApprovedBeforeYearEnd(ErpPurchaseAdjustmentLineEntity price,
                                                   Map<String, ErpPurchaseAdjustmentEntity> adjustmentMap,
                                                   LocalDateTime approveEnd) {
        ErpPurchaseAdjustmentEntity adjustment = adjustmentMap.get(price.getPId());
        return adjustment != null
                && adjustment.getApproveDate() != null
                && adjustment.getApproveDate().isBefore(approveEnd);
    }

    private static boolean isApprovedBeforeMonthEnd(ErpPurchaseAdjustmentLineEntity price,
                                                    Map<String, ErpPurchaseAdjustmentEntity> adjustmentMap,
                                                    LocalDateTime monthEnd) {
        LocalDateTime approveDate = getApproveDate(price, adjustmentMap);
        return approveDate != null && approveDate.isBefore(monthEnd);
    }

    private static LocalDateTime getApproveDate(ErpPurchaseAdjustmentLineEntity price,
                                                Map<String, ErpPurchaseAdjustmentEntity> adjustmentMap) {
        ErpPurchaseAdjustmentEntity adjustment = adjustmentMap.get(price.getPId());
        return adjustment == null ? null : adjustment.getApproveDate();
    }

    private static BigDecimal calculateAveragePrice(Map<Integer, BigDecimal> monthlyPrices) {
        BigDecimal total = BigDecimal.ZERO;
        int validCount = 0;
        for (BigDecimal price : monthlyPrices.values()) {
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(price);
                validCount++;
            }
        }
        return validCount > 0 ? total.divide(new BigDecimal(validCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private static Long parseLong(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String buildMaterialSupplierKey(String materialId, String supplierId) {
        return materialId + "::" + supplierId;
    }

    @Override
    public List<MaterialVo> getMaterialCodeList(MaterialQuery query) {
        log.info("查询物料列表，关键词：{}", query.getKeyword());

        // 构建查询条件 - 根据物料编码或物料名称查询
        LambdaQueryWrapper<ErpMaterialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErpMaterialEntity::getUseOrgId, "1");
        queryWrapper.and(wrapper ->
            wrapper.like(ErpMaterialEntity::getNumber, query.getKeyword())
                   .or()
                   .like(ErpMaterialEntity::getName, query.getKeyword())
        )
        .orderByAsc(ErpMaterialEntity::getNumber)
        .last("LIMIT 50");

        // 查询物料数据
        List<ErpMaterialEntity> materialList = materialMapper.selectList(queryWrapper);
        log.info("查询到{}条物料记录", materialList.size());

        if (CollUtil.isEmpty(materialList)) {
            return List.of();
        }

        // 转换为VO对象
        List<MaterialVo> result = materialList.stream()
                .map(material -> {
                    MaterialVo vo = new MaterialVo();
                    vo.setMaterialId(material.getMaterialId());
                    vo.setMaterialCode(material.getNumber());
                    vo.setMaterialName(material.getName());
                    vo.setSpecification(material.getSpecification());
                    vo.setMnemonicCode(material.getMnemonicCode());
                    vo.setDescription(material.getDescription());
                    return vo;
                })
                .collect(Collectors.toList());

        log.info("处理完成，返回{}个物料", result.size());
        return result;
    }

}
