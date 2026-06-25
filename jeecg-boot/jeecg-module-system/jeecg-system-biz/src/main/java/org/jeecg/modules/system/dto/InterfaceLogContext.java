package org.jeecg.modules.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class InterfaceLogContext {

    private String traceId;

    private String bizType;

    private String bizName;

    private String sourceService;

    private String interfaceName;

    private String requestMethod;

    private String requestUrl;

    private String requestHeaders;

    private String requestBody;

    private Date startTime;
}
