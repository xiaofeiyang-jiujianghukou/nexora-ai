package com.nexora.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置属性 — 从环境变量/配置文件注入
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class AIProperties {

    /** API Key */
    private String apiKey;

    /** API Base URL */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名称 */
    private String model = "deepseek-v4-pro";

    /** 是否启用真实 Provider (无 API Key 时使用 Mock) */
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }
}
