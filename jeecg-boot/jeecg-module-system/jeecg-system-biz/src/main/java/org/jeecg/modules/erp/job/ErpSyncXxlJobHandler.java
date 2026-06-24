package org.jeecg.modules.erp.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.service.IErpMaterialService;
import org.jeecg.modules.erp.service.IErpOrgService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.jeecg.modules.erp.service.IErpSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * ERP数据同步XXL-JOB入口。
 */
@Slf4j
@Component
public class ErpSyncXxlJobHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private final IErpMaterialService materialService;
    private final IErpSupplierService supplierService;
    private final IErpPurchaseAdjustmentService purchaseAdjustmentService;
    private final IErpOrgService orgService;
    private final LocalDate fixedCurrentDate;

    @Autowired
    public ErpSyncXxlJobHandler(IErpMaterialService materialService,
                                IErpSupplierService supplierService,
                                IErpPurchaseAdjustmentService purchaseAdjustmentService,
                                IErpOrgService orgService) {
        this(materialService, supplierService, purchaseAdjustmentService, orgService, null);
    }

    ErpSyncXxlJobHandler(IErpMaterialService materialService,
                         IErpSupplierService supplierService,
                         IErpPurchaseAdjustmentService purchaseAdjustmentService,
                         IErpOrgService orgService,
                         LocalDate fixedCurrentDate) {
        this.materialService = materialService;
        this.supplierService = supplierService;
        this.purchaseAdjustmentService = purchaseAdjustmentService;
        this.orgService = orgService;
        this.fixedCurrentDate = fixedCurrentDate;
    }

    @XxlJob("erpMaterialSyncJob")
    public ReturnT<String> erpMaterialSyncJob(String param) {
        return executeMonthlySync("物料", param, materialService::queryByDate);
    }

    @XxlJob("erpSupplierSyncJob")
    public ReturnT<String> erpSupplierSyncJob(String param) {
        return executeMonthlySync("供应商", param, supplierService::queryByDate);
    }

    @XxlJob("erpPurchaseAdjustmentSyncJob")
    public ReturnT<String> erpPurchaseAdjustmentSyncJob(String param) {
        return executeMonthlySync("采购调价", param, purchaseAdjustmentService::queryByDate);
    }

    @XxlJob("erpOrgSyncJob")
    public ReturnT<String> erpOrgSyncJob(String param) {
        return executeMonthlySync("组织", param, orgService::queryByDate);
    }

    private ReturnT<String> executeMonthlySync(String jobName, String param,
                                               BiFunction<String, String, ? extends List<?>> queryFunction) {
        List<DateRange> ranges;
        try {
            ranges = buildMonthlyRanges(extractBeginDate(param), currentDate());
        } catch (IllegalArgumentException e) {
            log.error("ERP{}同步参数错误：{}", jobName, e.getMessage());
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }

        int totalCount = 0;
        for (DateRange range : ranges) {
            List<?> result = queryFunction.apply(range.beginDate(), range.endDate());
            int count = result == null ? 0 : result.size();
            totalCount += count;
            log.info("ERP{}同步完成，日期范围：{} ~ {}，返回数量：{}", jobName, range.beginDate(), range.endDate(), count);
        }
        String message = "ERP" + jobName + "同步完成，执行月份数：" + ranges.size() + "，返回总数：" + totalCount;
        log.info(message);
        return new ReturnT<>(ReturnT.SUCCESS_CODE, message);
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
