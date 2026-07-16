-- V1.0.4: 新增 ai_languages 列追踪已生成的 AI 语种
-- 支持增量语言回填，避免全量重跑 LLM

ALTER TABLE news_article
    ADD COLUMN ai_languages VARCHAR(200) DEFAULT '' COMMENT '已生成AI内容的语种，逗号分隔，如 zh,en,ja';

-- 回填已有数据：从 ai_result JSON 中提取已有语言 key
-- ai_result 结构: {"zh":{...}, "en":{...}, "category":"...", "entities":[...]}
-- 排除 category/subCategory/entities/_languages 等元数据 key
UPDATE news_article
SET ai_languages = 'zh,en'
WHERE ai_result IS NOT NULL
  AND ai_result != ''
  AND ai_languages = '';
