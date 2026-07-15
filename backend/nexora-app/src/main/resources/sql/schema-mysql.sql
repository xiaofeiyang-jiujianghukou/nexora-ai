-- ============================================================
-- Nexora AI 数据库初始化脚本 v1.0
-- Database: nexora
-- Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS nexora
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE nexora;

-- ============================================================
-- 1. 用户域
-- ============================================================

-- 用户基础信息
CREATE TABLE IF NOT EXISTS sys_user
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username     VARCHAR(64)  NOT NULL COMMENT '用户名',
    email        VARCHAR(128) COMMENT '邮箱',
    password     VARCHAR(255) COMMENT '密码（BCrypt加密）',
    nickname     VARCHAR(64) COMMENT '昵称',
    avatar       VARCHAR(512) COMMENT '头像URL',
    language     VARCHAR(16)  NOT NULL DEFAULT 'zh-CN' COMMENT '语言偏好',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email),
    INDEX idx_sys_user_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 第三方登录关联
CREATE TABLE IF NOT EXISTS sys_user_account
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    provider     VARCHAR(32)  NOT NULL COMMENT '第三方平台: google/apple/wechat',
    open_id      VARCHAR(128) NOT NULL COMMENT '第三方OpenID',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_user_account (provider, open_id),
    INDEX idx_sys_user_account_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户第三方登录表';

-- ============================================================
-- 2. 新闻域
-- ============================================================

-- 新闻来源
CREATE TABLE IF NOT EXISTS news_source
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '来源ID',
    name         VARCHAR(128) NOT NULL COMMENT '来源名称',
    country      VARCHAR(64) COMMENT '国家/地区',
    language     VARCHAR(16) COMMENT '语言',
    url          VARCHAR(512) COMMENT '来源URL',
    type         VARCHAR(32)  NOT NULL DEFAULT 'RSS' COMMENT '类型: RSS/API/CRAWLER',
    weight       INT          NOT NULL DEFAULT 50 COMMENT '来源权重 (0-100)',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_news_source_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻来源表';

-- 新闻分类（树形结构）
CREATE TABLE IF NOT EXISTS news_category
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name         VARCHAR(64) NOT NULL COMMENT '分类名称',
    code         VARCHAR(32) NOT NULL COMMENT '分类编码',
    parent_id    BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID, 0表示顶级',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_news_category_code (code),
    INDEX idx_news_category_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻分类表';

-- 新闻标签字典
CREATE TABLE IF NOT EXISTS news_tag
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name         VARCHAR(64) NOT NULL COMMENT '标签名称',
    type         VARCHAR(32) NOT NULL DEFAULT 'KEYWORD' COMMENT '标签类型: KEYWORD/ENTITY/COMPANY/PERSON/COUNTRY',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_news_tag_name_type (name, type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻标签表';

-- 新闻文章主体
CREATE TABLE IF NOT EXISTS news_article
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '新闻ID',
    title        VARCHAR(512) NOT NULL COMMENT '标题',
    content      LONGTEXT COMMENT '正文',
    summary      TEXT COMMENT 'AI摘要（冗余字段，用于列表展示）',
    source_id    BIGINT COMMENT '来源ID',
    source_url   VARCHAR(1024) COMMENT '原文URL',
    language     VARCHAR(16)  NOT NULL DEFAULT 'zh-CN' COMMENT '语言',
    category_id  BIGINT COMMENT '分类ID',
    publish_time DATETIME COMMENT '发布时间',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    hot_score    DOUBLE       NOT NULL DEFAULT 0 COMMENT '热度分',
    view_count   INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
    like_count   INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_news_article_publish_time (publish_time),
    INDEX idx_news_article_hot_score (hot_score),
    INDEX idx_news_article_source (source_id),
    INDEX idx_news_article_category_status (category_id, status),
    INDEX idx_news_article_language_status (language, status, publish_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻文章表';

-- 新闻-标签关联
CREATE TABLE IF NOT EXISTS news_article_tag
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    news_id BIGINT NOT NULL COMMENT '新闻ID',
    tag_id  BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_news_article_tag (news_id, tag_id),
    INDEX idx_news_article_tag_tag (tag_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻标签关联表';

-- ============================================================
-- 3. AI 分析域
-- ============================================================

-- AI 分析结果（JSON 灵活字段）
CREATE TABLE IF NOT EXISTS news_ai_analysis
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析ID',
    news_id      BIGINT       NOT NULL COMMENT '新闻ID',
    model        VARCHAR(64)  NOT NULL COMMENT '使用的AI模型',
    summary      TEXT COMMENT 'AI摘要',
    facts        JSON COMMENT '核心事实列表',
    background   TEXT COMMENT '事件背景',
    impact       TEXT COMMENT '影响分析',
    keywords     JSON COMMENT '关键词 (JSON数组)',
    entities     JSON COMMENT '实体列表 (JSON数组)',
    category     VARCHAR(64) COMMENT 'AI分类结果',
    sub_category VARCHAR(64) COMMENT 'AI子分类',
    sentiment    VARCHAR(32) COMMENT '情感: POSITIVE/NEUTRAL/NEGATIVE',
    cost_token   INT         NOT NULL DEFAULT 0 COMMENT 'Token消耗',
    status       TINYINT     NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-失败',
    error_msg    TEXT COMMENT '失败原因',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_news_ai_analysis_news_model (news_id, model),
    INDEX idx_news_ai_analysis_news (news_id),
    INDEX idx_news_ai_analysis_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI分析结果表';

-- Prompt 调用记录（成本监控）
CREATE TABLE IF NOT EXISTS ai_prompt_record
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    business_type VARCHAR(64)  NOT NULL COMMENT '业务类型: SUMMARY/CLASSIFY/ENTITY_EXTRACT',
    model         VARCHAR(64)  NOT NULL COMMENT '使用模型',
    prompt        TEXT         NOT NULL COMMENT 'Prompt内容',
    response      LONGTEXT     NOT NULL COMMENT 'AI响应',
    cost_token    INT          NOT NULL DEFAULT 0 COMMENT 'Token消耗',
    duration_ms   INT          NOT NULL DEFAULT 0 COMMENT '耗时(ms)',
    success       TINYINT      NOT NULL DEFAULT 1 COMMENT '是否成功',
    error_msg     TEXT COMMENT '错误信息',
    created_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ai_prompt_record_business (business_type, created_time),
    INDEX idx_ai_prompt_record_model (model, created_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Prompt调用记录表';

-- ============================================================
-- 4. 事件域（Phase 2 核心，Phase 1 建表预留）
-- ============================================================

CREATE TABLE IF NOT EXISTS news_event
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    title        VARCHAR(512) NOT NULL COMMENT '事件标题',
    description  TEXT COMMENT '事件描述',
    event_time   DATETIME COMMENT '事件时间',
    importance   INT          NOT NULL DEFAULT 0 COMMENT '重要度',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_news_event_importance (importance),
    INDEX idx_news_event_time (event_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻事件表';

CREATE TABLE IF NOT EXISTS news_event_relation
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    event_id     BIGINT   NOT NULL COMMENT '事件ID',
    news_id      BIGINT   NOT NULL COMMENT '新闻ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_event_news (event_id, news_id),
    INDEX idx_event_relation_news (news_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='事件新闻关联表';

-- ============================================================
-- 5. 用户行为域
-- ============================================================

CREATE TABLE IF NOT EXISTS user_favorite
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id      BIGINT   NOT NULL COMMENT '用户ID',
    news_id      BIGINT   NOT NULL COMMENT '新闻ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_favorite (user_id, news_id),
    INDEX idx_user_favorite_user (user_id, created_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户收藏表';

CREATE TABLE IF NOT EXISTS user_subscription
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订阅ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    type         VARCHAR(32)  NOT NULL COMMENT '订阅类型: TAG/ENTITY/CATEGORY/COMPANY',
    target       VARCHAR(128) NOT NULL COMMENT '订阅目标',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_subscription_user (user_id, type),
    UNIQUE KEY uk_user_subscription (user_id, type, target)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户订阅表';

CREATE TABLE IF NOT EXISTS user_interest
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '兴趣ID',
    user_id      BIGINT   NOT NULL COMMENT '用户ID',
    tag_id       BIGINT   NOT NULL COMMENT '标签ID',
    weight       DOUBLE   NOT NULL DEFAULT 0 COMMENT '兴趣权重',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_interest (user_id, tag_id),
    INDEX idx_user_interest_weight (user_id, weight DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户兴趣表';

CREATE TABLE IF NOT EXISTS user_behavior
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    news_id       BIGINT       NOT NULL COMMENT '新闻ID',
    behavior_type VARCHAR(32)  NOT NULL COMMENT '行为类型: VIEW/LIKE/FAVORITE/SHARE/SEARCH',
    duration      INT          NOT NULL DEFAULT 0 COMMENT '停留时长(秒)',
    created_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_behavior_user (user_id, created_time),
    INDEX idx_user_behavior_news (news_id, behavior_type),
    INDEX idx_user_behavior_type_time (behavior_type, created_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户行为表';

-- ============================================================
-- 6. 热点域
-- ============================================================

CREATE TABLE IF NOT EXISTS news_hot_score
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    news_id        BIGINT   NOT NULL COMMENT '新闻ID',
    score          DOUBLE   NOT NULL DEFAULT 0 COMMENT '热度分',
    view_weight    DOUBLE   NOT NULL DEFAULT 0 COMMENT '阅读权重',
    favorite_weight DOUBLE  NOT NULL DEFAULT 0 COMMENT '收藏权重',
    time_decay     DOUBLE   NOT NULL DEFAULT 0 COMMENT '时间衰减因子',
    calculate_time DATETIME NOT NULL COMMENT '计算时间',
    created_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_news_hot_score_news (news_id, calculate_time),
    INDEX idx_news_hot_score_time (calculate_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='新闻热度分表';

-- ============================================================
-- 7. 初始数据: 新闻分类
-- ============================================================

INSERT INTO news_category (id, name, code, parent_id, sort) VALUES
(1, '国内', 'domestic', 0, 1),
(2, '国际', 'international', 0, 2),
(3, '科技', 'technology', 0, 3),
(4, 'AI', 'ai', 3, 1),
(5, '财经', 'finance', 0, 4),
(6, '社会', 'society', 0, 5),
(7, '体育', 'sports', 0, 6);

-- ============================================================
-- 8. 初始数据: 新闻来源
-- ============================================================

INSERT INTO news_source (id, name, country, language, url, type, weight) VALUES
(1, 'Reuters', 'UK', 'en', 'https://www.reuters.com', 'RSS', 95),
(2, 'BBC News', 'UK', 'en', 'https://www.bbc.com', 'RSS', 90),
(3, '新华社', 'CN', 'zh-CN', 'https://www.xinhuanet.com', 'RSS', 90),
(4, 'TechCrunch', 'US', 'en', 'https://techcrunch.com', 'RSS', 80),
(5, '36氪', 'CN', 'zh-CN', 'https://36kr.com', 'RSS', 75);
