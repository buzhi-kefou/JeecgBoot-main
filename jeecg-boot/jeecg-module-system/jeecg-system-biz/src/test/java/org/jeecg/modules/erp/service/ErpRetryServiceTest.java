package org.jeecg.modules.erp.service;

import org.jeecg.modules.erp.config.ErpConfigProperties;
import org.jeecg.modules.erp.entity.ErpDepartmentEntity;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpRetryServiceTest {

    private final IErpMaterialService materialService = mock(IErpMaterialService.class);
    private final IErpSupplierService supplierService = mock(IErpSupplierService.class);
    private final IErpPurchaseAdjustmentService purchaseAdjustmentService = mock(IErpPurchaseAdjustmentService.class);
    private final IErpOrgService orgService = mock(IErpOrgService.class);
    private final IErpDepartmentService departmentService = mock(IErpDepartmentService.class);
    private final IErpSalesOrderService salesOrderService = mock(IErpSalesOrderService.class);
    private final IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
    private final IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
    private final ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);
    private final IErpAuthService erpAuthService = mock(IErpAuthService.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final ErpConfigProperties erpConfigProperties = mock(ErpConfigProperties.class);

    @Test
    void retryFailedLogsSavesDepartmentRowsForDepartmentFormId() {
        SysInterfaceLog retryLog = new SysInterfaceLog();
        retryLog.setId("log-001");
        retryLog.setInterfaceName("BD_Department");
        retryLog.setRequestBody("""
                {"parameters":[{"fieldKeys":"FDEPTID,FName,FNumber"}]}
                """);
        when(interfaceLogService.findRetryableLogs(100)).thenReturn(List.of(retryLog));
        when(erpAuthService.login()).thenReturn("token-001");
        when(erpConfigProperties.getHeaderKey()).thenReturn("kdservice-sessionid");
        when(erpConfigProperties.getQueryUrl()).thenReturn("https://erp.example/query");
        when(restTemplate.postForEntity(eq("https://erp.example/query"), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        [["1001","制造部","D001"]]
                        """));

        ErpRetryService retryService = new ErpRetryService(
                materialService,
                supplierService,
                purchaseAdjustmentService,
                orgService,
                departmentService,
                salesOrderService,
                productionOrderService,
                salesDeliveryOrderService);
        ReflectionTestUtils.setField(retryService, "interfaceLogService", interfaceLogService);
        ReflectionTestUtils.setField(retryService, "erpAuthService", erpAuthService);
        ReflectionTestUtils.setField(retryService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(retryService, "erpConfigProperties", erpConfigProperties);

        int successCount = retryService.retryFailedLogs();

        assertEquals(1, successCount);
        ArgumentCaptor<Collection<ErpDepartmentEntity>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(departmentService).saveOrUpdateBatch(captor.capture());
        ErpDepartmentEntity department = captor.getValue().iterator().next();
        assertEquals("1001", department.getFDEPTID());
        assertEquals("制造部", department.getFName());
        assertEquals("D001", department.getFNumber());
        verify(interfaceLogService).markRetrying("log-001");
        verify(interfaceLogService).markRetrySuccess("log-001");
    }
}
