-- V1.0.9: 用户浏览历史表
-- 记录登录用户点击过的文章，纳入推荐引擎兴趣向量计算
-- 与收藏 (user_favorite) 配合使用，浏览权重 0.5x，收藏权重 1.0x

CREATE TABLE IF NOT EXISTS user_browsing_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    article_id  BIGINT       NOT NULL COMMENT '新闻ID',
    category_id BIGINT       DEFAULT NULL COMMENT '新闻分类ID（冗余，加速兴趣向量计算）',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
    updated_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近浏览时间',
    INDEX idx_user_time (user_id, updated_time DESC),
    UNIQUE KEY uk_user_article (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览历史';
