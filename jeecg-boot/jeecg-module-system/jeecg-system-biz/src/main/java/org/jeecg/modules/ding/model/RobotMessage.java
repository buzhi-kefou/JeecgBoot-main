package org.jeecg.modules.ding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 钉钉群机器人消息通用模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotMessage {

    /** 消息类型：text / markdown / link / actionCard */
    private String msgtype;

    /** 文本消息内容 */
    private TextContent text;

    /** Markdown 消息内容 */
    private MarkdownContent markdown;

    /** 链接消息内容 */
    private LinkContent link;

    /** 卡片消息内容 */
    private ActionCardContent actionCard;

    /** @指定人 */
    private At at;

    // ===== 各消息类型的内部类 =====

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionCardContent {
        private String title;
        private String text;
        /** single / btnOrientation */
        private String btnOrientation;
        private List<ActionCardBtn> btns;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionCardBtn {
        private String title;
        private String actionURL;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class At {
        /** 被 @ 人的钉钉 userId 列表 */
        private List<String> atUserIds;
        /** 是否 @ 所有人 */
        @Builder.Default
        private Boolean isAtAll = false;
    }

    // ===== 快捷构建方法 =====

    /**
     * 构建纯文本消息
     */
    public static RobotMessage text(String content) {
        return RobotMessage.builder()
                .msgtype("text")
                .text(TextContent.builder().content(content).build())
                .build();
    }

    /**
     * 构建纯文本消息（带 @）
     */
    public static RobotMessage text(String content, List<String> atUserIds, boolean isAtAll) {
        return RobotMessage.builder()
                .msgtype("text")
                .text(TextContent.builder().content(content).build())
                .at(At.builder().atUserIds(atUserIds).isAtAll(isAtAll).build())
                .build();
    }

    /**
     * 构建 Markdown 消息
     */
    public static RobotMessage markdown(String title, String text) {
        return RobotMessage.builder()
                .msgtype("markdown")
                .markdown(MarkdownContent.builder().title(title).text(text).build())
                .build();
    }

    /**
     * 构建链接消息
     */
    public static RobotMessage link(String title, String text, String messageUrl, String picUrl) {
        return RobotMessage.builder()
                .msgtype("link")
                .link(LinkContent.builder().title(title).text(text).messageUrl(messageUrl).picUrl(picUrl).build())
                .build();
    }
}
