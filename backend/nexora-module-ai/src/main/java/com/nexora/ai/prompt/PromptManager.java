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
                    You MUST write ALL output in {{language}}. Do NOT use any other language.
                    Analyze the following news and output strict JSON (no markdown, no explanation):
                    {
                      "title": "A concise, accurate title translated into {{language}}",
                      "summary": "2-3 sentences in {{language}} summarizing the core content",
                      "facts": ["Key fact 1 in {{language}}", "Key fact 2 in {{language}}", "Key fact 3 in {{language}}"],
                      "background": "Relevant context in {{language}}",
                      "impact": "Potential impact of this event in {{language}}"
                    }

                    ⚠️ IMPORTANT: Every single field value MUST be written in {{language}}. Never keep the original language.

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
