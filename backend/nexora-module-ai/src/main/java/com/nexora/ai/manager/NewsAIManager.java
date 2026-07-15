package com.nexora.ai.manager;

import com.nexora.ai.provider.LLMProvider;
import com.nexora.ai.prompt.PromptManager;
import com.nexora.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 分析编排器 — 生成多语言摘要 + 分类 + 实体识别。
 * 返回结构：{"zh":{...},"en":{...},"category":"...","subCategory":"...","entities":[...]}
 */
@Slf4j
@Component
public class NewsAIManager {

    private final LLMProvider llmProvider;
    private final PromptManager promptManager;

    public NewsAIManager(LLMProvider llmProvider, PromptManager promptManager) {
        this.llmProvider = llmProvider;
        this.promptManager = promptManager;
    }

    public Map<String, Object> analyze(String title, String content) {
        log.info("开始AI分析: title={}", title.length() > 80 ? title.substring(0, 80) : title);

        String shortContent = content != null && content.length() > 1500
                ? content.substring(0, 1500) : (content != null ? content : "");

        // ---- 中文摘要 ----
        String zhPrompt = promptManager.render("news-summary", Map.of("content", content != null ? content : ""));
        String zhResult = llmProvider.chat("你是一名新闻编辑。只输出JSON，不要markdown代码块。", zhPrompt);
        Map<String, Object> zh = parseJsonSafe(zhResult);

        // ---- 英文摘要 ----
        String enPrompt = buildEnglishPrompt(title, shortContent);
        String enResult = llmProvider.chat("You are a news editor. Output ONLY JSON, no markdown.", enPrompt);
        Map<String, Object> en = parseJsonSafe(enResult);

        // ---- 分类 ----
        String clsPrompt = promptManager.render("news-classify",
                Map.of("title", title != null ? title : "", "content", shortContent));
        String clsResult = llmProvider.chat("只输出JSON：{\"category\":\"类别\",\"subCategory\":\"子类别\"}", clsPrompt);
        Map<String, Object> cls = parseJsonSafe(clsResult);

        // ---- 实体 ----
        String entPrompt = promptManager.render("entity-extract",
                Map.of("content", shortContent));
        String entResult = llmProvider.chat("只输出JSON数组，不要其他文字。", entPrompt);

        // ---- 组装多语言结果 ----
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("zh", zh);
        result.put("en", en);
        result.put("category", cls.getOrDefault("category", "科技"));
        result.put("subCategory", cls.getOrDefault("subCategory", ""));
        result.put("entities", parseJsonSafe(entResult));
        result.put("facts", zh.getOrDefault("facts", java.util.Collections.emptyList()));
        result.put("background", zh.getOrDefault("background", ""));
        result.put("impact", zh.getOrDefault("impact", ""));

        String summaryZh = (String) zh.getOrDefault("summary", "");
        String summaryEn = (String) en.getOrDefault("summary", "");
        log.info("AI分析完成: zh={}chars, en={}chars, category={}", summaryZh.length(), summaryEn.length(), result.get("category"));

        return result;
    }

    private String buildEnglishPrompt(String title, String content) {
        return "Analyze this news and output strict JSON (no markdown, no explanation):\n" +
                "{\"summary\":\"2-3 sentence English summary (80-120 words)\",\n" +
                " \"facts\":[\"key fact 1\",\"key fact 2\",\"key fact 3\"],\n" +
                " \"background\":\"context (40-60 words)\",\n" +
                " \"impact\":\"potential impact (40-60 words)\"}\n\n" +
                "Example output:\n" +
                "{\"summary\":\"Apple unveiled its M4-powered MacBook Pro with significant performance gains and extended battery life.\"," +
                "\"facts\":[\"M4 chip uses 2nm process\",\"CPU 50pct faster, GPU 80pct faster\",\"24-hour battery life\"]," +
                "\"background\":\"This continues Apple's annual chip upgrade cycle following M3.\"," +
                "\"impact\":\"Further solidifies Apple's lead in high-end laptops and pushes ARM architecture adoption.\"}\n\n" +
                "Title: " + (title != null ? title : "") + "\n" +
                "Content: " + (content != null ? content : "");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonSafe(String json) {
        try {
            String cleaned = json.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-z]*\\s*", "").replace("```", "").trim();
            }
            if (cleaned.startsWith("{")) {
                return JsonUtils.fromJson(cleaned, Map.class);
            }
            if (cleaned.startsWith("[")) {
                return Map.of("items", JsonUtils.getObjectMapper().readValue(cleaned, Object.class));
            }
            log.warn("AI non-JSON: {}", cleaned.substring(0, Math.min(80, cleaned.length())));
            return Map.of();
        } catch (Exception e) {
            log.warn("AI parse err: {} | {}...", e.getMessage(),
                    json.length() > 50 ? json.substring(0, 50) : json);
            return Map.of();
        }
    }
}
