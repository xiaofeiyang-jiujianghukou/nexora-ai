package com.nexora.ai.provider;

import com.nexora.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Mock LLM Provider — 开发和测试用，返回模拟 AI 响应
 */
@Slf4j
public class MockLLMProvider implements LLMProvider {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public String getModelName() {
        return "mock-model";
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.debug("Mock LLM called: system={}, user={}", systemPrompt.substring(0, Math.min(50, systemPrompt.length())), userMessage.substring(0, Math.min(100, userMessage.length())));

        if (userMessage.contains("classify") || systemPrompt.contains("分类")) {
            return JsonUtils.toJson(Map.of("category", "AI", "subCategory", "LLM"));
        }

        if (userMessage.contains("entity") || systemPrompt.contains("实体")) {
            return JsonUtils.toJson(List.of(
                    Map.of("type", "company", "name", "OpenAI"),
                    Map.of("type", "product", "name", "GPT-6"),
                    Map.of("type", "country", "name", "美国")
            ));
        }

        // 默认：返回摘要
        return JsonUtils.toJson(Map.of(
                "summary", "这是一条关于AI技术发展的重要新闻。",
                "facts", List.of("新模型发布", "性能大幅提升", "行业广泛关注"),
                "background", "AI行业持续快速发展，各大厂商竞相推出新产品。",
                "impact", "将对科技行业产生深远影响，加速AI应用落地。"
        ));
    }
}
