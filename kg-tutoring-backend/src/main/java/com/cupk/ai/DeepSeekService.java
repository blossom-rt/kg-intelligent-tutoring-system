package com.cupk.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek AI 服务 —— 调用 DeepSeek API 生成智能内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate;

    /**
     * 调用 DeepSeek 生成内容
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return AI 生成的文本
     */
    public String generate(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            log.info("调用 DeepSeek API, model={}", model);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);
            log.info("DeepSeek API 响应状态: {}", response.getStatusCode());

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                Object choicesObj = response.getBody().get("choices");
                if (choicesObj instanceof List) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                    if (!choices.isEmpty()) {
                        Object msgObj = choices.get(0).get("message");
                        if (msgObj instanceof Map) {
                            Map<String, Object> message = (Map<String, Object>) msgObj;
                            Object content = message.get("content");
                            if (content instanceof String) return (String) content;
                        }
                    }
                }
            }
            log.warn("DeepSeek API 返回异常: {}", response.getBody());
            return fallbackResponse(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            return fallbackResponse(systemPrompt, userPrompt);
        }
    }

    private String fallbackResponse(String systemPrompt, String userPrompt) {
        // 根据关键词生成默认的模板回复
        if (userPrompt.contains("知识点") || userPrompt.contains("学习总结")) {
            return "【学习要点】\n1. 理解核心概念和定义\n2. 掌握典型例题的解题方法\n3. 完成配套练习巩固知识\n\n【学习建议】\n建议结合教材内容进行学习，做好笔记，遇到不理解的地方可以反复学习。";
        }
        if (userPrompt.contains("错题") || userPrompt.contains("讲解")) {
            return "【解题思路】\n1. 仔细阅读题干，理解题目要求\n2. 分析已知条件\n3. 逐步推导答案\n\n【易错提醒】\n注意审题，避免遗漏关键条件。建议重新做一遍巩固。";
        }
        if (userPrompt.contains("测评") || userPrompt.contains("诊断")) {
            return "【成绩分析】\n请回顾错题，找出薄弱知识点进行针对性练习。\n\n【后续建议】\n1. 定期复习已学内容\n2. 建立错题本\n3. 制定合理学习计划";
        }
        return "AI 服务暂时不可用，请稍后重试。";
    }
}
