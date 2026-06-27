package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;

import java.util.List;

public interface ISysInterfaceLogService extends IService<SysInterfaceLog> {

    SysInterfaceLog start(InterfaceLogContext context);

    void success(String logId, Integer responseStatus, String responseBody, Long costTime);

    void fail(String logId, Integer responseStatus, String responseBody, Throwable error, Long costTime);

    /**
     * 查找待重试的失败日志（retryStatus = PENDING 且 nextRetryTime <= now）。
     */
    List<SysInterfaceLog> findRetryableLogs(int maxResults);

    /**
     * 标记日志为重试中。
     */
    void markRetrying(String logId);

    /**
     * 标记日志为重试成功。
     */
    void markRetrySuccess(String logId);

    /**
     * 标记日志为重试失败（重试耗尽）。
     */
    void markRetryFailed(String logId);

    /**
     * 更新日志的重试信息（retryCount、nextRetryTime），保持 PENDING 状态。
     */
    void updateRetryInfo(String logId, int retryCount, java.util.Date nextRetryTime);
}
