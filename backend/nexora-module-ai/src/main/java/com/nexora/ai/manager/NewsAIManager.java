package com.nexora.ai.manager;

import com.nexora.ai.provider.LLMProvider;
import com.nexora.ai.prompt.PromptManager;
import com.nexora.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 分析编排器 — 摘要→分类→实体识别→保存结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsAIManager {

    private final LLMProvider llmProvider;
    private final PromptManager promptManager;

    /**
     * 执行完整 AI 分析
     * @return 分析结果 JSON
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyze(String title, String content) {
        log.info("开始AI分析: title={}", title.substring(0, Math.min(80, title.length())));

        // 1. 生成摘要
        String summaryPrompt = promptManager.render("news-summary",
                Map.of("content", content));
        String summaryResult = llmProvider.chat("", summaryPrompt);
        Map<String, Object> summaryMap = JsonUtils.fromJson(summaryResult, Map.class);

        // 2. 新闻分类
        String classifyPrompt = promptManager.render("news-classify",
                Map.of("title", title, "content", content));
        String classifyResult = llmProvider.chat("", classifyPrompt);
        Map<String, Object> classifyMap = JsonUtils.fromJson(classifyResult, Map.class);

        // 3. 实体识别
        String entityPrompt = promptManager.render("entity-extract",
                Map.of("content", content));
        String entityResult = llmProvider.chat("", entityPrompt);

        log.info("AI分析完成: title={}, summary={}chars, category={}",
                title.substring(0, Math.min(30, title.length())),
                summaryMap.getOrDefault("summary", "").toString().length(),
                classifyMap.getOrDefault("category", "unknown"));

        return Map.of(
                "summary", summaryMap.getOrDefault("summary", ""),
                "facts", summaryMap.getOrDefault("facts", java.util.Collections.emptyList()),
                "background", summaryMap.getOrDefault("background", ""),
                "impact", summaryMap.getOrDefault("impact", ""),
                "category", classifyMap.getOrDefault("category", "科技"),
                "subCategory", classifyMap.getOrDefault("subCategory", ""),
                "entities", JsonUtils.fromJson(entityResult, Object.class)
        );
    }
}
