package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpMaterialService;
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
public class ErpMaterialServiceImpl extends ServiceImpl<ErpMaterialEntityMapper, ErpMaterialEntity> implements IErpMaterialService {

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<ErpMaterialEntity> queryByDate(String beginDateStr, String endDateStr) {
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
        detailDto.setFieldKeys(new ErpMaterialEntity());
        detailDto.setFilterString(filterString);

        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("BD_MATERIAL");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpMaterialEntity> request = erpRequestService.request(queryDto, ErpMaterialEntity.class);

        transactionTemplate.execute(status -> {
            saveOrUpdateMaterials(request);
            return null;
        });

        return request;
    }

    private void saveOrUpdateMaterials(List<ErpMaterialEntity> request) {
        List<ErpMaterialEntity> insertList = new ArrayList<>();
        List<ErpMaterialEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            Set<Long> materialIds = request.stream()
                    .map(ErpMaterialEntity::getMaterialId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Long> existMaterialIds = CollUtil.isEmpty(materialIds) ? Collections.emptySet() :
                    baseMapper.selectByIds(materialIds).stream()
                            .map(ErpMaterialEntity::getMaterialId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            for (ErpMaterialEntity entity : request) {
                if (existMaterialIds.contains(entity.getMaterialId())) {
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
