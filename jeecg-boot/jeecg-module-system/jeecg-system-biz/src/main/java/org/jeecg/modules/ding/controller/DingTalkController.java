package org.jeecg.modules.ding.controller;


import lombok.RequiredArgsConstructor;
import org.jeecg.modules.ding.model.RobotMessage;
import org.jeecg.modules.ding.service.DingTalkRobotService;
import org.jeecg.modules.ding.service.DingTalkWorkNoticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钉钉消息推送测试接口
 */
@RestController
@RequestMapping("/dingtalk")
@RequiredArgsConstructor
public class DingTalkController {

    private final DingTalkRobotService robotService;
    private final DingTalkWorkNoticeService workNoticeService;

    // ==================== 群机器人接口 ====================

    /**
     * 发送纯文本消息到钉钉群
     *
     * POST /dingtalk/robot/text
     * Body: { "content": "这是测试消息" }
     */
    @PostMapping("/robot/text")
    public String sendRobotText(@RequestBody TextRequest request) {
        return robotService.sendText(request.getContent());
    }

    /**
     * 发送纯文本消息到钉钉群（带 @）
     *
     * POST /dingtalk/robot/text-at
     * Body: { "content": "紧急通知", "atUserIds": ["user001"], "isAtAll": false }
     */
    @PostMapping("/robot/text-at")
    public String sendRobotTextAt(@RequestBody TextAtRequest request) {
        return robotService.sendText(request.getContent(), request.getAtUserIds(), request.isAtAll());
    }

    /**
     * 发送 Markdown 消息到钉钉群
     *
     * POST /dingtalk/robot/markdown
     * Body: { "title": "系统告警", "text": "### 告警通知\n> 服务异常\n> 时间：2026-07-16" }
     */
    @PostMapping("/robot/markdown")
    public String sendRobotMarkdown(@RequestBody MarkdownRequest request) {
        return robotService.sendMarkdown(request.getTitle(), request.getText());
    }

    /**
     * 发送链接消息到钉钉群
     *
     * POST /dingtalk/robot/link
     * Body: { "title": "审批通知", "text": "您有一条新的审批待处理", "messageUrl": "https://xxx", "picUrl": "" }
     */
    @PostMapping("/robot/link")
    public String sendRobotLink(@RequestBody LinkRequest request) {
        return robotService.sendLink(request.getTitle(), request.getText(), request.getMessageUrl(), request.getPicUrl());
    }

    /**
     * 发送自定义消息到钉钉群（可自行组装任意类型）
     *
     * POST /dingtalk/robot/custom
     * Body: 完整的 RobotMessage 对象
     */
    @PostMapping("/robot/custom")
    public String sendRobotCustom(@RequestBody RobotMessage message) {
        return robotService.send(message);
    }

    // ==================== 工作通知接口 ====================

    /**
     * 发送文本工作通知给指定用户
     *
     * POST /dingtalk/work/text
     * Body: { "userIds": ["user001", "user002"], "content": "您有新的任务待处理" }
     */
    @PostMapping("/work/text")
    public String sendWorkText(@RequestBody WorkTextRequest request) {
        return workNoticeService.sendText(request.getUserIds(), request.getContent());
    }

    /**
     * 发送 Markdown 工作通知给指定用户
     *
     * POST /dingtalk/work/markdown
     * Body: { "userIds": ["user001"], "title": "审批提醒", "text": "### 审批提醒\n> 请及时处理" }
     */
    @PostMapping("/work/markdown")
    public String sendWorkMarkdown(@RequestBody WorkMarkdownRequest request) {
        return workNoticeService.sendMarkdown(request.getUserIds(), request.getTitle(), request.getText());
    }

    // ==================== 请求 DTO ====================

    @lombok.Data
    public static class TextRequest {
        private String content;
    }

    @lombok.Data
    public static class TextAtRequest {
        private String content;
        private List<String> atUserIds;
        private boolean isAtAll;
    }

    @lombok.Data
    public static class MarkdownRequest {
        private String title;
        private String text;
    }

    @lombok.Data
    public static class LinkRequest {
        private String title;
        private String text;
        private String messageUrl;
        private String picUrl;
    }

    @lombok.Data
    public static class WorkTextRequest {
        private List<String> userIds;
        private String content;
    }

    @lombok.Data
    public static class WorkMarkdownRequest {
        private List<String> userIds;
        private String title;
        private String text;
    }
}
