package org.jeecg.modules.erp.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.service.ErpRetryService;
import org.springframework.stereotype.Component;

/**
 * ERP接口失败重试定时任务。
 * <p>
 * 定期扫描 sys_interface_log 中 retryStatus=PENDING 的记录，
 * 重新发送失败的ERP接口请求并将结果保存到数据库。
 * <p>
 * XXL-Job 配置建议：
 * <ul>
 *   <li>JobHandler: erpInterfaceRetryJob</li>
 *   <li>CRON: 0 * /5 * * * ?  (每5分钟执行一次)</li>
 * </ul>
 */
@Slf4j
@Component
public class ErpRetryXxlJobHandler {

    private final ErpRetryService erpRetryService;

    public ErpRetryXxlJobHandler(ErpRetryService erpRetryService) {
        this.erpRetryService = erpRetryService;
    }

    @XxlJob("erpInterfaceRetryJob")
    public void erpInterfaceRetryJob() {
        XxlJobHelper.log("开始执行ERP接口失败重试任务");
        try {
            int successCount = erpRetryService.retryFailedLogs();
            String message = "ERP接口重试任务完成，成功：" + successCount;
            log.info(message);
            XxlJobHelper.log(message);
            XxlJobHelper.handleSuccess(message);
        } catch (Exception e) {
            String message = "ERP接口重试任务执行异常：" + e.getMessage();
            log.error(message, e);
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(message);
        }
    }
}
