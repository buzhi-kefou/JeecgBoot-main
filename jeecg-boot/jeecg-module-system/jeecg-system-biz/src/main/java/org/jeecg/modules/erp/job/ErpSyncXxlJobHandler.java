package org.jeecg.modules.erp.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.exception.ChunkSyncFailureException;
import org.jeecg.modules.erp.service.IErpMaterialService;
import org.jeecg.modules.erp.service.IErpOrgService;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.jeecg.modules.erp.service.IErpSalesDeliveryOrderService;
import org.jeecg.modules.erp.service.IErpSalesOrderService;
import org.jeecg.modules.erp.service.IErpSupplierService;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * ERP数据同步XXL-JOB入口。
 */
@Slf4j
@Component
public class ErpSyncXxlJobHandler {

    // todo 目前没有执行记录表，无法实现完整的增量同步

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private final IErpMaterialService materialService;
    private final IErpSupplierService supplierService;
    private final IErpPurchaseAdjustmentService purchaseAdjustmentService;
    private final IErpOrgService orgService;
    private final IErpSalesOrderService salesOrderService;
    private final IErpProductionOrderService productionOrderService;
    private final IErpSalesDeliveryOrderService salesDeliveryOrderService;
    private final ISysInterfaceLogService interfaceLogService;
    private final LocalDate fixedCurrentDate;

    @Autowired
    public ErpSyncXxlJobHandler(IErpMaterialService materialService,
                                IErpSupplierService supplierService,
                                IErpPurchaseAdjustmentService purchaseAdjustmentService,
                                IErpOrgService orgService,
                                IErpSalesOrderService salesOrderService,
                                IErpProductionOrderService productionOrderService,
                                IErpSalesDeliveryOrderService salesDeliveryOrderService,
                                ISysInterfaceLogService interfaceLogService) {
        this(materialService, supplierService, purchaseAdjustmentService, orgService, salesOrderService,
                productionOrderService, salesDeliveryOrderService, interfaceLogService, null);
    }

    ErpSyncXxlJobHandler(IErpMaterialService materialService,
                         IErpSupplierService supplierService,
                         IErpPurchaseAdjustmentService purchaseAdjustmentService,
                         IErpOrgService orgService,
                         IErpSalesOrderService salesOrderService,
                         IErpProductionOrderService productionOrderService,
                         IErpSalesDeliveryOrderService salesDeliveryOrderService,
                         ISysInterfaceLogService interfaceLogService,
                         LocalDate fixedCurrentDate) {
        this.materialService = materialService;
        this.supplierService = supplierService;
        this.purchaseAdjustmentService = purchaseAdjustmentService;
        this.orgService = orgService;
        this.salesOrderService = salesOrderService;
        this.productionOrderService = productionOrderService;
        this.salesDeliveryOrderService = salesDeliveryOrderService;
        this.interfaceLogService = interfaceLogService;
        this.fixedCurrentDate = fixedCurrentDate;
    }

    @XxlJob("erpMaterialSyncJob")
    public void erpMaterialSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("物料", param, materialService::queryByDate);
    }

    @XxlJob("erpSupplierSyncJob")
    public void erpSupplierSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("供应商", param, supplierService::queryByDate);
    }

    @XxlJob("erpPurchaseAdjustmentSyncJob")
    public void erpPurchaseAdjustmentSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("采购调价", param, purchaseAdjustmentService::queryByDate);
    }

    @XxlJob("erpOrgSyncJob")
    public void erpOrgSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("组织", param, orgService::queryByDate);
    }

    @XxlJob("erpSalesOrderSyncJob")
    public void erpSalesOrderSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("销售订单", param, salesOrderService::queryByDate);
    }

    @XxlJob("erpProductionOrderSyncJob")
    public void erpProductionOrderSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        String finalParam = param;
        List<DateRange> ranges;
        try {
            ranges = buildMonthlyRanges(extractBeginDate(finalParam), currentDate());
        } catch (IllegalArgumentException e) {
            String message = "ERP生产订单同步参数错误：" + e.getMessage();
            log.error(message);
            XxlJobHelper.log(message);
            XxlJobHelper.handleFail(message);
            return;
        }
        int totalCount = 0;
        int failedRetryRecordCount = 0;
        int failedRangeCount = 0;
        for (DateRange range : ranges) {
            try {
                List<?> result = productionOrderService.queryByDate(range.beginDate(), range.endDate());
                int count = result == null ? 0 : result.size();
                totalCount += count;
                String rangeMessage = "ERP生产订单同步完成，日期范围：" + range.beginDate() + " ~ " + range.endDate()
                        + "，返回数量：" + count;
                log.info(rangeMessage);
                XxlJobHelper.log(rangeMessage);
            } catch (ChunkSyncFailureException e) {
                List<String> failedFids = e.getFailedEntities().stream()
                        .map(ErpProductionOrderEntity::getFid)
                        .filter(fid -> fid != null && !fid.isBlank())
                        .distinct()
                        .toList();
                saveProductionOrderChunkRetryLog(failedFids, e);
                failedRangeCount++;
                failedRetryRecordCount += failedFids.size();
                String message = "ERP生产订单同步部分chunk失败，日期范围：" + range.beginDate() + " ~ " + range.endDate()
                        + "，失败记录数：" + failedFids.size()
                        + "，已写入接口日志，等待 erpInterfaceRetryJob 自动重试，继续执行后续日期范围";
                log.error(message);
                XxlJobHelper.log(message);
            } catch (RuntimeException e) {
                String message = "ERP生产订单同步执行异常：日期范围：" + range.beginDate() + " ~ " + range.endDate()
                        + "，异常信息：" + e.getMessage();
                log.error(message, e);
                XxlJobHelper.log(e);
                XxlJobHelper.handleFail(message);
                throw e;
            }
        }
        String message = "ERP生产订单同步完成，执行月份数：" + ranges.size() + "，返回总数：" + totalCount;
        if (failedRetryRecordCount > 0) {
            message += "，chunk失败月份数：" + failedRangeCount + "，失败记录数：" + failedRetryRecordCount
                    + "，已写入接口日志等待重试";
        }
        log.info(message);
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    private void saveProductionOrderChunkRetryLog(List<String> failedFids, ChunkSyncFailureException e) {
        if (failedFids.isEmpty()) {
            return;
        }
        QueryDto queryDto = buildProductionOrderChunkRetryQuery(failedFids);
        Date now = new Date();
        SysInterfaceLog logEntity = new SysInterfaceLog();
        logEntity.setId(IdWorker.getIdStr());
        logEntity.setTraceId(IdWorker.getIdStr());
        logEntity.setBizType("ERP_SYNC");
        logEntity.setBizName("ERP生产订单chunk重试");
        logEntity.setSourceService("ErpSyncXxlJobHandler");
        logEntity.setInterfaceName("PRD_MO");
        logEntity.setRequestMethod("POST");
        logEntity.setRequestBody(JSON.toJSONString(queryDto));
        logEntity.setSuccess(false);
        logEntity.setErrorType(e.getClass().getName());
        logEntity.setErrorMessage(e.getMessage());
        logEntity.setStartTime(now);
        logEntity.setEndTime(now);
        logEntity.setCreateTime(now);
        logEntity.setRetryStatus("PENDING");
        logEntity.setRetryCount(0);
        logEntity.setNextRetryTime(now);
        interfaceLogService.save(logEntity);
    }

    private QueryDto buildProductionOrderChunkRetryQuery(List<String> failedFids) {
        QueryDetailDto detailDto = new QueryDetailDto();
        detailDto.setFormId("PRD_MO");
        detailDto.setFieldKeys(new ErpProductionOrderEntity());
        detailDto.setFilterString("FID in (" + failedFids.stream()
                .map(this::quoteErpFilterValue)
                .collect(Collectors.joining(",")) + ")");
        detailDto.setOrderString("FModifyDate desc");

        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));
        return queryDto;
    }

    private String quoteErpFilterValue(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    @XxlJob("erpSalesDeliveryOrderSyncJob")
    public void erpSalesDeliveryOrderSyncJob() {
        String param = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(param)) {
            param = currentDate().format(DATE_FORMATTER);
        }
        executeMonthlySync("销售出库单", param, salesDeliveryOrderService::queryByDate);
    }

    private void executeMonthlySync(String jobName, String param,
                                    BiFunction<String, String, ? extends List<?>> queryFunction) {
        List<DateRange> ranges;
        try {
            ranges = buildMonthlyRanges(extractBeginDate(param), currentDate());
        } catch (IllegalArgumentException e) {
            String message = "ERP" + jobName + "同步参数错误：" + e.getMessage();
            log.error(message);
            XxlJobHelper.log(message);
            XxlJobHelper.handleFail(message);
            return;
        }

        int totalCount = 0;
        try {
            for (DateRange range : ranges) {
                List<?> result = queryFunction.apply(range.beginDate(), range.endDate());
                int count = result == null ? 0 : result.size();
                totalCount += count;
                String rangeMessage = "ERP" + jobName + "同步完成，日期范围：" + range.beginDate() + " ~ " + range.endDate()
                        + "，返回数量：" + count;
                log.info(rangeMessage);
                XxlJobHelper.log(rangeMessage);
            }
        } catch (RuntimeException e) {
            String message = "ERP" + jobName + "同步执行异常：" + e.getMessage();
            log.error(message, e);
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(message);
            throw e;
        }
        String message = "ERP" + jobName + "同步完成，执行月份数：" + ranges.size() + "，返回总数：" + totalCount;
        log.info(message);
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    static List<DateRange> buildMonthlyRanges(String beginDateStr, LocalDate currentDate) {
        if (StrUtil.isBlank(beginDateStr)) {
            throw new IllegalArgumentException("请指定开始日期，格式：yyyy-MM-dd");
        }
        LocalDate beginDate;
        try {
            beginDate = LocalDate.parse(beginDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("开始日期格式错误，请使用yyyy-MM-dd格式");
        }
        if (beginDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("开始日期不能晚于当前日期");
        }

        List<DateRange> ranges = new ArrayList<>();
        LocalDate rangeBegin = beginDate;
        YearMonth currentMonth = YearMonth.from(currentDate);
        while (!YearMonth.from(rangeBegin).isAfter(currentMonth)) {
            LocalDate rangeEnd = YearMonth.from(rangeBegin).atEndOfMonth();
            ranges.add(new DateRange(rangeBegin.format(DATE_FORMATTER), rangeEnd.format(DATE_FORMATTER)));
            rangeBegin = rangeEnd.plusDays(1);
        }
        return ranges;
    }

    private static String extractBeginDate(String param) {
        if (StrUtil.isBlank(param)) {
            return null;
        }
        String trimmedParam = param.trim();
        if (!trimmedParam.startsWith("{")) {
            return trimmedParam;
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(trimmedParam);
        } catch (JSONException e) {
            throw new IllegalArgumentException("任务参数JSON格式错误");
        }
        String beginDate = jsonObject.getString("beginStr");
        if (StrUtil.isBlank(beginDate)) {
            beginDate = jsonObject.getString("beginDate");
        }
        if (StrUtil.isBlank(beginDate)) {
            beginDate = jsonObject.getString("startDate");
        }
        return beginDate;
    }

    private LocalDate currentDate() {
        return fixedCurrentDate == null ? LocalDate.now() : fixedCurrentDate;
    }

    record DateRange(String beginDate, String endDate) {
    }
}
