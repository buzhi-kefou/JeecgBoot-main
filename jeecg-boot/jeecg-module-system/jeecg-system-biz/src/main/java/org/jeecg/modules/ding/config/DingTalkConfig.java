package org.jeecg.modules.ding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkConfig {

    private Robot robot = new Robot();
    private WorkNotice workNotice = new WorkNotice();

    @Data
    public static class Robot {
        /** 群机器人 Webhook 地址 */
        private String webhook;
        /** 群机器人加签密钥 */
        private String secret;
    }

    @Data
    public static class WorkNotice {
        /** 企业内部应用 AppKey */
        private String appKey;
        /** 企业内部应用 AppSecret */
        private String appSecret;
        /** 应用 AgentId */
        private String agentId;
    }
}
