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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ErpMaterialServiceImpl extends ServiceImpl<ErpMaterialEntityMapper, ErpMaterialEntity> implements IErpMaterialService {

    @Resource
    private ErpRequestService erpRequestService;

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

        List<ErpMaterialEntity> insertList = new ArrayList<>();
        List<ErpMaterialEntity> updateList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request)) {
            for (ErpMaterialEntity entity : request) {
                ErpMaterialEntity byId = baseMapper.selectById(entity.getMaterialId());
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
        return request;
    }
}
