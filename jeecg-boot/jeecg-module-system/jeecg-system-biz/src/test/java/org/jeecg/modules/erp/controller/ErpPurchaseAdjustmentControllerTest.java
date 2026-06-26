package org.jeecg.modules.erp.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;
import org.jeecg.modules.erp.entity.ErpSalesDeliveryOrderEntity;
import org.jeecg.modules.erp.service.IErpProductionOrderService;
import org.jeecg.modules.erp.service.IErpSalesDeliveryOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpPurchaseAdjustmentControllerTest {

    @Test
    void queryProductionOrderDelegatesToProductionOrderService() {
        IErpProductionOrderService productionOrderService = mock(IErpProductionOrderService.class);
        when(productionOrderService.queryByDate("2026-06-01", "2026-06-30"))
                .thenReturn(List.of(new ErpProductionOrderEntity(), new ErpProductionOrderEntity()));
        ErpPurchaseAdjustmentController controller = new ErpPurchaseAdjustmentController();
        ReflectionTestUtils.setField(controller, "erpProductionOrderService", productionOrderService);

        Result<Integer> result = controller.queryProductionOrder("2026-06-01", "2026-06-30");

        assertEquals(2, result.getResult());
        verify(productionOrderService).queryByDate("2026-06-01", "2026-06-30");
    }

    @Test
    void querySalesDeliveryOrderDelegatesToSalesDeliveryOrderService() {
        IErpSalesDeliveryOrderService salesDeliveryOrderService = mock(IErpSalesDeliveryOrderService.class);
        when(salesDeliveryOrderService.queryByDate("2026-06-01", "2026-06-30"))
                .thenReturn(List.of(new ErpSalesDeliveryOrderEntity(), new ErpSalesDeliveryOrderEntity()));
        ErpPurchaseAdjustmentController controller = new ErpPurchaseAdjustmentController();
        ReflectionTestUtils.setField(controller, "erpSalesDeliveryOrderService", salesDeliveryOrderService);

        Result<Integer> result = controller.querySalesDeliveryOrder("2026-06-01", "2026-06-30");

        assertEquals(2, result.getResult());
        verify(salesDeliveryOrderService).queryByDate("2026-06-01", "2026-06-30");
    }
}
