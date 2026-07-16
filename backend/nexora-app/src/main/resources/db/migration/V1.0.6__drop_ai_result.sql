-- V1.0.6: 清理旧列，所有多语言内容已迁移至 news_article_i18n 表
-- 仅在 V1.0.5 数据迁移验证通过后执行

ALTER TABLE news_article DROP COLUMN ai_result;
ALTER TABLE news_article DROP COLUMN ai_languages;
