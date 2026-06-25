package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;

public interface ISysInterfaceLogService extends IService<SysInterfaceLog> {

    SysInterfaceLog start(InterfaceLogContext context);

    void success(String logId, Integer responseStatus, String responseBody, Long costTime);

    void fail(String logId, Integer responseStatus, String responseBody, Throwable error, Long costTime);
}
