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
                    Analyze the following news and output strict JSON in {{language}} (no markdown, no explanation):
                    {
                      "title": "Write a concise, accurate title in {{language}} (translate the original if needed)",
                      "summary": "2-3 sentences summarizing the core content in {{language}} (80-120 words)",
                      "facts": ["Key fact 1", "Key fact 2", "Key fact 3"],
                      "background": "Relevant context and background in {{language}} (40-60 words)",
                      "impact": "Potential impact of this event in {{language}} (40-60 words)"
                    }

                    Example output:
                    {"title":"Apple Unveils M4-Powered MacBook Pro with 24-Hour Battery Life","summary":"Apple unveiled its new MacBook Pro powered by the M4 chip, featuring significant performance gains and extended battery life.","facts":["M4 chip uses 2nm process","CPU 50% faster, GPU 80% faster","24-hour battery life"],"background":"This follows Apple's annual chip upgrade cycle since M3, continuing ARM architecture momentum.","impact":"Further solidifies Apple's lead in high-end laptops and pushes industry-wide ARM adoption."}

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
