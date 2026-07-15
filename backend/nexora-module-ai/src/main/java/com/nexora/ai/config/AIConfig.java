package com.nexora.ai.config;

import com.nexora.ai.provider.DeepSeekProvider;
import com.nexora.ai.provider.LLMProvider;
import com.nexora.ai.provider.MockLLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块配置
 * - 有 API Key → DeepSeekProvider (真实调用)
 * - 无 API Key → MockLLMProvider (开发/测试用)
 */
@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public LLMProvider llmProvider(AIProperties properties) {
        if (properties.isEnabled()) {
            log.info("启用 DeepSeek Provider: model={}, baseUrl={}",
                    properties.getModel(), properties.getBaseUrl());
            return new DeepSeekProvider(properties);
        }
        log.warn("未配置 LLM_API_KEY，使用 Mock Provider。设置环境变量 LLM_API_KEY 以启用真实 AI 调用。");
        return new MockLLMProvider();
    }
}
