# Nexora AI 数据库详细设计 v1.0

版本：V1.0
数据库：MySQL 8.0+
设计目标：

> 支撑 Nexora News 第一阶段业务，同时为未来 Nexora Daily、Nexora Insight、Nexora Enterprise、推荐系统、知识图谱预留扩展能力。

---

# 1. 数据库设计原则

## 1.1 数据分类原则

Nexora 的数据分为四类：

```
业务数据
    |
    ├── 用户
    ├── 新闻
    ├── 收藏
    └── 订阅


内容数据
    |
    ├── 原始新闻
    ├── AI摘要
    ├── 标签
    └── 事件


行为数据
    |
    ├── 阅读
    ├── 点击
    ├── 搜索
    └── 偏好


分析数据
    |
    ├── 热度
    ├── 趋势
    └── 推荐特征
```

---

# 2. 数据库规划

未来微服务拆分：

```
nexora_user

用户领域


nexora_news

新闻领域


nexora_ai

AI分析领域


nexora_search

搜索索引配置


nexora_behavior

用户行为


nexora_report

日报/洞察

```

---

第一阶段为了降低复杂度：

采用：

```
nexora
```

单库。

未来按领域拆分。

---

# 3. 用户模块设计

---

# 3.1 用户表

## sys_user

用途：

用户基础信息。

```sql
CREATE TABLE sys_user
(
    id BIGINT PRIMARY KEY COMMENT '用户ID',

    username VARCHAR(64)
        NOT NULL COMMENT '用户名',

    email VARCHAR(128)
        COMMENT '邮箱',

    password VARCHAR(255)
        COMMENT '密码',

    nickname VARCHAR(64)
        COMMENT '昵称',

    avatar VARCHAR(512)
        COMMENT '头像',

    language VARCHAR(16)
        DEFAULT 'zh-CN'
        COMMENT '语言',

    status TINYINT
        DEFAULT 1
        COMMENT '状态',

    created_time DATETIME,

    updated_time DATETIME

);
```

---

# 3.2 用户第三方登录

未来支持：

* Google
* Apple
* 微信

表：

## sys_user_account

```sql
CREATE TABLE sys_user_account
(
    id BIGINT PRIMARY KEY,

    user_id BIGINT,

    provider VARCHAR(32)
        COMMENT 'google/apple/wechat',

    open_id VARCHAR(128),

    created_time DATETIME

);
```

---

# 4. 新闻核心模型

这是 Nexora 最重要的数据。

---

# 4.1 新闻原始表

## news_article

存储：

采集后的新闻。

```sql
CREATE TABLE news_article
(

id BIGINT PRIMARY KEY,


title VARCHAR(512)
COMMENT '标题',


content LONGTEXT
COMMENT '正文',


summary TEXT
COMMENT 'AI摘要',


source_id BIGINT
COMMENT '来源',


language VARCHAR(16)
COMMENT '语言',


category_id BIGINT
COMMENT '分类',


publish_time DATETIME,


status TINYINT,


hot_score DOUBLE DEFAULT 0,


created_time DATETIME,


updated_time DATETIME


);
```

---

# 设计说明

这里：

不直接存：

AI全部结果。

为什么？

因为：

AI结果会不断变化。

例如：

今天：

GPT-5总结。

未来：

DeepSeek重新分析。

所以：

拆开。

---

# 4.2 新闻来源表

## news_source

例如：

BBC

Reuters

新华社

```sql
CREATE TABLE news_source
(

id BIGINT PRIMARY KEY,


name VARCHAR(128),


country VARCHAR(64),


language VARCHAR(16),


url VARCHAR(512),


type VARCHAR(32),


created_time DATETIME

);
```

---

# 4.3 新闻分类

## news_category

```sql
CREATE TABLE news_category
(

id BIGINT PRIMARY KEY,


name VARCHAR(64),


parent_id BIGINT,


sort INT

);
```

数据：

```
科技

财经

国际

国内

AI

社会
```

---

# 5. AI分析模型设计

AI 是 Nexora 的核心。

---

# 5.1 AI分析结果

## news_ai_analysis

```sql
CREATE TABLE news_ai_analysis
(

id BIGINT PRIMARY KEY,


news_id BIGINT,


model VARCHAR(64)
COMMENT '使用模型',


summary TEXT,


keywords JSON,


entities JSON,


sentiment VARCHAR(32),


impact TEXT,


created_time DATETIME

);
```

---

例如：

数据：

```json
{
"keywords":[
"OpenAI",
"GPT",
"AI"
],

"entities":[
{
"type":"company",
"name":"OpenAI"
}
]
}
```

---

# 为什么 JSON？

因为：

AI输出结构变化快。

例如：

未来增加：

```
risk

trend

prediction

relation
```

不需要频繁修改表结构。

---

# 5.2 Prompt记录

## ai_prompt_record

用于：

模型优化。

```sql
CREATE TABLE ai_prompt_record
(

id BIGINT PRIMARY KEY,


business_type VARCHAR(64),


model VARCHAR(64),


prompt TEXT,


response LONGTEXT,


cost_token INT,


created_time DATETIME

);
```

---

用途：

分析：

* 哪个模型效果好
* token成本
* Prompt优化

---

# 6. 新闻事件模型

这是未来 Insight 的基础。

普通新闻：

```
文章
```

高级：

```
事件
```

例如：

10篇新闻：

```
OpenAI发布GPT-6
```

应该成为：

一个事件。

---

# 6.1 新闻事件

## news_event

```sql
CREATE TABLE news_event
(

id BIGINT PRIMARY KEY,


title VARCHAR(512),


description TEXT,


event_time DATETIME,


importance INT,


created_time DATETIME

);
```

---

# 6.2 新闻事件关联

## news_event_relation

```sql
CREATE TABLE news_event_relation
(

id BIGINT PRIMARY KEY,


event_id BIGINT,


news_id BIGINT

);
```

---

未来：

Insight：

基于：

```
Event
```

生成。

---

# 7. 标签体系

## news_tag

```sql
CREATE TABLE news_tag
(

id BIGINT PRIMARY KEY,


name VARCHAR(64),


type VARCHAR(32)

);
```

例如：

```
AI

芯片

新能源

OpenAI

美国
```

---

## 新闻标签关系

## news_article_tag

```sql
CREATE TABLE news_article_tag
(

id BIGINT PRIMARY KEY,


news_id BIGINT,


tag_id BIGINT

);
```

---

# 8. 用户兴趣体系

未来推荐核心。

---

## user_interest

```sql
CREATE TABLE user_interest
(

id BIGINT PRIMARY KEY,


user_id BIGINT,


tag_id BIGINT,


weight DOUBLE,


created_time DATETIME

);
```

---

例如：

用户：

关注：

AI

数据：

```
tag=AI

weight=0.95
```

---

# 9. 用户行为

推荐系统基础。

---

## user_behavior

```sql
CREATE TABLE user_behavior
(

id BIGINT PRIMARY KEY,


user_id BIGINT,


news_id BIGINT,


behavior_type VARCHAR(32),


duration INT,


created_time DATETIME

);
```

行为：

```
VIEW

LIKE

FAVORITE

SHARE

SEARCH
```

---

# 10. 收藏

## user_favorite

```sql
CREATE TABLE user_favorite
(

id BIGINT PRIMARY KEY,


user_id BIGINT,


news_id BIGINT,


created_time DATETIME

);
```

---

# 11. 订阅体系

例如：

用户订阅：

```
AI

OpenAI

新能源
```

---

## user_subscription

```sql
CREATE TABLE user_subscription
(

id BIGINT PRIMARY KEY,


user_id BIGINT,


type VARCHAR(32),


target VARCHAR(128),


created_time DATETIME

);
```

---

# 12. 热点模型

热点不要直接存在新闻表。

---

## news_hot_score

```sql
CREATE TABLE news_hot_score
(

id BIGINT PRIMARY KEY,


news_id BIGINT,


score DOUBLE,


calculate_time DATETIME

);
```

---

计算：

```
阅读量

+

收藏

+

来源数量

+

时间衰减

+

AI重要度
```

---

# 13. 数据关系总览

```
                 sys_user

                    |

                    |

            user_behavior

                    |

                    |

                news_article

        /          |          \

       /           |           \


 news_source   news_ai     news_event

                  |

                  |

             news_tag


                  |

            user_interest

```

---

# 14. 索引设计

## 新闻查询

```sql
INDEX idx_publish_time
(
publish_time
)
```

---

热点：

```sql
INDEX idx_hot_score
(
hot_score
)
```

---

来源：

```sql
INDEX idx_source
(
source_id
)
```

---

# 15. 分库分表预留

未来大规模：

新闻：

可能：

亿级。

拆：

```
news_article_202601

news_article_202602
```

或者：

ShardingSphere。

---

行为：

天然大表。

未来：

```
user_behavior
```

进入：

ClickHouse。

---

# 16. 第一阶段核心表数量控制

MVP：

不要超过：

15 张。

实际：

```
sys_user

sys_user_account


news_article

news_source

news_category

news_ai_analysis

news_event

news_event_relation

news_tag

news_article_tag


user_behavior

user_favorite

user_interest


news_hot_score
```

共：

14 张。

足够支撑第一阶段。

---

# 17. 下一步设计

数据库确定后，下一步应该设计：

# 《Nexora AI 新闻处理流水线详细设计》

这是整个系统最核心部分。

包括：

1. 新闻采集架构
2. RSS/API/Crawler策略
3. 去重算法
4. 新闻质量评分
5. AI处理Pipeline
6. Prompt设计
7. Embedding设计
8. 新闻事件聚类
9. 热点计算算法
10. RocketMQ消息流转

因为 Nexora 和普通新闻APP最大的区别就在这里：

**不是如何存新闻，而是如何把100万条新闻变成用户真正关心的20条信息。**

下一步建议继续设计这一部分。
