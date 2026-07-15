package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻来源实体 (news_source)
 */
@Data
@TableName("news_source")
public class NewsSourceDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String country;

    private String language;

    private String url;

    private String type;

    private Integer weight;

    private Integer status;

    private LocalDateTime createdTime;
}
