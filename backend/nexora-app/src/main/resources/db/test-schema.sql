-- H2 Test Schema (MySQL Compatibility Mode)
CREATE TABLE IF NOT EXISTS sys_user
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    username     VARCHAR(64)  NOT NULL,
    email        VARCHAR(128),
    password     VARCHAR(255),
    nickname     VARCHAR(64),
    avatar       VARCHAR(512),
    language     VARCHAR(16)  NOT NULL DEFAULT 'zh-CN',
    status       TINYINT      NOT NULL DEFAULT 1,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   TINYINT      NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user(username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email ON sys_user(email);

CREATE TABLE IF NOT EXISTS news_article
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(512) NOT NULL,
    content      CLOB,
    summary      CLOB,
    source_id    BIGINT,
    source_url   VARCHAR(1024),
    language     VARCHAR(16)  NOT NULL DEFAULT 'zh-CN',
    category_id  BIGINT,
    publish_time TIMESTAMP,
    status       TINYINT      NOT NULL DEFAULT 0,
    hot_score    DOUBLE       NOT NULL DEFAULT 0,
    view_count   INT          NOT NULL DEFAULT 0,
    like_count   INT          NOT NULL DEFAULT 0,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS news_source
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(128) NOT NULL,
    country      VARCHAR(64),
    language     VARCHAR(16),
    url          VARCHAR(512),
    type         VARCHAR(32)  NOT NULL DEFAULT 'RSS',
    weight       INT          NOT NULL DEFAULT 50,
    status       TINYINT      NOT NULL DEFAULT 1,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS news_category
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(64) NOT NULL,
    code         VARCHAR(32) NOT NULL,
    parent_id    BIGINT      NOT NULL DEFAULT 0,
    sort         INT         NOT NULL DEFAULT 0,
    status       TINYINT     NOT NULL DEFAULT 1,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_favorite
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT   NOT NULL,
    news_id      BIGINT   NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_favorite ON user_favorite(user_id, news_id);

INSERT INTO news_category (id, name, code, parent_id, sort) VALUES
(1, '国内', 'domestic', 0, 1),
(2, '国际', 'international', 0, 2),
(3, '科技', 'technology', 0, 3),
(4, 'AI', 'ai', 3, 1),
(5, '财经', 'finance', 0, 4);

INSERT INTO news_source (id, name, type, weight) VALUES
(1, 'Reuters', 'RSS', 95),
(2, 'BBC News', 'RSS', 90),
(3, '新华社', 'RSS', 90);

CREATE TABLE IF NOT EXISTS user_subscription
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(32)  NOT NULL,
    target       VARCHAR(128) NOT NULL,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS news_article_i18n
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id   BIGINT       NOT NULL,
    lang_code    VARCHAR(16)  NOT NULL,
    title        VARCHAR(512),
    summary      CLOB,
    facts        CLOB,
    background   CLOB,
    impact       CLOB,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_article_lang ON news_article_i18n(article_id, lang_code);
