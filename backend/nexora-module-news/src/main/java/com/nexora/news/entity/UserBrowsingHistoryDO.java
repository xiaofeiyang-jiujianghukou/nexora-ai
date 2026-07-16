package com.nexora.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户浏览历史实体 (user_browsing_history)
 */
@Data
@TableName("user_browsing_history")
public class UserBrowsingHistoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long articleId;

    private Long categoryId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
