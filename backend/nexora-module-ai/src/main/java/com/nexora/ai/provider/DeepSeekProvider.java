package com.nexora.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.ai.config.AIProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek Provider — 调用 DeepSeek API
 * API 兼容 OpenAI Chat Completions 格式
 */
@Slf4j
public class DeepSeekProvider implements LLMProvider {

    private final AIProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekProvider(AIProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userMessage) {
        String url = properties.getBaseUrl() + "/v1/chat/completions";

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.3,
                "max_tokens", 2048
        );

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            long duration = System.currentTimeMillis() - start;
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("DeepSeek API 返回空响应");
            }

            // 提取 choices[0].message.content
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("DeepSeek API 返回无 choices: {}", body);
                throw new RuntimeException("AI 响应异常");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            // 提取 token 用量
            Map<String, Object> usage = (Map<String, Object>) body.get("usage");
            int tokens = usage != null ? (int) usage.getOrDefault("total_tokens", 0) : 0;

            log.info("DeepSeek 调用成功: model={}, duration={}ms, tokens={}, responseLen={}",
                    properties.getModel(), duration, tokens, content.length());

            return content;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("DeepSeek 调用失败: url={}, duration={}ms", url, duration, e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }
}
