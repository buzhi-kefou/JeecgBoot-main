package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_interface_log")
public class SysInterfaceLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String traceId;

    private String bizType;

    private String bizName;

    private String sourceService;

    private String interfaceName;

    private String requestMethod;

    private String requestUrl;

    private String requestHeaders;

    private String requestBody;

    private Integer responseStatus;

    private String responseBody;

    private Boolean success;

    private String errorType;

    private String errorMessage;

    private Long costTime;

    private Date startTime;

    private Date endTime;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    /**
     * 重试状态：PENDING-待重试, RETRYING-重试中, SUCCESS-重试成功, FAILED-重试耗尽, SKIPPED-跳过
     */
    private String retryStatus;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    private Date nextRetryTime;
}
