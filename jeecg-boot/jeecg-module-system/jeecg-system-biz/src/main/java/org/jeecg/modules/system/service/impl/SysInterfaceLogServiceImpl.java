package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.mapper.SysInterfaceLogMapper;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class SysInterfaceLogServiceImpl extends ServiceImpl<SysInterfaceLogMapper, SysInterfaceLog>
        implements ISysInterfaceLogService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 2000;

    @Override
    public SysInterfaceLog start(InterfaceLogContext context) {
        Date now = new Date();
        SysInterfaceLog logEntity = new SysInterfaceLog();
        logEntity.setId(IdWorker.getIdStr());
        if (context != null) {
            logEntity.setTraceId(context.getTraceId());
            logEntity.setBizType(context.getBizType());
            logEntity.setBizName(context.getBizName());
            logEntity.setSourceService(context.getSourceService());
            logEntity.setInterfaceName(context.getInterfaceName());
            logEntity.setRequestMethod(context.getRequestMethod());
            logEntity.setRequestUrl(context.getRequestUrl());
            logEntity.setRequestHeaders(maskSensitiveHeaders(context.getRequestHeaders()));
            logEntity.setRequestBody(context.getRequestBody());
            logEntity.setStartTime(context.getStartTime() == null ? now : context.getStartTime());
        } else {
            logEntity.setStartTime(now);
        }
        logEntity.setSuccess(false);
        logEntity.setCreateTime(now);
        try {
            baseMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("接口日志开始记录保存失败，traceId：{}", logEntity.getTraceId(), e);
        }
        return logEntity;
    }

    @Override
    public void success(String logId, Integer responseStatus, String responseBody, Long costTime) {
        if (StrUtil.isBlank(logId)) {
            return;
        }
        SysInterfaceLog logEntity = new SysInterfaceLog();
        logEntity.setId(logId);
        logEntity.setSuccess(true);
        logEntity.setResponseStatus(responseStatus);
        logEntity.setResponseBody(responseBody);
        logEntity.setCostTime(costTime);
        logEntity.setEndTime(new Date());
        try {
            baseMapper.updateById(logEntity);
        } catch (Exception e) {
            log.warn("接口日志成功记录更新失败，logId：{}", logId, e);
        }
    }

    @Override
    public void fail(String logId, Integer responseStatus, String responseBody, Throwable error, Long costTime) {
        if (StrUtil.isBlank(logId)) {
            return;
        }
        SysInterfaceLog logEntity = new SysInterfaceLog();
        logEntity.setId(logId);
        logEntity.setSuccess(false);
        logEntity.setResponseStatus(responseStatus);
        logEntity.setResponseBody(responseBody);
        logEntity.setCostTime(costTime);
        logEntity.setEndTime(new Date());
        if (error != null) {
            logEntity.setErrorType(error.getClass().getName());
            logEntity.setErrorMessage(limitLength(error.getMessage(), ERROR_MESSAGE_MAX_LENGTH));
        }
        try {
            baseMapper.updateById(logEntity);
        } catch (Exception e) {
            log.warn("接口日志失败记录更新失败，logId：{}", logId, e);
        }
    }

    private static String maskSensitiveHeaders(String headers) {
        if (StrUtil.isBlank(headers)) {
            return headers;
        }
        String masked = headers.replaceAll("(?i)(\"(?:cookie|authorization|token|headerToken)\"\\s*:\\s*\")([^\"]*)", "$1***");
        masked = masked.replaceAll("(?i)(kdservice-sessionid=)[^;,\"\\s]+", "$1***");
        masked = masked.replaceAll("(?i)(Bearer\\s+)[^;,\"\\s]+", "$1***");
        return masked;
    }

    private static String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
