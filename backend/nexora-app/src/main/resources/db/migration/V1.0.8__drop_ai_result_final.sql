-- V1.0.8: 最终删除 ai_result / ai_languages 列
-- 数据已全部迁移到 news_article_i18n 表，旧列不再需要
-- 执行前确认: SELECT COUNT(*) FROM news_article_i18n WHERE lang_code IN ('zh','en') > 0

ALTER TABLE news_article DROP COLUMN ai_result;
ALTER TABLE news_article DROP COLUMN ai_languages;
