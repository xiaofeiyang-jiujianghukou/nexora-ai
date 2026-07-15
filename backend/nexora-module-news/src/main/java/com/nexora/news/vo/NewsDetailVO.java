package com.nexora.news.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailVO {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private String sourceName;

    private String sourceUrl;

    private String language;

    private String categoryName;

    private Double hotScore;

    private Integer viewCount;

    private Integer likeCount;

    private Integer status;

    // AI 分析
    private AiAnalysisVO aiAnalysis;

    // 标签
    private List<String> tags;

    private LocalDateTime publishTime;

    private LocalDateTime createdTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiAnalysisVO {
        private String summary;
        private String background;
        private String impact;
        private List<String> keywords;
        private String sentiment;
    }
}
