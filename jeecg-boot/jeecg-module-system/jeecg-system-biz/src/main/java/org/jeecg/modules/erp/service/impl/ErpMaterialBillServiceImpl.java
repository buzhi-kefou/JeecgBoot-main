package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;
import org.jeecg.modules.erp.entity.ErpMaterialBillLineEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialBillEntityMapper;
import org.jeecg.modules.erp.mapper.ErpMaterialBillLineEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpMaterialBillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class ErpMaterialBillServiceImpl extends ServiceImpl<ErpMaterialBillEntityMapper, ErpMaterialBillEntity> implements IErpMaterialBillService {

    private static final int UPSERT_BATCH_SIZE = 500;

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ErpMaterialBillLineEntityMapper materialBillLineEntityMapper;

    @Override
    public List<ErpMaterialBillEntity> queryByDate(String beginDateStr, String endDateStr) {
        String filterString = "";
        if (StrUtil.isNotBlank(beginDateStr)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
            try {
                LocalDate.parse(beginDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            filterString += "FModifyDate >='" + beginDateStr + " 00:00:00'";
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
            filterString += "FModifyDate <='" + endDateStr + " 23:59:59'";
        }

        QueryDetailDto detailDto = new QueryDetailDto();
        detailDto.setFieldKeys(new ErpMaterialBillEntity());
        detailDto.setFilterString(filterString);
        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("ENG_BOM");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpMaterialBillEntity> request = erpRequestService.request(queryDto, ErpMaterialBillEntity.class);
        transactionTemplate.execute(status -> {
            saveOrUpdateMaterialBills(request);
            return null;
        });
        return request;
    }

    @Override
    public void saveOrUpdateMaterialBills(List<ErpMaterialBillEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return;
        }

        Map<Long, ErpMaterialBillEntity> materialBillMap = new LinkedHashMap<>();
        Map<Long, ErpMaterialBillLineEntity> lineMap = new LinkedHashMap<>();
        for (ErpMaterialBillEntity entity : request) {
            Long billId = entity.getId();
            if (billId != null) {
                materialBillMap.put(billId, entity);
            }
            if (CollUtil.isEmpty(entity.getEntries())) {
                continue;
            }
            for (ErpMaterialBillLineEntity line : entity.getEntries()) {
                if (line == null || line.getId() == null) {
                    continue;
                }
                line.setBillId(billId);
                lineMap.put(line.getId(), line);
            }
        }

        executeInChunks(List.copyOf(materialBillMap.values()), baseMapper::upsertBatch);
        executeInChunks(List.copyOf(lineMap.values()), materialBillLineEntityMapper::upsertBatch);
    }

    private static <T> void executeInChunks(List<T> list, Consumer<List<T>> consumer) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (int fromIndex = 0; fromIndex < list.size(); fromIndex += UPSERT_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + UPSERT_BATCH_SIZE, list.size());
            consumer.accept(list.subList(fromIndex, toIndex));
        }
    }
}
