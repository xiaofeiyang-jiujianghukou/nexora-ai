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
                    请用中文分析以下新闻，输出严格JSON（不要markdown代码块，不要任何解释文字）：
                    {
                      "summary": "2-3句话概括新闻核心内容，100-200字",
                      "facts": ["关键事实1", "关键事实2", "关键事实3"],
                      "background": "事件背景和相关上下文，50-100字",
                      "impact": "该事件可能产生的影响，50-100字"
                    }

                    示例输出：
                    {"summary":"苹果发布了搭载M4芯片的新MacBook Pro，性能大幅提升。","facts":["M4芯片采用2nm工艺","CPU性能提升50%","电池续航24小时"],"background":"这是苹果继M3之后的新一代芯片，延续了每年更新一次的节奏。","impact":"将进一步巩固苹果在高端笔记本市场的领先地位，推动整个行业向ARM架构迁移。"}

                    新闻内容：{{content}}
                    """;
            case "news-classify" -> """
                    请将以下新闻分类。输出严格JSON（不要markdown代码块）：
                    {"category":"类别名称","subCategory":"子类别"}

                    可用类别：科技、AI、财经、国际、国内、社会、体育
                    子类别示例：智能手机、人工智能、金融、外交、教育、足球

                    新闻标题：{{title}}
                    新闻内容：{{content}}
                    """;
            case "entity-extract" -> """
                    从以下新闻中提取命名实体。输出严格JSON数组（不要markdown代码块）：
                    [
                      {"name":"实体名","type":"类型标签"}
                    ]

                    类型标签可选：公司、人物、国家、产品、组织、城市、技术

                    示例：[{"name":"OpenAI","type":"公司"},{"name":"GPT-6","type":"产品"},{"name":"美国","type":"国家"}]

                    新闻内容：{{content}}
                    """;
            default -> "请处理以下内容：{{content}}";
        };
    }
}
