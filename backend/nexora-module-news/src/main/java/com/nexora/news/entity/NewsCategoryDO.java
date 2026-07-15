package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻分类实体 (news_category)
 */
@Data
@TableName("news_category")
public class NewsCategoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdTime;
}
