package com.nexora.ai.config;

import com.nexora.ai.provider.DeepSeekProvider;
import com.nexora.ai.provider.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块配置 — 生产环境直接使用 DeepSeek，未配置 API Key 时启动报错。
 */
@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public LLMProvider llmProvider(AIProperties properties) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "未配置 LLM_API_KEY 环境变量，AI 模块无法启动。" +
                    "请设置环境变量 LLM_API_KEY=sk-xxx 或 JVM 参数 -Dllm.api-key=sk-xxx。");
        }
        log.info("启用 LLM Provider: model={}, baseUrl={}",
                properties.getModel(), properties.getBaseUrl());
        return new DeepSeekProvider(properties);
    }
}
