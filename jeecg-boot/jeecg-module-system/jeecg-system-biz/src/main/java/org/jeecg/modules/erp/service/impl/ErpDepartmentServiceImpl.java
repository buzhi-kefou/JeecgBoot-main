package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpDepartmentEntity;
import org.jeecg.modules.erp.mapper.ErpDepartmentEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpDepartmentService;
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

/**
 * ERP部门
 */
@Slf4j
@Service
public class ErpDepartmentServiceImpl extends ServiceImpl<ErpDepartmentEntityMapper, ErpDepartmentEntity> implements IErpDepartmentService {

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<ErpDepartmentEntity> queryByDate(String beginDateStr, String endDateStr) {
        String filterString = "";
        if (StrUtil.isNotBlank(beginDateStr)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);
            try {
                LocalDate.parse(beginDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("日期格式错误，请使用yyyy-MM-dd格式");
                return null;
            }
            filterString += ("FModifyDate >='" + beginDateStr + " 00:00:00'");
        }

        if (StrUtil.isNotBlank(endDateStr)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);
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
        detailDto.setFieldKeys(new ErpDepartmentEntity());
        detailDto.setFilterString(filterString);
        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("BD_Department");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));
        List<ErpDepartmentEntity> request = erpRequestService.request(queryDto, ErpDepartmentEntity.class);
        transactionTemplate.execute(status -> {
            saveOrUpdateDepartments(request);
            return null;
        });
        return request;
    }

    private void saveOrUpdateDepartments(List<ErpDepartmentEntity> request) {
        List<ErpDepartmentEntity> insertList = new ArrayList<>();
        List<ErpDepartmentEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            Set<String> departmentIds = request.stream()
                    .map(ErpDepartmentEntity::getFDEPTID)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Set<String> existingIds = CollUtil.isEmpty(departmentIds) ? Collections.emptySet() :
                    baseMapper.selectByIds(departmentIds).stream()
                            .map(ErpDepartmentEntity::getFDEPTID)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

            for (ErpDepartmentEntity entity : request) {
                if (existingIds.contains(entity.getFDEPTID())) {
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
