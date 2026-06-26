package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.OrgQuery;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.erp.mapper.ErpOrgEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpOrgService;
import org.jeecg.modules.erp.vo.OrgVo;
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
public class ErpOrgServiceImpl extends ServiceImpl<ErpOrgEntityMapper, ErpOrgEntity> implements IErpOrgService {

    @Resource
    private ErpRequestService erpRequestService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<ErpOrgEntity> queryByDate(String beginDateStr, String endDateStr) {
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
        detailDto.setFieldKeys(new ErpOrgEntity());
        detailDto.setFilterString(filterString);

        detailDto.setOrderString("FModifyDate desc");
        detailDto.setFormId("ORG_Organizations");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));

        List<ErpOrgEntity> request = erpRequestService.request(queryDto, ErpOrgEntity.class);
        transactionTemplate.execute(status -> {
            saveOrUpdateOrgs(request);
            return null;
        });

        return request;
    }

    private void saveOrUpdateOrgs(List<ErpOrgEntity> request) {
        List<ErpOrgEntity> insertList = new ArrayList<>();
        List<ErpOrgEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            Set<Long> ids = request.stream()
                    .map(ErpOrgEntity::getOrgId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Long> existIds = CollUtil.isEmpty(ids) ? Collections.emptySet() :
                    baseMapper.selectByIds(ids).stream()
                            .map(ErpOrgEntity::getOrgId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            for (ErpOrgEntity entity : request) {
                if (existIds.contains(entity.getOrgId())) {
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

    @Override
    public Page<OrgVo> getOrgList(OrgQuery query) {
        long pageNo = query.getPageNo() == null || query.getPageNo() < 1 ? 1 : query.getPageNo();
        long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();

        LambdaQueryWrapper<ErpOrgEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like(ErpOrgEntity::getName, query.getKeyword())
                    .or()
                    .like(ErpOrgEntity::getNumber, query.getKeyword()));
        }
        queryWrapper.orderByAsc(ErpOrgEntity::getNumber);

        Page<ErpOrgEntity> orgPage = baseMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        Page<OrgVo> resultPage = new Page<>(pageNo, pageSize, orgPage.getTotal());
        resultPage.setRecords(orgPage.getRecords().stream()
                .map(org -> {
                    OrgVo vo = new OrgVo();
                    vo.setOrgId(org.getOrgId() == null ? null : String.valueOf(org.getOrgId()));
                    vo.setOrgCode(org.getNumber());
                    vo.setOrgName(org.getName());
                    return vo;
                })
                .toList());
        return resultPage;
    }
}
