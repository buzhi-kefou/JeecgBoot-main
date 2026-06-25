package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpMaterialEntity;
import org.jeecg.modules.erp.entity.ErpSupplierEntity;
import org.jeecg.modules.erp.mapper.ErpSupplierEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpSupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErpSupplierServiceImpl extends ServiceImpl<ErpSupplierEntityMapper, ErpSupplierEntity> implements IErpSupplierService {

    @Resource
    private ErpRequestService erpRequestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ErpSupplierEntity> queryByDate(String beginDateStr, String endDateStr) {
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
        detailDto.setFieldKeys(new ErpSupplierEntity());
        detailDto.setFilterString(filterString);

        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("BD_Supplier");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpSupplierEntity> request = erpRequestService.request(queryDto, ErpSupplierEntity.class);

        List<ErpSupplierEntity> insertList = new ArrayList<>();
        List<ErpSupplierEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            Set<String> supplierIds = request.stream()
                    .map(ErpSupplierEntity::getSupplierId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<String> existSupplierIds = CollUtil.isEmpty(supplierIds) ? Collections.emptySet() :
                    baseMapper.selectByIds(supplierIds).stream()
                            .map(ErpSupplierEntity::getSupplierId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            for (ErpSupplierEntity entity : request) {
                if (existSupplierIds.contains(entity.getSupplierId())) {
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

        log.error("请求返回实体列表： {}", request);
        return request;
    }
}
