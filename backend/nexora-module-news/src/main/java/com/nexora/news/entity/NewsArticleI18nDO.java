package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻多语言 AI 内容实体 (news_article_i18n)
 * 每种语言独立一行，水平扩展，100 种语言 = 100 行，不是 100 列。
 */
@Data
@TableName("news_article_i18n")
public class NewsArticleI18nDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    /** 语言代码: zh, en, ja, ko, fr, de, ru */
    private String langCode;

    /** AI 生成标题 */
    private String title;

    /** AI 摘要 */
    private String summary;

    /** 核心事实列表 JSON: ["fact1", "fact2"] */
    private String facts;

    /** 事件背景 */
    private String background;

    /** 影响分析 */
    private String impact;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
