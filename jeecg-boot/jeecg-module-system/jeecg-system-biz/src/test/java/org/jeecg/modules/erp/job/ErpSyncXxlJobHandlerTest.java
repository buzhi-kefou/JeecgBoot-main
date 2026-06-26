package org.jeecg.modules.erp.job;

import com.xxl.job.core.context.XxlJobContext;
import org.jeecg.modules.erp.service.IErpMaterialService;
import org.jeecg.modules.erp.service.IErpOrgService;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
import org.jeecg.modules.erp.service.IErpPurchaseAdjustmentService;
import org.jeecg.modules.erp.service.IErpSalesDeliveryOrderService;
import org.jeecg.modules.erp.service.IErpSalesOrderService;
import org.jeecg.modules.erp.service.IErpSupplierService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpSyncXxlJobHandlerTest {

    @AfterEach
    void clearXxlJobContext() {
        XxlJobContext.setXxlJobContext(null);
    }

    @Test
    void erpSalesOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        IErpMaterialService materialService = mock(IErpMaterialService.class);
        IErpSupplierService supplierService = mock(IErpSupplierService.class);
        IErpPurchaseAdjustmentService purchaseAdjustmentService = mock(IErpPurchaseAdjustmentService.class);
        IErpOrgService orgService = mock(IErpOrgService.class);
        IErpSalesOrderService salesOrderService = mock(IErpSalesOrderService.class);
        IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
        IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
        when(salesOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = new ErpSyncXxlJobHandler(
                materialService,
                supplierService,
                purchaseAdjustmentService,
                orgService,
                salesOrderService,
                productionOrderService,
                salesDeliveryOrderService,
                LocalDate.of(2026, 6, 26));

        handler.erpSalesOrderSyncJob();

        verify(salesOrderService).queryByDate("2026-06-26", "2026-06-30");
    }

    @Test
    void erpProductionOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        IErpMaterialService materialService = mock(IErpMaterialService.class);
        IErpSupplierService supplierService = mock(IErpSupplierService.class);
        IErpPurchaseAdjustmentService purchaseAdjustmentService = mock(IErpPurchaseAdjustmentService.class);
        IErpOrgService orgService = mock(IErpOrgService.class);
        IErpSalesOrderService salesOrderService = mock(IErpSalesOrderService.class);
        IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
        IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
        when(productionOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = new ErpSyncXxlJobHandler(
                materialService,
                supplierService,
                purchaseAdjustmentService,
                orgService,
                salesOrderService,
                productionOrderService,
                salesDeliveryOrderService,
                LocalDate.of(2026, 6, 26));

        handler.erpProductionOrderSyncJob();

        verify(productionOrderService).queryByDate("2026-06-26", "2026-06-30");
    }

    @Test
    void erpSalesDeliveryOrderSyncJobUsesCurrentDateWhenParamIsBlank() {
        IErpMaterialService materialService = mock(IErpMaterialService.class);
        IErpSupplierService supplierService = mock(IErpSupplierService.class);
        IErpPurchaseAdjustmentService purchaseAdjustmentService = mock(IErpPurchaseAdjustmentService.class);
        IErpOrgService orgService = mock(IErpOrgService.class);
        IErpSalesOrderService salesOrderService = mock(IErpSalesOrderService.class);
        IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
        IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
        when(salesDeliveryOrderService.queryByDate("2026-06-26", "2026-06-30")).thenReturn(List.of());
        XxlJobContext.setXxlJobContext(new XxlJobContext(1L, "", null, 0, 1));

        ErpSyncXxlJobHandler handler = new ErpSyncXxlJobHandler(
                materialService,
                supplierService,
                purchaseAdjustmentService,
                orgService,
                salesOrderService,
                productionOrderService,
                salesDeliveryOrderService,
                LocalDate.of(2026, 6, 26));

        handler.erpSalesDeliveryOrderSyncJob();

        verify(salesDeliveryOrderService).queryByDate("2026-06-26", "2026-06-30");
    }
}
