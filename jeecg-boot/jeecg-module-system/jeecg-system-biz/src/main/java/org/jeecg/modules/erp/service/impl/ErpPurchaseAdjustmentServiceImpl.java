package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentEntity;
import org.jeecg.modules.erp.entity.ErpPurchaseAdjustmentLineEntity;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentEntityMapper;
import org.jeecg.modules.erp.mapper.ErpPurchaseAdjustmentLineEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErpPurchaseAdjustmentServiceImpl extends ServiceImpl<ErpPurchaseAdjustmentEntityMapper, ErpPurchaseAdjustmentEntity> implements IErpPurchaseAdjustmentService {

    @Resource
    private ErpRequestService erpRequestService;


    @Resource
    private ErpPurchaseAdjustmentLineEntityMapper entryMapper;

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
            filterString += ("FModifyDate >='" + beginDateStr + "'");
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
            filterString += ("FModifyDate <='" + endDateStr + "'");
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

}
