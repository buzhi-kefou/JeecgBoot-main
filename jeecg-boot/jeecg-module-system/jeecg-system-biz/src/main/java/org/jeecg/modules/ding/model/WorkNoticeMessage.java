package org.jeecg.modules.ding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 钉钉工作通知消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkNoticeMessage {

    /** 应用 AgentId */
    private String agent_id;

    /** 接收人 userId 列表，多个用逗号分隔（最多 5000 人） */
    private String userid_list;

    /** 消息内容 */
    private Msg msg;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Msg {
        private String msgtype;
        private TextContent text;
        private MarkdownContent markdown;
        private LinkContent link;
        private OaContent oa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent {
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkdownContent {
        private String title;
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkContent {
        private String title;
        private String text;
        private String messageUrl;
        private String picUrl;
    }

    /**
     * OA 消息内容（钉钉特有的工作通知格式）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OaContent {
        private String head;
        private OaBody body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OaBody {
        private String title;
        private String content;
        private String image;
        private String file_count;
        private String rich;
    }

    // ===== 快捷构建方法 =====

    /**
     * 构建文本工作通知
     */
    public static WorkNoticeMessage text(String agentId, List<String> userIds, String content) {
        return WorkNoticeMessage.builder()
                .agent_id(agentId)
                .userid_list(String.join(",", userIds))
                .msg(Msg.builder()
                        .msgtype("text")
                        .text(TextContent.builder().content(content).build())
                        .build())
                .build();
    }

    /**
     * 构建 Markdown 工作通知
     */
    public static WorkNoticeMessage markdown(String agentId, List<String> userIds, String title, String text) {
        return WorkNoticeMessage.builder()
                .agent_id(agentId)
                .userid_list(String.join(",", userIds))
                .msg(Msg.builder()
                        .msgtype("markdown")
                        .markdown(MarkdownContent.builder().title(title).text(text).build())
                        .build())
                .build();
    }
}
