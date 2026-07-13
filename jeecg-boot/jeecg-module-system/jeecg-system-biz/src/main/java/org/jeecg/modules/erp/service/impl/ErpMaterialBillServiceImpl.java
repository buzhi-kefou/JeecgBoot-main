package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.dto.MaterialBillChildLineDto;
import org.jeecg.modules.erp.dto.MaterialQuery;
import org.jeecg.modules.erp.entity.ErpMaterialBillEntity;
import org.jeecg.modules.erp.entity.ErpMaterialBillLineEntity;
import org.jeecg.modules.erp.mapper.ErpMaterialBillEntityMapper;
import org.jeecg.modules.erp.mapper.ErpMaterialBillLineEntityMapper;
import org.jeecg.modules.erp.service.ErpRequestService;
import org.jeecg.modules.erp.service.IErpMaterialBillService;
import org.jeecg.modules.erp.vo.MaterialVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Slf4j
@Service
public class ErpMaterialBillServiceImpl extends ServiceImpl<ErpMaterialBillEntityMapper, ErpMaterialBillEntity> implements IErpMaterialBillService {

    private static final int UPSERT_BATCH_SIZE = 200;

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

    @Override
    public List<MaterialBillChildLineDto> queryChildLineTree(String materialCode, Long useOrgId) {
        List<MaterialBillChildLineDto> lines = baseMapper.selectAllChildLines(materialCode, useOrgId);
        if (CollUtil.isEmpty(lines)) {
            return List.of();
        }

        Map<Long, List<MaterialBillChildLineDto>> linesByParentBillId = lines.stream()
                .filter(line -> line.getParentBillId() != null)
                .collect(Collectors.groupingBy(MaterialBillChildLineDto::getParentBillId,
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, List<MaterialBillChildLineDto>> linesByLevelAndMaterialCode = lines.stream()
                .filter(line -> line.getLevelNo() != null && StrUtil.isNotBlank(line.getMaterialCodeChild()))
                .collect(Collectors.groupingBy(line -> line.getLevelNo() + ":" + line.getMaterialCodeChild(),
                        LinkedHashMap::new, Collectors.toList()));
        Map<MaterialBillChildLineDto, List<MaterialBillChildLineDto>> childrenByLine = new LinkedHashMap<>();
        Set<Long> childLineIds = new HashSet<>();
        for (MaterialBillChildLineDto line : lines) {
            if (line.getBomId() != null && line.getBomId() > 0) {
                List<MaterialBillChildLineDto> children = linesByParentBillId.getOrDefault(line.getBomId(), List.of());
                if (CollUtil.isNotEmpty(children)) {
                    childrenByLine.put(line, new ArrayList<>(children));
                    children.forEach(child -> childLineIds.add(child.getId()));
                }
            }
        }
        for (MaterialBillChildLineDto line : lines) {
            if (childLineIds.contains(line.getId()) || line.getLevelNo() == null
                    || StrUtil.isBlank(line.getParentMaterialCode())) {
                continue;
            }
            List<MaterialBillChildLineDto> parents = linesByLevelAndMaterialCode.getOrDefault(
                    (line.getLevelNo() - 1) + ":" + line.getParentMaterialCode(), List.of());
            for (MaterialBillChildLineDto parent : parents) {
                childrenByLine.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(line);
                childLineIds.add(line.getId());
            }
        }
        lines.forEach(line -> line.setChildren(childrenByLine.getOrDefault(line, List.of())));
        return lines.stream()
                .filter(line -> !childLineIds.contains(line.getId()))
                .toList();
    }

    @Override
    public List<MaterialVo> getMaterialBillMaterialCodeList(MaterialQuery query) {
        LambdaQueryWrapper<ErpMaterialBillEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getUseOrgId())) {
            try {
                queryWrapper.eq(ErpMaterialBillEntity::getUseOrgId, Long.valueOf(query.getUseOrgId()));
            } catch (NumberFormatException e) {
                return List.of();
            }
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like(ErpMaterialBillEntity::getMaterialCode, query.getKeyword())
                    .or()
                    .like(ErpMaterialBillEntity::getItemName, query.getKeyword()));
        }
        queryWrapper.orderByAsc(ErpMaterialBillEntity::getMaterialCode)
                .last("LIMIT 100");

        Map<String, MaterialVo> materialMap = new LinkedHashMap<>();
        for (ErpMaterialBillEntity bill : baseMapper.selectList(queryWrapper)) {
            if (StrUtil.isBlank(bill.getMaterialCode())) {
                continue;
            }
            materialMap.putIfAbsent(bill.getMaterialCode(), toMaterialVo(bill));
            if (materialMap.size() >= 50) {
                break;
            }
        }
        return List.copyOf(materialMap.values());
    }

    private static MaterialVo toMaterialVo(ErpMaterialBillEntity bill) {
        MaterialVo vo = new MaterialVo();
        vo.setMaterialCode(bill.getMaterialCode());
        vo.setMaterialName(bill.getItemName());
        vo.setSpecification(bill.getItemModel());
        vo.setDescription(bill.getDescription());
        return vo;
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
