package org.jeecg.modules.erp.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.config.ErpConfigProperties;
import org.jeecg.modules.erp.entity.*;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ERP接口失败请求重试服务。
 * <p>
 * 从 sys_interface_log 中查找失败的ERP接口请求，
 * 重新发送HTTP请求并将结果保存到对应业务表。
 */
@Slf4j
@Service
public class ErpRetryService {

    private static final int BATCH_SIZE = 100;

    private static final Map<String, Class<?>> FORM_ID_ENTITY_CLASS_MAP = Map.of(
            "BD_MATERIAL", ErpMaterialEntity.class,
            "BD_Supplier", ErpSupplierEntity.class,
            "PUR_PAT", ErpPurchaseAdjustmentEntity.class,
            "ORG_Organizations", ErpOrgEntity.class,
            "SAL_SaleOrder", ErpSalesOrderEntity.class,
            "PRD_MO", ErpProductionOrderEntity.class,
            "SAL_OUTSTOCK", ErpSalesDeliveryOrderEntity.class
    );

    private final Map<String, IService<?>> formIdServiceMap;

    @Resource
    private ISysInterfaceLogService interfaceLogService;

    @Resource
    private ErpConfigProperties erpConfigProperties;

    @Resource
    private IErpAuthService erpAuthService;

    @Resource(name = "erpRestTemplate")
    private RestTemplate restTemplate;

    public ErpRetryService(IErpMaterialService materialService,
                           IErpSupplierService supplierService,
                           IErpPurchaseAdjustmentService purchaseAdjustmentService,
                           IErpOrgService orgService,
                           IErpSalesOrderService salesOrderService,
                           IErpProductionOrderService productionOrderService,
                           IErpSalesDeliveryOrderService salesDeliveryOrderService) {
        formIdServiceMap = Map.of(
                "BD_MATERIAL", materialService,
                "BD_Supplier", supplierService,
                "PUR_PAT", purchaseAdjustmentService,
                "ORG_Organizations", orgService,
                "SAL_SaleOrder", salesOrderService,
                "PRD_MO", productionOrderService,
                "SAL_OUTSTOCK", salesDeliveryOrderService
        );
    }

    /**
     * 查找并重试所有待重试的失败日志。
     *
     * @return 重试成功数量
     */
    public int retryFailedLogs() {
        List<SysInterfaceLog> logs = interfaceLogService.findRetryableLogs(BATCH_SIZE);
        if (logs.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (SysInterfaceLog interfaceLog : logs) {
            if (retrySingleLog(interfaceLog)) {
                successCount++;
            }
        }
        return successCount;
    }

    private boolean retrySingleLog(SysInterfaceLog interfaceLog) {
        String logId = interfaceLog.getId();
        interfaceLogService.markRetrying(logId);
        try {
            doRetry(interfaceLog);
            interfaceLogService.markRetrySuccess(logId);
            log.info("ERP接口重试成功，logId：{}，formId：{}", logId, interfaceLog.getInterfaceName());
            return true;
        } catch (Exception e) {
            int retryCount = (interfaceLog.getRetryCount() == null ? 0 : interfaceLog.getRetryCount()) + 1;
            int maxAttempts = getRetryMaxAttempts();
            if (retryCount >= maxAttempts) {
                interfaceLogService.markRetryFailed(logId);
                log.error("ERP接口重试失败（已达最大重试次数{}），logId：{}，formId：{}",
                        maxAttempts, logId, interfaceLog.getInterfaceName(), e);
            } else {
                long backoffMs = getRetryBackoffMs(retryCount);
                interfaceLogService.updateRetryInfo(logId, retryCount, new Date(System.currentTimeMillis() + backoffMs));
                log.warn("ERP接口重试失败（第{}次/共{}次），logId：{}，formId：{}，下次重试时间：{}ms后",
                        retryCount, maxAttempts, logId, interfaceLog.getInterfaceName(), backoffMs / 1000, e);
            }
            return false;
        }
    }

    private void doRetry(SysInterfaceLog interfaceLog) {
        String formId = interfaceLog.getInterfaceName();
        String requestBody = interfaceLog.getRequestBody();

        if (StrUtil.isBlank(formId) || StrUtil.isBlank(requestBody)) {
            throw new IllegalStateException("日志缺少formId或requestBody，无法重试");
        }

        // 解析请求体获取fieldKeys
        JSONObject requestJson = JSON.parseObject(requestBody);
        JSONObject paramJson = requestJson.getJSONArray("parameters").getJSONObject(0);
        String fieldKeys = paramJson.getString("fieldKeys");

        // 重新发送ERP请求，确保获取最新数据（避免复用可能已损坏的历史响应体）
        String responseBody = resendErpRequest(requestBody);

        // 获取实体类型和服务
        Class<?> entityClass = FORM_ID_ENTITY_CLASS_MAP.get(formId);
        IService<?> service = formIdServiceMap.get(formId);
        if (entityClass == null || service == null) {
            throw new IllegalStateException("未找到formId对应的实体类型或服务：" + formId);
        }

        // 解析响应为实体并保存
        @SuppressWarnings({"rawtypes"})
        List entities = ErpRequestService.parseRows(responseBody, fieldKeys, entityClass);
        if (!entities.isEmpty()) {
            saveOrUpdateEntities(service, entities);
            log.info("ERP接口重试保存成功，formId：{}，数量：{}", formId, entities.size());
        }
    }

    private String resendErpRequest(String requestBody) {
        String token = erpAuthService.login();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, erpConfigProperties.getHeaderKey() + "=" + token);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                erpConfigProperties.getQueryUrl(),
                requestEntity,
                String.class);
        return response.getBody();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void saveOrUpdateEntities(IService service, List entities) {
        service.saveOrUpdateBatch(entities);
    }

    static long getRetryBackoffMs(int retryCount) {
        long[] backoffMs = {60_000L, 300_000L, 1_500_000L};
        if (retryCount < 0) {
            return backoffMs[0];
        }
        if (retryCount >= backoffMs.length) {
            return backoffMs[backoffMs.length - 1];
        }
        return backoffMs[retryCount];
    }

    static int getRetryMaxAttempts() {
        return 3;
    }
}
