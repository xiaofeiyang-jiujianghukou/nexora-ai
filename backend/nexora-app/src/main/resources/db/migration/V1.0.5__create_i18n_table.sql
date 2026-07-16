-- V1.0.5: 创建 news_article_i18n 关联表，每种语言独立一行
-- 替代原来的 ai_result JSON 列，支持水平扩展

CREATE TABLE news_article_i18n (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT NOT NULL,
    lang_code   VARCHAR(10) NOT NULL COMMENT '语言代码: zh, en, ja, ko, fr, de, ru',
    title       VARCHAR(500) COMMENT 'AI生成标题',
    summary     TEXT COMMENT 'AI摘要',
    facts       JSON COMMENT '核心事实列表',
    background  TEXT COMMENT '事件背景',
    impact      TEXT COMMENT '影响分析',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_article_lang (article_id, lang_code),
    INDEX idx_lang_code (lang_code),
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT '新闻多语言AI内容表';
