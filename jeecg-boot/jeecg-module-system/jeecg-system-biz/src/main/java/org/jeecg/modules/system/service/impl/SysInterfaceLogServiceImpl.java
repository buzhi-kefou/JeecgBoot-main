package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.mapper.SysInterfaceLogMapper;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SysInterfaceLogServiceImpl extends ServiceImpl<SysInterfaceLogMapper, SysInterfaceLog>
        implements ISysInterfaceLogService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 2000;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 重试退避时间（毫秒）：1分钟、5分钟、25分钟
     */
    private static final long[] RETRY_BACKOFF_MS = {60_000L, 300_000L, 1_500_000L};

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
//        logEntity.setResponseBody(responseBody);
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
        // 设置重试状态
        logEntity.setRetryStatus("PENDING");
        logEntity.setRetryCount(0);
        logEntity.setNextRetryTime(new Date(System.currentTimeMillis() + RETRY_BACKOFF_MS[0]));
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

    @Override
    public List<SysInterfaceLog> findRetryableLogs(int maxResults) {
        LambdaQueryWrapper<SysInterfaceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInterfaceLog::getRetryStatus, "PENDING")
                .eq(SysInterfaceLog::getBizType, "ERP_SYNC")
                .le(SysInterfaceLog::getNextRetryTime, new Date())
                .orderByAsc(SysInterfaceLog::getNextRetryTime)
                .last("LIMIT " + maxResults);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public void markRetrying(String logId) {
        SysInterfaceLog update = new SysInterfaceLog();
        update.setId(logId);
        update.setRetryStatus("RETRYING");
        try {
            baseMapper.updateById(update);
        } catch (Exception e) {
            log.warn("标记重试中失败，logId：{}", logId, e);
        }
    }

    @Override
    public void markRetrySuccess(String logId) {
        SysInterfaceLog update = new SysInterfaceLog();
        update.setId(logId);
        update.setRetryStatus("SUCCESS");
        update.setSuccess(true);
        try {
            baseMapper.updateById(update);
        } catch (Exception e) {
            log.warn("标记重试成功失败，logId：{}", logId, e);
        }
    }

    @Override
    public void markRetryFailed(String logId) {
        SysInterfaceLog update = new SysInterfaceLog();
        update.setId(logId);
        update.setRetryStatus("FAILED");
        try {
            baseMapper.updateById(update);
        } catch (Exception e) {
            log.warn("标记重试失败，logId：{}", logId, e);
        }
    }

    @Override
    public void updateRetryInfo(String logId, int retryCount, Date nextRetryTime) {
        SysInterfaceLog update = new SysInterfaceLog();
        update.setId(logId);
        update.setRetryStatus("PENDING");
        update.setRetryCount(retryCount);
        update.setNextRetryTime(nextRetryTime);
        try {
            baseMapper.updateById(update);
        } catch (Exception e) {
            log.warn("更新重试信息失败，logId：{}", logId, e);
        }
    }

    static long getRetryBackoffMs(int retryCount) {
        if (retryCount < 0 || retryCount >= RETRY_BACKOFF_MS.length) {
            return RETRY_BACKOFF_MS[RETRY_BACKOFF_MS.length - 1];
        }
        return RETRY_BACKOFF_MS[retryCount];
    }

    static int getMaxRetryAttempts() {
        return MAX_RETRY_ATTEMPTS;
    }
}
