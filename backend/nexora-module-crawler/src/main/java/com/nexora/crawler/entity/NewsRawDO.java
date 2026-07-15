package com.nexora.crawler.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 原始新闻实体 (news_raw)
 */
@Data
@TableName("news_raw")
public class NewsRawDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String source;

    private String sourceType;

    private String title;

    private String content;

    private String url;

    private String author;

    private LocalDateTime publishTime;

    private String hash;

    private String simHash;

    /** 0-待处理 1-已清洗 2-重复 3-已拒绝 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
