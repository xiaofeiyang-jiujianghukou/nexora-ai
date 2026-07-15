package com.nexora.news.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 新闻列表 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSummaryVO {

    private Long id;

    private String title;

    private String summary;

    private String sourceName;

    private String language;

    private String categoryName;

    private Double hotScore;

    private Integer viewCount;

    private List<String> tags;

    /** 多语言 AI 分析结果 JSON (zh/en summary, facts, background, impact) */
    private Map<String, Object> aiResult;

    private LocalDateTime publishTime;
}
