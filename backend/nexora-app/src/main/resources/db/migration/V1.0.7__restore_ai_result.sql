-- V1.0.7: 紧急恢复 ai_result 列（V1.0.6 过早执行，数据尚未迁移到 i18n 表）
-- 数据迁移完成后再执行 DROP

ALTER TABLE news_article ADD COLUMN ai_result TEXT COMMENT '多语言 AI 分析结果 JSON（迁移过渡期，待 V1.0.8 删除）';
ALTER TABLE news_article ADD COLUMN ai_languages VARCHAR(200) DEFAULT '' COMMENT '已生成AI内容的语种（迁移过渡期，待 V1.0.8 删除）';
