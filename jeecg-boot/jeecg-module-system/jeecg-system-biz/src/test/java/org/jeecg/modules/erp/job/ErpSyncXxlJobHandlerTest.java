package org.jeecg.modules.erp.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.context.XxlJobContext;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpSyncXxlJobHandlerTest {

    private final IErpMaterialService materialService = mock(IErpMaterialService.class);
    private final IErpSupplierService supplierService = mock(IErpSupplierService.class);
    private final IErpPurchaseAdjustmentService purchaseAdjustmentService = mock(IErpPurchaseAdjustmentService.class);
    private final IErpOrgService orgService = mock(IErpOrgService.class);
    private final IErpSalesOrderService salesOrderService = mock(IErpSalesOrderService.class);
    private final IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
    private final IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
    private final ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);

    @AfterEach
    void clearXxlJobContext() {
        XxlJobContext.setXxlJobContext(null);
    }

    @Test
    void erpSalesOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        when(salesOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = newHandler(LocalDate.of(2026, 6, 26));

        handler.erpSalesOrderSyncJob();

        verify(salesOrderService).queryByDate("2026-06-26", "2026-06-30");
    }

    @Test
    void erpProductionOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        when(productionOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = newHandler(LocalDate.of(2026, 6, 26));

        handler.erpProductionOrderSyncJob();

        verify(productionOrderService).queryByDate("2026-06-26", "2026-06-30");
    }

    @Test
    void erpProductionOrderSyncJobSavesFailedChunkFidsAsRetryableInterfaceLog() {
        ErpProductionOrderEntity first = new ErpProductionOrderEntity();
        first.setFid("FID001");
        ErpProductionOrderEntity duplicate = new ErpProductionOrderEntity();
        duplicate.setFid("FID001");
        ErpProductionOrderEntity second = new ErpProductionOrderEntity();
        second.setFid("FID002");
        ChunkSyncFailureException exception = new ChunkSyncFailureException("chunk save failed",
                List.of(first, duplicate, second));
        when(productionOrderService.queryByDate("2026-06-26", "2026-06-30")).thenThrow(exception);
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));
        ErpSyncXxlJobHandler handler = newHandler(LocalDate.of(2026, 6, 26));

        handler.erpProductionOrderSyncJob();

        ArgumentCaptor<SysInterfaceLog> logCaptor = ArgumentCaptor.forClass(SysInterfaceLog.class);
        verify(interfaceLogService).save(logCaptor.capture());
        SysInterfaceLog logEntity = logCaptor.getValue();
        assertEquals("ERP_SYNC", logEntity.getBizType());
        assertEquals("ERP生产订单chunk重试", logEntity.getBizName());
        assertEquals("ErpSyncXxlJobHandler", logEntity.getSourceService());
        assertEquals("PRD_MO", logEntity.getInterfaceName());
        assertEquals("POST", logEntity.getRequestMethod());
        assertFalse(logEntity.getSuccess());
        assertEquals("PENDING", logEntity.getRetryStatus());
        assertEquals(0, logEntity.getRetryCount());
        assertNotNull(logEntity.getNextRetryTime());
        assertEquals(ChunkSyncFailureException.class.getName(), logEntity.getErrorType());
        assertEquals("chunk save failed", logEntity.getErrorMessage());

        JSONObject requestBody = JSONObject.parseObject(logEntity.getRequestBody());
        JSONArray parameters = requestBody.getJSONArray("parameters");
        JSONObject detail = parameters.getJSONObject(0);
        assertEquals("PRD_MO", detail.getString("formId"));
        assertEquals("FID in ('FID001','FID002')", detail.getString("filterString"));
        assertEquals("FModifyDate desc", detail.getString("orderString"));
        assertNotNull(detail.getString("fieldKeys"));
    }

    @Test
    void erpProductionOrderSyncJobContinuesAndSucceedsWhenChunkFails() {
        ErpProductionOrderEntity failed = new ErpProductionOrderEntity();
        failed.setFid("FID001");
        ChunkSyncFailureException exception = new ChunkSyncFailureException("chunk save failed", List.of(failed));
        when(productionOrderService.queryByDate("2026-05-01", "2026-05-31")).thenThrow(exception);
        when(productionOrderService.queryByDate("2026-06-01", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "2026-05-01", null, 0, 1));
        ErpSyncXxlJobHandler handler = newHandler(LocalDate.of(2026, 6, 26));

        handler.erpProductionOrderSyncJob();

        InOrder inOrder = inOrder(productionOrderService);
        inOrder.verify(productionOrderService).queryByDate("2026-05-01", "2026-05-31");
        inOrder.verify(productionOrderService).queryByDate("2026-06-01", "2026-06-30");
        verify(interfaceLogService).save(any(SysInterfaceLog.class));
        assertEquals(XxlJobContext.HANDLE_CODE_SUCCESS, XxlJobContext.getXxlJobContext().getHandleCode());
    }

    @Test
    void erpSalesDeliveryOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        when(salesDeliveryOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = newHandler(LocalDate.of(2026, 6, 26));

        handler.erpSalesDeliveryOrderSyncJob();

        verify(salesDeliveryOrderService).queryByDate("2026-06-26", "2026-06-30");
    }

    private ErpSyncXxlJobHandler newHandler(LocalDate fixedCurrentDate) {
        return new ErpSyncXxlJobHandler(
                materialService,
                supplierService,
                purchaseAdjustmentService,
                orgService,
                salesOrderService,
                productionOrderService,
                salesDeliveryOrderService,
                interfaceLogService,
                fixedCurrentDate);
    }
}
