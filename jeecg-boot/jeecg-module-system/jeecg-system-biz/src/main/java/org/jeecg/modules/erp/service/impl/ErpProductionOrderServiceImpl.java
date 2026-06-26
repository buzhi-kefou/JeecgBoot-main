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
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
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
        transactionTemplate.execute(status -> {
            saveOrUpdateProductionOrders(request);
            return null;
        });

        return request;
    }

    private void saveOrUpdateProductionOrders(List<ErpProductionOrderEntity> request) {
        List<ErpProductionOrderEntity> insertList = new ArrayList<>();
        List<ErpProductionOrderEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            Set<String> ids = request.stream()
                    .map(ErpProductionOrderEntity::getFid)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<String> existIds = CollUtil.isEmpty(ids) ? Collections.emptySet() :
                    baseMapper.selectByIds(ids).stream()
                            .map(ErpProductionOrderEntity::getFid)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            for (ErpProductionOrderEntity entity : request) {
                if (existIds.contains(entity.getFid())) {
                    updateList.add(entity);
                } else {
                    insertList.add(entity);
                }
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
