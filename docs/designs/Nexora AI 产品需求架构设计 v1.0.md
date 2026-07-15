# Nexora AI 产品需求架构设计 v1.0

版本：V1.0
产品名称：**Nexora AI**
核心产品：**Nexora News**
产品定位：**全球智能信息平台**
第一阶段定位：**AI 新闻助手（AI News Assistant）**

---

# 1. 产品概述

## 1.1 产品愿景

Nexora AI 致力于帮助用户：

> 在信息爆炸时代，用 AI 快速发现、理解和追踪全球重要信息。

传统新闻平台解决：

> "发生了什么"

Nexora AI 解决：

> "发生了什么、为什么发生、有什么影响、未来可能如何发展"

---

## 1.2 产品定位

### 第一阶段

Nexora News：

> AI 驱动的全球热点新闻理解平台。

核心能力：

```
新闻采集
    ↓
AI理解
    ↓
热点聚合
    ↓
智能摘要
    ↓
事件分析
    ↓
用户订阅
```

---

## 1.3 产品形态

支持：

### Web

目标：

* 深度阅读
* 搜索
* 个人信息中心
* 企业版入口

技术：

Vue3 + TypeScript

---

### APP

目标：

* 快速浏览
* 推送
* 语音播报
* 个性日报

技术：

建议：

Flutter

原因：

* Android/iOS 一套代码
* 性能接近原生
* 生态成熟

---

# 2. 产品用户画像

## 2.1 普通信息用户

特点：

每天希望快速了解：

* 国内热点
* 国际事件
* 科技新闻

需求：

```
5分钟知道今天世界发生什么
```

---

## 2.2 技术人员

重点关注：

* AI
* 云计算
* 开源
* 大模型

需求：

```
跟踪技术趋势
```

---

## 2.3 投资/研究人员

关注：

* 企业动态
* 行业变化
* 政策影响

需求：

```
发现信息价值
```

---

## 2.4 企业用户（未来）

需求：

* 舆情监控
* 竞争分析
* 行业报告

对应：

Nexora Enterprise

---

# 3. 产品功能架构

整体：

```
Nexora AI
│
├── Nexora News
│
├── Nexora Daily
│
├── Nexora Insight
│
└── Nexora Enterprise
```

第一阶段：

重点建设：

```
Nexora News
```

---

# 4. Nexora News 功能设计

# 4.1 首页热点

页面：

```
首页

今日热点

├── 国内
├── 国际
├── AI科技
├── 财经
└── 我的关注
```

每个热点：

展示：

```
标题

AI摘要

热度

来源数量

发布时间
```

---

# 4.2 新闻详情

结构：

```
新闻详情

标题

来源

时间


AI摘要

--------

核心事实

--------

事件背景

--------

影响分析

--------

相关新闻

--------

原文链接

```

---

# 4.3 AI摘要能力

输入：

新闻正文。

输出：

结构化内容：

```json
{
"title":"",
"summary":"",
"facts":[
],
"background":"",
"impact":"",
"keywords":[]
}
```

---

# 4.4 新闻搜索

支持：

关键词：

```
OpenAI

华为

芯片

新能源
```

搜索：

* 标题
* 正文
* 标签
* 实体

技术：

Elasticsearch。

---

# 4.5 用户关注

用户可以关注：

```
人物

公司

行业

关键词

国家
```

例如：

关注：

```
NVIDIA

OpenAI

人工智能
```

自动生成：

个人新闻流。

---

# 5. Nexora Daily 设计

每日自动生成：

```
我的日报

日期

今日十大热点

AI行业动态

科技趋势

财经变化

关注事件追踪
```

发送方式：

第一阶段：

APP

未来：

* 邮件
* 微信
* 企业微信
* Telegram

---

# 6. AI能力架构

AI 不直接散落业务。

设计：

AI Gateway。

架构：

```
业务服务

   |
   v

AI Gateway

   |
   +---- DeepSeek

   |
   +---- OpenAI

   |
   +---- Qwen

   |
   +---- Claude
```

---

## AI能力模块

## 6.1 摘要

Summary

## 6.2 分类

Category

例如：

```
科技

财经

国际

社会
```

---

## 6.3 实体识别

抽取：

```
公司

人物

国家

产品

事件
```

---

## 6.4 情感分析

例如：

企业舆情：

```
正面

中性

负面
```

---

## 6.5 Embedding

用于：

语义搜索。

推荐。

事件聚类。

---

# 7. 新闻生命周期设计

完整流程：

```
新闻源

↓

Crawler

↓

Raw News

↓

清洗

↓

去重

↓

AI分析

↓

事件聚类

↓

ES索引

↓

用户展示

```

---

# 8. 数据采集体系

来源：

## 国内

包括：

* 新闻网站公开内容
* RSS
* API
* 合作数据

---

## 国际

包括：

* Reuters
* BBC
* CNN
* AP
* 官方媒体

---

采集服务：

```
crawler-service
```

负责：

* 抓取
* 清洗
* 解析
* 调度

---

# 9. 系统总体架构

第一阶段：

采用：

**模块化单体 + 微服务演进设计**

整体：

```
                    Web/App

                       |

                 Gateway

                       |

              Nexora Platform

                       |

 ------------------------------------------------

 |          |          |          |              |

News      AI       Search      User        Crawler


 |

MySQL

Redis

ES

RocketMQ

```

---

# 10. 后端技术选型

## 服务端

Java：

```
JDK 21+

Spring Boot 3

Spring Cloud Alibaba
```

---

## 数据库

主：

MySQL 8

用途：

业务数据。

例如：

用户

新闻元数据

收藏

订阅

---

## 缓存

Redis

用途：

热点：

```
Top新闻

用户Session

访问计数

排行榜
```

---

## 搜索

Elasticsearch

用途：

新闻全文搜索。

---

## MQ

RocketMQ

用途：

异步事件。

例如：

```
新闻采集完成

↓

AI分析

↓

索引建立
```

---

# 11. 服务边界设计

初期：

模块化：

```
nexora-platform

├── news

├── ai

├── crawler

├── search

├── user

└── notification

```

未来拆：

```
news-service

ai-service

crawler-service

search-service

user-service

notification-service
```

---

# 12. 单服务内部架构规范

采用：

Lean DDD。

目录：

```
news-service


controller


manager


service


mapper


entity


dto


client


event


job


consumer


config

```

原则：

## Controller

入口。

可以：

直接调用 Service。

---

## Manager

可选。

只有：

复杂流程才出现。

例如：

```
发布新闻

同步AI分析

建立索引

通知用户
```

---

## Service

单一领域。

例如：

```
NewsService

UserService
```

---

## Mapper

MyBatis-Plus。

---

# 13. 新闻核心数据模型

## News

```
id

title

content

source

language

category

publish_time

status

summary

hot_score

created_time
```

---

## NewsEvent

事件模型：

```
event_id

title

entities

start_time

related_news
```

---

## User

```
id

username

email

language

preferences
```

---

# 14. Redis设计

## 热点排行榜

Key:

```
news:hot:list
```

结构：

ZSet。

---

## 用户关注缓存

```
user:interest:{id}
```

---

## 新闻详情缓存

```
news:detail:{id}
```

---

# 15. Elasticsearch设计

索引：

```
news_index
```

字段：

```
title

content

summary

keywords

entities

embedding

publish_time
```

支持：

* 全文搜索
* 语义搜索
* 相似新闻

---

# 16. 消息设计

RocketMQ Topic:

新闻采集：

```
news-collected
```

AI分析：

```
news-ai-task
```

索引：

```
news-index-task
```

通知：

```
user-notification
```

---

# 17. 推荐系统演进

第一阶段：

规则推荐。

公式：

```
热度

+

用户关注

+

时间衰减

+

领域权重
```

---

第二阶段：

机器学习。

加入：

```
用户行为

点击

停留

收藏

分享
```

---

第三阶段：

AI Agent 推荐。

---

# 18. APP语音能力

未来支持：

国内厂商优先：

推荐：

## 阿里云智能语音

## 腾讯云语音

## 火山引擎语音

能力：

```
新闻文本

↓

TTS

↓

语音播报
```

---

# 19. MVP开发计划

## Phase 1（3个月）

目标：

上线 Nexora News。

功能：

✅ 用户系统

✅ 新闻采集

✅ AI摘要

✅ 首页热点

✅ 新闻详情

✅ 搜索

✅ 收藏

技术：

模块化单体。

---

## Phase 2（3-6个月）

增加：

✅ Nexora Daily

✅ 推送

✅ 用户兴趣

✅ 语音播报

✅ 热点聚类

---

## Phase 3（6-12个月）

增加：

✅ Nexora Insight

✅ AI研究助手

✅ 企业情报

---

# 20. 最终技术架构定位

Nexora AI：

采用：

> **Spring Boot 3 + Java 21 + MyBatis-Plus + MySQL + Redis + Elasticsearch + RocketMQ + AI Gateway + Lean DDD 架构**

设计原则：

1. 不为了架构而架构。
2. 不为了DDD增加无价值层。
3. 不为了微服务提前拆分。
4. 业务复杂度驱动架构复杂度。
5. 从模块化单体平滑演进微服务。

---

## 下一步建议

产品设计完成后，下一阶段应该进入：

**《Nexora AI 技术架构详细设计 v1.0》**

重点设计：

1. Maven 多模块工程结构
2. 服务拆分边界
3. MySQL 表结构
4. Redis Key设计
5. ES Mapping设计
6. RocketMQ消息模型
7. AI Gateway设计
8. 新闻采集架构
9. Docker/K8s部署方案
10. CI/CD流水线

这部分会直接进入开发阶段。
