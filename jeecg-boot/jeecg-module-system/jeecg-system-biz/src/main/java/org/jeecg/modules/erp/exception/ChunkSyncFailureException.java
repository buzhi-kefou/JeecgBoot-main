package org.jeecg.modules.erp.exception;

import lombok.Getter;
import org.jeecg.modules.erp.entity.ErpProductionOrderEntity;

import java.util.List;

/**
 * 生产订单分块同步失败异常，携带所有失败块的实体数据，供精确重试调度使用。
 */
@Getter
public class ChunkSyncFailureException extends RuntimeException {

    private final List<ErpProductionOrderEntity> failedEntities;

    public ChunkSyncFailureException(String message, List<ErpProductionOrderEntity> failedEntities) {
        super(message);
        this.failedEntities = failedEntities;
    }

}
