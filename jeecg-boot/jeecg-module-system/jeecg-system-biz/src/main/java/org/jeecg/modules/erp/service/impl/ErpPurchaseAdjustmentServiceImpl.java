package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.*;
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
        if (query == null || query.getYear() == null) {
            throw new IllegalArgumentException("年份不能为空");
        }

        int pageNo = query.getPageNo() == null || query.getPageNo() <= 0 ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        LocalDate yearStart = LocalDate.of(query.getYear(), 1, 1);
        LocalDate nextYearStart = yearStart.plusYears(1);

        List<MaterialSupplierPriceLineRow> rows = baseMapper.selectMaterialSupplierPriceRows(
                yearStart,
                nextYearStart,
                query.getMaterialCode(),
                query.getSupplierId(),
                query.getUseOrgId()
        );
        if (CollUtil.isEmpty(rows)) {
            return new Page<>(pageNo, pageSize, 0);
        }

        Map<MaterialSupplierPriceGroupKey, List<MaterialSupplierPriceLineRow>> groupedRows = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> new MaterialSupplierPriceGroupKey(row.getMaterialId(), row.getSupplierId(), row.getUseOrgId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MaterialSupplierPriceVo> groupedRecords = groupedRows.values().stream()
                .map(groupRows -> buildMaterialSupplierPriceVo(groupRows, query.getYear()))
                .collect(Collectors.toList());

        long total = groupedRecords.size();
        long offset = (long) (pageNo - 1) * pageSize;
        int fromIndex = offset >= total ? groupedRecords.size() : (int) offset;
        int toIndex = Math.min(fromIndex + pageSize, groupedRecords.size());

        Page<MaterialSupplierPriceVo> page = new Page<>(pageNo, pageSize, total);
        page.setRecords(groupedRecords.subList(fromIndex, toIndex));
        return page;
    }

    private MaterialSupplierPriceVo buildMaterialSupplierPriceVo(List<MaterialSupplierPriceLineRow> rows, int year) {
        MaterialSupplierPriceLineRow first = rows.get(0);
        MaterialSupplierPriceVo vo = new MaterialSupplierPriceVo();
        vo.setMaterialId(first.getMaterialId());
        vo.setMaterialCode(first.getMaterialCode());
        vo.setMaterialName(first.getMaterialName());
        vo.setSpecification(first.getSpecification());
        vo.setUseOrgId(first.getUseOrgId());
        vo.setSupplierId(first.getSupplierId());
        vo.setSupplierCode(first.getSupplierCode());
        vo.setSupplierName(first.getSupplierName());
        vo.setSupplierShortName(first.getSupplierShortName());
        vo.setRecordCount(rows.size());

        Map<Integer, BigDecimal> monthlyPrices = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyPrices.put(month, resolveMonthPrice(rows, year, month));
        }
        vo.setMonthlyPrices(monthlyPrices);
        vo.setAvgPrice(calculateAveragePrice(monthlyPrices));
        vo.setChangeRate(calculateChangeRate(monthlyPrices));
        return vo;
    }

    private BigDecimal resolveMonthPrice(List<MaterialSupplierPriceLineRow> rows, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDateTime monthEndTime = monthEnd.atTime(23, 59, 59);
        MaterialSupplierPriceLineRow best = null;

        for (MaterialSupplierPriceLineRow row : rows) {
            if (!isValidForMonth(row, monthStart, monthEnd, monthEndTime)) {
                continue;
            }
            if (isBetterMonthCandidate(row, best)) {
                best = row;
            }
        }
        return best == null || best.getAfterTaxPrice() == null ? BigDecimal.ZERO : best.getAfterTaxPrice();
    }

    private boolean isValidForMonth(MaterialSupplierPriceLineRow row, LocalDate monthStart, LocalDate monthEnd, LocalDateTime monthEndTime) {
        return row.getEffectiveDate() != null
                && row.getExpiryDate() != null
                && row.getApproveDate() != null
                // 生效日期在月末之前
                && !row.getEffectiveDate().isAfter(monthEnd)
                // 失效日期在月初之后
                && !row.getExpiryDate().isBefore(monthStart)
                // 审批日期在月末之前
                && !row.getApproveDate().isAfter(monthEndTime);
    }

    private boolean isBetterMonthCandidate(MaterialSupplierPriceLineRow candidate, MaterialSupplierPriceLineRow current) {
        if (current == null) {
            return true;
        }
        int effectiveCompare = candidate.getEffectiveDate().compareTo(current.getEffectiveDate());
        if (effectiveCompare != 0) {
            return effectiveCompare > 0;
        }
        return candidate.getApproveDate().isAfter(current.getApproveDate());
    }

    private BigDecimal calculateAveragePrice(Map<Integer, BigDecimal> monthlyPrices) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal price : monthlyPrices.values()) {
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                sum = sum.add(price);
                count++;
            }
        }
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateChangeRate(Map<Integer, BigDecimal> monthlyPrices) {
        BigDecimal firstMonthPrice = monthlyPrices.get(1);
        BigDecimal lastMonthPrice = monthlyPrices.get(12);
        if (firstMonthPrice == null || firstMonthPrice.compareTo(BigDecimal.ZERO) == 0 || lastMonthPrice == null) {
            return BigDecimal.ZERO;
        }
        return lastMonthPrice.subtract(firstMonthPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(firstMonthPrice, 2, RoundingMode.HALF_UP);
    }

    private record MaterialSupplierPriceGroupKey(String materialId, String supplierId, String useOrgId) {
    }

    @Override
    public List<MaterialVo> getMaterialCodeList(MaterialQuery query) {
        log.info("查询物料列表，关键词：{}", query.getKeyword());

        // 构建查询条件 - 根据物料编码或物料名称查询
        LambdaQueryWrapper<ErpMaterialEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getUseOrgId())) {
            queryWrapper.eq(ErpMaterialEntity::getUseOrgId, query.getUseOrgId());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryWrapper.and(wrapper ->
                    wrapper.like(ErpMaterialEntity::getNumber, query.getKeyword())
                            .or()
                            .like(ErpMaterialEntity::getName, query.getKeyword())
            );
        }
        filterMaterialWithAdjustmentLine(queryWrapper);
        queryWrapper.orderByAsc(ErpMaterialEntity::getNumber)
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

    private static void filterMaterialWithAdjustmentLine(LambdaQueryWrapper<ErpMaterialEntity> queryWrapper) {
        queryWrapper.exists("select 1 from erp_purchase_adjustment_line line where line.material_id = erp_material.material_id");
    }

}
