package com.nexora.ai.manager;

import com.nexora.ai.provider.LLMProvider;
import com.nexora.ai.prompt.PromptManager;
import com.nexora.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 分析编排器 — 生成多语言摘要 + 分类 + 实体识别。
 * 返回结构：{"zh":{title,summary,facts,background,impact}, "en":{...}, ... , "category":"...","entities":[...]}
 *
 * 新增语言：在 TARGET_LANGUAGES 列表中添加条目即可。
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

    /** 目标分析语言（按需添加，aiResult key = langCode） */
    private record LangDef(String langCode, String langName, String systemPrompt) {}
    private static final List<LangDef> TARGET_LANGUAGES = List.of(
            new LangDef("zh", "中文", "你是一名新闻编辑。只输出JSON，不要markdown代码块。"),
            new LangDef("en", "English", "You are a news editor. Output ONLY JSON, no markdown."),
            new LangDef("ja", "日本語", "あなたはニュース編集者です。JSONのみを出力し、マークダウンは使わないでください。"),
            new LangDef("ko", "한국어", "당신은 뉴스 편집자입니다. JSON만 출력하고 마크다운은 사용하지 마세요."),
            new LangDef("fr", "Français", "Vous êtes un éditeur de presse. Sortie JSON uniquement, pas de markdown."),
            new LangDef("de", "Deutsch", "Sie sind Nachrichtenredakteur. Nur JSON ausgeben, kein Markdown."),
            new LangDef("ru", "Русский", "Вы редактор новостей. Выводите только JSON, без markdown.")
    );

    public Map<String, Object> analyze(String title, String content) {
        log.info("开始AI分析: title={}", title.length() > 80 ? title.substring(0, 80) : title);

        String shortContent = content != null && content.length() > 1500
                ? content.substring(0, 1500) : (content != null ? content : "");

        // ---- 多语言 AI 分析 ----
        Map<String, Object> result = new LinkedHashMap<>();
        for (LangDef lang : TARGET_LANGUAGES) {
            String prompt = buildLangPrompt(lang, title, content != null ? content : "");
            String aiResult = llmProvider.chat(lang.systemPrompt, prompt);
            Map<String, Object> section = parseJsonSafe(aiResult);
            if (section == null) section = new LinkedHashMap<>();
            // 标题回退：LLM 未生成则用原标题
            if (!section.containsKey("title") || section.get("title") == null
                    || section.get("title").toString().isBlank()) {
                section.put("title", title != null ? title : "");
            }
            result.put(lang.langCode, section);
            log.info("AI [{}]: {}chars", lang.langCode,
                    section.getOrDefault("summary", "").toString().length());
        }

        // ---- 分类 ----
        String clsPrompt = promptManager.render("news-classify",
                Map.of("title", title != null ? title : "", "content", shortContent));
        String clsResult = llmProvider.chat("Output ONLY JSON: {\"category\":\"...\",\"subCategory\":\"...\"}", clsPrompt);
        Map<String, Object> cls = parseJsonSafe(clsResult);

        // ---- 实体 ----
        String entPrompt = promptManager.render("entity-extract",
                Map.of("content", shortContent));
        String entResult = llmProvider.chat("Output ONLY a JSON array, nothing else.", entPrompt);

        result.put("category", cls.getOrDefault("category", "Technology"));
        result.put("subCategory", cls.getOrDefault("subCategory", ""));
        result.put("entities", parseJsonSafe(entResult));

        log.info("AI分析完成: languages={}, category={}", result.keySet().stream()
                .filter(TARGET_LANGUAGES.stream().map(LangDef::langCode).toList()::contains)
                .toList(), result.get("category"));

        return result;
    }

    /** 为目标语言构建分析 prompt — 所有语言走同一个模板，language 参数驱动 LLM 输出语言 */
    private String buildLangPrompt(LangDef lang, String title, String content) {
        return promptManager.render("news-summary", Map.of(
                "language", lang.langName,
                "title", title != null ? title : "",
                "content", content != null ? content : ""));
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
