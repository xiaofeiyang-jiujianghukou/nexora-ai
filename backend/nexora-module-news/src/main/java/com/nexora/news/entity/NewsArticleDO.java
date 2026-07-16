package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻文章实体 (news_article)
 */
@Data
@TableName("news_article")
public class NewsArticleDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String summary;

    private Long sourceId;

    private String sourceUrl;

    private String language;

    private Long categoryId;

    private LocalDateTime publishTime;

    private Integer status;

    private Double hotScore;

    private Integer viewCount;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer isDeleted;
}
