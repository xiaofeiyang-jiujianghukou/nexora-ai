package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体 (user_favorite)
 */
@Data
@TableName("user_favorite")
public class UserFavoriteDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long newsId;

    private LocalDateTime createdTime;
}
