-- ============================================================
-- Flyway Migration V1.0.1 — 插入 RSS 新闻源种子数据
-- ============================================================

INSERT IGNORE INTO news_source (name, country, language, url, type, weight, status, created_time) VALUES
-- 英文科技媒体（热门）
('TechCrunch', 'US', 'en', 'https://techcrunch.com/feed/', 'RSS', 80, 1, NOW()),
('The Verge', 'US', 'en', 'https://www.theverge.com/rss/index.xml', 'RSS', 75, 1, NOW()),
('Ars Technica', 'US', 'en', 'https://feeds.arstechnica.com/arstechnica/index', 'RSS', 70, 1, NOW()),
('Wired', 'US', 'en', 'https://www.wired.com/feed/rss', 'RSS', 75, 1, NOW()),
('Hacker News', 'US', 'en', 'https://hnrss.org/frontpage', 'RSS', 65, 1, NOW()),

-- 英文综合新闻（高权威度）
('BBC News', 'UK', 'en', 'https://feeds.bbci.co.uk/news/rss.xml', 'RSS', 90, 1, NOW()),
('Reuters', 'UK', 'en', 'https://www.reutersagency.com/feed/', 'RSS', 95, 1, NOW()),
('The Guardian', 'UK', 'en', 'https://www.theguardian.com/international/rss', 'RSS', 80, 1, NOW()),

-- 中文科技媒体
('36氪', 'CN', 'zh-CN', 'https://36kr.com/feed', 'RSS', 75, 1, NOW()),
('少数派', 'CN', 'zh-CN', 'https://sspai.com/feed', 'RSS', 60, 1, NOW()),
('InfoQ 中文', 'CN', 'zh-CN', 'https://www.infoq.cn/feed', 'RSS', 65, 1, NOW());
