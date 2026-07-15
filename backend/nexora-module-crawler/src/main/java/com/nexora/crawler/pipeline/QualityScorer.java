package com.nexora.crawler.pipeline;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 新闻质量评分器
 * 公式: sourceWeight + contentLength + originality + authority
 */
@Component
public class QualityScorer {

    // 来源权重（映射表，可配置化）
    private static final Map<String, Integer> SOURCE_WEIGHTS = Map.ofEntries(
            Map.entry("reuters", 95),
            Map.entry("bbc", 90),
            Map.entry("新华社", 90),
            Map.entry("techcrunch", 80),
            Map.entry("36氪", 75)
    );

    /**
     * 计算综合质量分
     *
     * @param sourceName   来源名称
     * @param contentText  清洗后的文本
     * @param url          原文 URL
     * @return 0-100 的评分
     */
    public int score(String sourceName, String contentText, String url) {
        double sourceScore = getSourceWeight(sourceName);
        double lengthScore = getLengthScore(contentText);
        double authorityScore = getAuthorityScore(url);

        // 加权综合
        return (int) Math.min(100, sourceScore * 0.4 + lengthScore * 0.35 + authorityScore * 0.25);
    }

    /**
     * 获取来源权重 (0-100)
     */
    public int getSourceWeight(String sourceName) {
        if (sourceName == null) return 30;
        return SOURCE_WEIGHTS.getOrDefault(sourceName.toLowerCase(), 40);
    }

    /**
     * 内容长度评分
     * < 200 chars → 0, 200-500 → 50, 500-2000 → 80, > 2000 → 100
     */
    public int getLengthScore(String text) {
        if (text == null) return 0;
        int len = text.length();
        if (len < 200) return (int) (len / 200.0 * 30);
        if (len < 500) return 30 + (int) ((len - 200) / 300.0 * 20);
        if (len < 2000) return 50 + (int) ((len - 500) / 1500.0 * 30);
        return Math.min(100, 80 + (len - 2000) / 100);
    }

    /**
     * 域名权威度评分
     * .gov/.edu → 100, 知名域名 → 80, 其他 → 50
     */
    public int getAuthorityScore(String url) {
        if (url == null) return 30;
        String lower = url.toLowerCase();
        if (lower.contains(".gov") || lower.contains(".edu")) return 100;
        if (lower.contains("reuters.com") || lower.contains("bbc.com")
                || lower.contains("xinhuanet.com")) return 90;
        return 50;
    }
}
