package com.nexora.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板管理器 — 从 classpath 加载 YAML Prompt 模板
 */
@Slf4j
@Component
public class PromptManager {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 获取 Prompt 模板内容
     */
    public String getPrompt(String templateName) {
        return cache.computeIfAbsent(templateName, this::loadPrompt);
    }

    /**
     * 渲染 Prompt — 替换模板变量 {{key}}
     */
    public String render(String templateName, Map<String, String> variables) {
        String template = getPrompt(templateName);
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String loadPrompt(String name) {
        String path = "prompts/" + name + ".yaml";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("Failed to load prompt: {}", path, e);
        }
        // 返回默认 prompt
        log.info("Using default prompt for: {}", name);
        return getDefaultPrompt(name);
    }

    private String getDefaultPrompt(String name) {
        return switch (name) {
            case "news-summary" -> """
                    ╔══════════════════════════════════════════════════════════════╗
                    ║  CRITICAL: TRANSLATE + REWRITE entirely in {{language}}.  ║
                    ║  Source may be in ANY language — detect and DO NOT mirror. ║
                    ║  NEVER output the original language. NEVER mix languages.   ║
                    ╚══════════════════════════════════════════════════════════════╝

                    Step 1: Detect the source language of Title/Content.
                    Step 2: Translate ALL meaningful content into {{language}}.
                    Step 3: Write every field value in {{language}} — no exceptions.

                    Output strict JSON (no markdown, no explanation, no code blocks):
                    {
                      "title": "News title fully translated into {{language}}",
                      "summary": "2-3 sentence summary in {{language}}",
                      "facts": ["Fact 1 in {{language}}", "Fact 2 in {{language}}", "Fact 3 in {{language}}"],
                      "background": "Relevant context in {{language}}",
                      "impact": "Potential impact in {{language}}"
                    }

                    VERIFY before responding: Are ALL 5 fields in {{language}}?
                    If any field contains non-{{language}} text, REWRITE it.

                    Title: {{title}}
                    Content: {{content}}
                    """;
            case "news-classify" -> """
                    Classify the following news. Output strict JSON (no markdown):
                    {"category":"category name","subCategory":"sub category"}

                    Available categories: technology, AI, finance, international, domestic, society, sports
                    Sub-category examples: smartphone, artificial intelligence, banking, diplomacy, education, football

                    Title: {{title}}
                    Content: {{content}}
                    """;
            case "entity-extract" -> """
                    Extract named entities from the following news. Output strict JSON array (no markdown):
                    [{"name":"entity name","type":"type label"}]

                    Type labels: company, person, country, product, organization, city, technology

                    Example: [{"name":"OpenAI","type":"company"},{"name":"GPT-6","type":"product"},{"name":"USA","type":"country"}]

                    Content: {{content}}
                    """;
            default -> "Process the following: {{content}}";
        };
    }
}
