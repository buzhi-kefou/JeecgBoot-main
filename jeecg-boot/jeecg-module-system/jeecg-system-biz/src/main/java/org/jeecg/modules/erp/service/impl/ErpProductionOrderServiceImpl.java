package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.mapper.ErpProductionOrderEntityMapper;
import org.jeecg.modules.erp.exception.ChunkSyncFailureException;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Value("${erp.sync.batch-size:500}")
    private int batchSize;

    @Override
    public List<ErpProductionOrderEntity> queryByDate(String beginDateStr, String endDateStr) {
        String filterString = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        if (StrUtil.isNotBlank(beginDateStr)) {
            try {
                LocalDate.parse(beginDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            filterString += ("FModifyDate >='" + beginDateStr + " 00:00:00'");
        }

        if (StrUtil.isNotBlank(endDateStr)) {
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

    private void saveOrUpdateProductionOrders(List<ErpProductionOrderEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return;
        }
        int total = request.size();
        int effectiveBatchSize = batchSize > 0 ? batchSize : 500;
        int totalChunks = (total + effectiveBatchSize - 1) / effectiveBatchSize;
        List<ErpProductionOrderEntity> failedEntities = new ArrayList<>();
        log.info("erp production order sync: start processing {} records, batch size = {}, chunks = {}",
                total, effectiveBatchSize, totalChunks);

        for (int i = 0; i < total; i += effectiveBatchSize) {
            int end = Math.min(i + effectiveBatchSize, total);
            List<ErpProductionOrderEntity> chunk = request.subList(i, end);
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

    private void processChunkByUpsert(List<ErpProductionOrderEntity> chunk) {
        baseMapper.upsertBatch(chunk);
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
