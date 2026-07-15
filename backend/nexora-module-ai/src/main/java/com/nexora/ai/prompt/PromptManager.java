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
                    role:
                      system: 你是一名专业新闻编辑
                    task: 请用中文总结以下新闻，输出JSON格式：
                    {
                      "summary": "200字以内的摘要",
                      "facts": ["事实1", "事实2"],
                      "background": "事件背景",
                      "impact": "影响分析"
                    }
                    新闻内容：{{content}}
                    """;
            case "news-classify" -> """
                    role:
                      system: 你是一名新闻分类专家
                    task: 请将以下新闻分类，可选: 国内/国际/科技/AI/财经/社会/体育
                    新闻标题：{{title}}
                    新闻内容：{{content}}
                    """;
            case "entity-extract" -> """
                    role:
                      system: 你是一名信息抽取专家
                    task: 从以下新闻中提取实体 (公司/人物/国家/产品/组织)，输出JSON数组
                    新闻内容：{{content}}
                    """;
            default -> "请处理以下内容：{{content}}";
        };
    }
}
