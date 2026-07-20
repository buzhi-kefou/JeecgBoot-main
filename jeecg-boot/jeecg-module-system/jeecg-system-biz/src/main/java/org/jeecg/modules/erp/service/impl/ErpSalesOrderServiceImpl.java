package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpSalesOrderEntity;
import org.jeecg.modules.erp.entity.ErpSalesOrderFinanceEntity;
import org.jeecg.modules.erp.mapper.ErpSalesOrderEntityMapper;
import org.jeecg.modules.erp.mapper.ErpSalesOrderFinanceEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpSalesOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ErpSalesOrderServiceImpl extends ServiceImpl<ErpSalesOrderEntityMapper, ErpSalesOrderEntity>
        implements IErpSalesOrderService {

    private static final int UPSERT_BATCH_SIZE = 200;

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ErpSalesOrderFinanceEntityMapper financeMapper;

    @Override
    public List<ErpSalesOrderEntity> queryByDate(String beginDateStr, String endDateStr) {
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
        detailDto.setFieldKeys(new ErpSalesOrderEntity());
        detailDto.setFilterString(filterString);
        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("SAL_SaleOrder");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpSalesOrderEntity> request = erpRequestService.request(queryDto, ErpSalesOrderEntity.class);
        transactionTemplate.execute(status -> {
            saveOrUpdateSalesOrders(request);
            return null;
        });

        return request;
    }

    private void saveOrUpdateSalesOrders(List<ErpSalesOrderEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return;
        }
        for (int fromIndex = 0; fromIndex < request.size(); fromIndex += UPSERT_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + UPSERT_BATCH_SIZE, request.size());
            baseMapper.upsertBatch(request.subList(fromIndex, toIndex));
        }
        saveOrUpdateFinances(request);
    }

    private void saveOrUpdateFinances(List<ErpSalesOrderEntity> request) {
        if (CollUtil.isEmpty(request)) {
            return;
        }

        Map<Long, ErpSalesOrderFinanceEntity> financeMap = new LinkedHashMap<>();
        for (ErpSalesOrderEntity order : request) {
            if (order == null || order.getFid() == null || order.getFinanceEntity() == null
                    || order.getFinanceEntity().getEntryId() == null) {
                continue;
            }
            ErpSalesOrderFinanceEntity finance = order.getFinanceEntity();
            financeMap.put(finance.getEntryId(), finance);
            finance.setPid(order.getFid());
        }
        if (financeMap.isEmpty()) {
            return;
        }

        List<ErpSalesOrderFinanceEntity> finances = List.copyOf(financeMap.values());
        for (int fromIndex = 0; fromIndex < finances.size(); fromIndex += UPSERT_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + UPSERT_BATCH_SIZE, finances.size());
            financeMapper.upsertBatch(finances.subList(fromIndex, toIndex));
        }
    }
}
