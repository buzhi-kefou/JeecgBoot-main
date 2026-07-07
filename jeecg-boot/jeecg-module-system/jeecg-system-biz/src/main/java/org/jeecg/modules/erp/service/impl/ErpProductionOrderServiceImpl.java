package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.dto.ProductionOrderQuery;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.entity.ErpProductionOrderLineEntity;
import org.jeecg.modules.erp.mapper.ErpProductionOrderEntityMapper;
import org.jeecg.modules.erp.mapper.ErpProductionOrderLineEntityMapper;
import org.jeecg.modules.erp.exception.ChunkSyncFailureException;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
import org.jeecg.modules.erp.vo.ProductionOrderListVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErpProductionOrderServiceImpl extends ServiceImpl<ErpProductionOrderEntityMapper, ErpProductionOrderEntity>
        implements IErpProductionOrderService {

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ErpProductionOrderLineEntityMapper entryMapper;

    @Value("${erp.sync.batch-size:500}")
    private int batchSize;

    @Override
    public List<ErpProductionOrderEntity> queryByDate(String beginDateStr, String endDateStr) {
        String filterString = " FDocumentStatus = 'C'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        if (StrUtil.isNotBlank(beginDateStr)) {
            try {
                LocalDate.parse(beginDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            filterString += (" and FModifyDate >='" + beginDateStr + " 00:00:00'");
        }

        if (StrUtil.isNotBlank(endDateStr)) {
            try {
                LocalDate.parse(endDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
//            if (StrUtil.isNotBlank(filterString)) {
//                filterString += " and ";
//            }
            filterString += (" and FModifyDate <='" + endDateStr + " 23:59:59'");
        }

        QueryDetailDto detailDto = new QueryDetailDto();
        detailDto.setFieldKeys(new ErpProductionOrderEntity());
        detailDto.setFilterString(filterString);
        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("PRD_MO");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpProductionOrderEntity> request = erpRequestService.request(queryDto, ErpProductionOrderEntity.class);
        saveOrUpdateProductionOrders(request);

        return request;
    }

    @Override
    public Page<ProductionOrderListVo> queryProductionOrderPage(ProductionOrderQuery query) {
        ProductionOrderQuery condition = query == null ? new ProductionOrderQuery() : query;
        int pageNo = condition.getPageNo() == null || condition.getPageNo() <= 0 ? 1 : condition.getPageNo();
        int pageSize = condition.getPageSize() == null || condition.getPageSize() <= 0 ? 10 : condition.getPageSize();

        return baseMapper.customizeSelectPage(new Page<>(pageNo, pageSize), condition);
    }

    private void saveOrUpdateProductionOrders(List<ErpProductionOrderEntity> request) {
        List<ErpProductionOrderEntity> uniqueRequest = mergeDuplicateProductionOrders(request);
        if (CollUtil.isEmpty(uniqueRequest)) {
            return;
        }
        int total = uniqueRequest.size();
        int effectiveBatchSize = batchSize > 0 ? batchSize : 500;
        int totalChunks = (total + effectiveBatchSize - 1) / effectiveBatchSize;
        List<ErpProductionOrderEntity> failedEntities = new ArrayList<>();
        log.info("erp production order sync: start processing {} records, batch size = {}, chunks = {}",
                total, effectiveBatchSize, totalChunks);

        for (int i = 0; i < total; i += effectiveBatchSize) {
            int end = Math.min(i + effectiveBatchSize, total);
            List<ErpProductionOrderEntity> chunk = uniqueRequest.subList(i, end);
            int chunkIndex = i / effectiveBatchSize + 1;
            try {
                transactionTemplate.execute(status -> {
                    processChunkByUpsert(chunk);
                    return null;
                });
                log.info("erp production order sync: chunk {}/{} done (records {}-{})",
                        chunkIndex, totalChunks, i + 1, end);
            } catch (Exception e) {
                failedEntities.addAll(chunk);
                log.error("erp production order sync: chunk {}/{} failed (records {}-{}), skipping",
                        chunkIndex, totalChunks, i + 1, end, e);
            }
        }

        if (!failedEntities.isEmpty()) {
            throw new ChunkSyncFailureException(
                    String.format("erp production order sync: %d/%d chunks failed, re-schedule erpProductionOrderChunkRetryJob",
                            failedEntities.size(), total),
                    failedEntities);
        }
        log.info("erp production order sync: all {} chunks completed successfully", totalChunks);
    }

    static List<ErpProductionOrderEntity> mergeDuplicateProductionOrders(List<ErpProductionOrderEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return List.of();
        }

        Map<String, ErpProductionOrderEntity> productionOrderMap = new LinkedHashMap<>();
        List<ErpProductionOrderEntity> entitiesWithoutId = new ArrayList<>();
        for (ErpProductionOrderEntity entity : request) {
            if (entity == null) {
                continue;
            }
            String fid = entity.getFid();
            if (fid == null) {
                entitiesWithoutId.add(entity);
                continue;
            }
            ErpProductionOrderEntity mergedEntity = productionOrderMap.get(fid);
            if (mergedEntity == null) {
                productionOrderMap.put(fid, entity);
                continue;
            }
            mergeEntries(mergedEntity, entity);
        }

        List<ErpProductionOrderEntity> result = new ArrayList<>(productionOrderMap.values());
        result.addAll(entitiesWithoutId);
        return result;
    }

    private static void mergeEntries(ErpProductionOrderEntity target, ErpProductionOrderEntity source) {
        if (CollUtil.isEmpty(source.getEntries())) {
            return;
        }
        List<ErpProductionOrderLineEntity> mergedEntries = new ArrayList<>();
        if (CollUtil.isNotEmpty(target.getEntries())) {
            mergedEntries.addAll(target.getEntries());
        }
        mergedEntries.addAll(source.getEntries());
        target.setEntries(mergedEntries);
    }

    private void processChunkByUpsert(List<ErpProductionOrderEntity> chunk) {
        baseMapper.upsertBatch(chunk);
        List<ErpProductionOrderLineEntity> entries = collectEntries(chunk);
        if (CollUtil.isEmpty(entries)) {
            return;
        }
        List<String> entryIds = entries.stream()
                .map(ErpProductionOrderLineEntity::getFEntryId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(entryIds)) {
            entryMapper.deleteByIds(entryIds);
        }
        entryMapper.insert(entries);
//        for (ErpProductionOrderLineEntity entry : entries) {
//            entryMapper.insert(entry);
//        }
    }

    private List<ErpProductionOrderLineEntity> collectEntries(List<ErpProductionOrderEntity> chunk) {
        List<ErpProductionOrderLineEntity> entries = new ArrayList<>();
        for (ErpProductionOrderEntity entity : chunk) {
            if (entity == null || CollUtil.isEmpty(entity.getEntries())) {
                continue;
            }
            for (ErpProductionOrderLineEntity entry : entity.getEntries()) {
                if (entry == null) {
                    continue;
                }
                if (StrUtil.isBlank(entry.getPId())) {
                    entry.setPId(entity.getFid());
                }
                entries.add(entry);
            }
        }
        return entries;
    }

    private void processChunk(List<ErpProductionOrderEntity> chunk) {
        Set<String> ids = chunk.stream()
                .map(ErpProductionOrderEntity::getFid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> existIds = CollUtil.isEmpty(ids) ? Collections.emptySet() :
                baseMapper.selectByIds(ids).stream()
                        .map(ErpProductionOrderEntity::getFid)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        List<ErpProductionOrderEntity> insertList = new ArrayList<>();
        List<ErpProductionOrderEntity> updateList = new ArrayList<>();
        for (ErpProductionOrderEntity entity : chunk) {
            if (existIds.contains(entity.getFid())) {
                updateList.add(entity);
            } else {
                insertList.add(entity);
            }
        }

        if (CollUtil.isNotEmpty(insertList)) {
            this.saveBatch(insertList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
    }
}
