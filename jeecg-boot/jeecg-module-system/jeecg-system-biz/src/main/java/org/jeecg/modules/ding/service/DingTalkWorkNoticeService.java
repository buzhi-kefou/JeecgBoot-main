package org.jeecg.modules.ding.service;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.ding.config.DingTalkConfig;
import org.jeecg.modules.ding.model.WorkNoticeMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉工作通知消息推送服务
 *
 * 适用场景：向指定用户推送工作通知（审批提醒、任务通知、业务消息等）
 * 特点：消息出现在钉钉"工作通知"中，可指定具体用户
 * 限制：每个应用每天向同一用户最多发送 500 条通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkWorkNoticeService {

    private final DingTalkConfig dingTalkConfig;

    /** 缓存 access_token，避免频繁请求 */
    private final ConcurrentHashMap<String, TokenCache> tokenCache = new ConcurrentHashMap<>();

    /** token 有效期缓存（钉钉 token 有效期 2 小时） */
    private static final long TOKEN_EXPIRE_MS = 7200_000L;

    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
    private static final String SEND_MSG_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";

    /**
     * 发送文本工作通知
     */
    public String sendText(List<String> userIds, String content) {
        WorkNoticeMessage message = WorkNoticeMessage.text(
                dingTalkConfig.getWorkNotice().getAgentId(),
                userIds,
                content
        );
        return send(message);
    }

    /**
     * 发送 Markdown 工作通知
     */
    public String sendMarkdown(List<String> userIds, String title, String text) {
        WorkNoticeMessage message = WorkNoticeMessage.markdown(
                dingTalkConfig.getWorkNotice().getAgentId(),
                userIds,
                title,
                text
        );
        return send(message);
    }

    /**
     * 发送工作通知消息
     */
    public String send(WorkNoticeMessage message) {
        String accessToken = getAccessToken();
        String url = SEND_MSG_URL + "?access_token=" + accessToken;
        String jsonBody = JSONUtil.toJsonStr(message);
        log.info("发送工作通知: {}", jsonBody);

        String response = HttpUtil.post(url, jsonBody);
        log.info("工作通知响应: {}", response);
        return response;
    }

    /**
     * 获取企业内部应用 access_token（带缓存）
     */
    private String getAccessToken() {
        String appKey = dingTalkConfig.getWorkNotice().getAppKey();
        TokenCache cache = tokenCache.get(appKey);

        if (cache != null && System.currentTimeMillis() - cache.timestamp < TOKEN_EXPIRE_MS) {
            log.info("使用缓存的 access_token");
            return cache.token;
        }

        log.info("请求新的 access_token");
        String url = GET_TOKEN_URL + "?appkey=" + appKey + "&appsecret=" + dingTalkConfig.getWorkNotice().getAppSecret();
        String response = HttpUtil.get(url);
        log.info("获取 token 响应: {}", response);

        cn.hutool.json.JSONObject json = JSONUtil.parseObj(response);
        if (json.getInt("code") != 0) {
            throw new RuntimeException("获取 access_token 失败: " + response);
        }

        String token = json.getStr("access_token");
        tokenCache.put(appKey, new TokenCache(token, System.currentTimeMillis()));
        return token;
    }

    private record TokenCache(String token, long timestamp) {}
}
