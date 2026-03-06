package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiSummaryService {

    private final ChatClient chatClient;

    public AiSummaryService(ChatClient.Builder chatClientBuilder) {
        log.info("AiSummaryService 构造方法被调用");
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        你是一个笔记摘要助手。
                        请根据用户提供的笔记标题与内容生成中文摘要：
                        - 1~3 句话
                        - 简洁、客观
                        - 不要添加不存在的信息
                        """)
                .build();
        log.info("ChatClient 构建完成，是否为 null: {}", chatClient == null);
    }

    public String generateSummary(String title, String content) {
        log.info("generateSummary 被调用，title: {}, content长度: {}", title, content == null ? 0 : content.length());
        String safeTitle = title == null ? "" : title.trim();
        String safeContent = content == null ? "" : content.trim();

        String prompt = """
                标题：%s
                内容：%s
                
                请生成摘要：
                """.formatted(safeTitle, safeContent);

        try {
            String result = chatClient.prompt(prompt).call().content();
            log.info("AI 摘要生成成功，结果: {}", result);
            return result == null ? "" : result.trim();
        } catch (Exception ex) {
            log.error("AI 摘要生成失败", ex);  // 打印完整堆栈
            return "";
        }
    }
}