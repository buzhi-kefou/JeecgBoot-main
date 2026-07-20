package org.jeecg.modules.ding.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.ding.config.DingTalkConfig;
import org.jeecg.modules.ding.model.RobotMessage;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 钉钉群机器人消息推送服务
 *
 * 适用场景：向钉钉群推送通知消息（告警、日报、监控等）
 * 限制：每个机器人每分钟最多发送 20 条消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkRobotService {

    private final DingTalkConfig dingTalkConfig;

    /**
     * 发送消息到钉钉群
     *
     * @param message 消息对象（支持 text / markdown / link / actionCard）
     * @return 钉钉接口返回的响应
     */
    public String send(RobotMessage message) {
        String webhook = buildWebhookUrl();
        String jsonBody = JSONUtil.toJsonStr(message);
        log.info("发送钉钉群机器人消息: {}", jsonBody);

        String response = HttpUtil.post(webhook, jsonBody);
        log.info("钉钉群机器人响应: {}", response);
        return response;
    }

    /**
     * 发送纯文本消息
     */
    public String sendText(String content) {
        return send(RobotMessage.text(content));
    }

    /**
     * 发送纯文本消息（带 @）
     */
    public String sendText(String content, java.util.List<String> atUserIds, boolean isAtAll) {
        return send(RobotMessage.text(content, atUserIds, isAtAll));
    }

    /**
     * 发送 Markdown 消息
     */
    public String sendMarkdown(String title, String text) {
        return send(RobotMessage.markdown(title, text));
    }

    /**
     * 发送链接消息
     */
    public String sendLink(String title, String text, String messageUrl, String picUrl) {
        return send(RobotMessage.link(title, text, messageUrl, picUrl));
    }

    /**
     * 构建 Webhook URL（如果配置了 secret，会自动加签）
     *
     * 加签流程：
     * 1. timestamp = 当前时间毫秒值
     * 2. sign = HMAC-SHA256(timestamp + "\n" + secret, secret) → Base64 → URL编码
     * 3. 将 timestamp 和 sign 拼接到 webhook URL
     */
    private String buildWebhookUrl() {
        return buildWebhookUrl(System.currentTimeMillis());
    }

    /**
     * 使用指定时间戳构建 Webhook URL，便于验证签名结果。
     */
    String buildWebhookUrl(long timestamp) {
        String webhook = dingTalkConfig.getRobot().getWebhook();
        String secret = dingTalkConfig.getRobot().getSecret();

        if (secret == null || secret.isBlank()) {
            log.info("未配置加签密钥，使用原始 Webhook URL");
            return webhook;
        }

        String stringToSign = timestamp + "\n" + secret;
        byte[] hmac = SecureUtil.hmacSha256(secret.getBytes(StandardCharsets.UTF_8))
                .digest(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(hmac), StandardCharsets.UTF_8);

        String separator = webhook.contains("?") ? "&" : "?";
        log.debug("已为钉钉群机器人 Webhook 生成加签参数，timestamp={}", timestamp);
        return webhook + separator + "timestamp=" + timestamp + "&sign=" + sign;
    }
}
