好的，我们继续进入下一阶段：

# Nexora AI 技术架构详细设计 v1.0

目标：

> 基于前面确定的产品定位和 Lean DDD 架构原则，设计一套可以从模块化单体平滑演进到微服务的企业级技术架构。

核心技术路线：

```
Java 21
Spring Boot 3.x
Spring Cloud Alibaba
MyBatis-Plus
MySQL 8
Redis
Elasticsearch
RocketMQ
Docker
Kubernetes/K3s
AI Gateway
```

---

# 1. 整体技术架构演进路线

我们不采用：

> 第一天 20 个微服务

而采用：

> 模块化单体 → 服务拆分 → 云原生微服务

---

## Phase 1：模块化单体

开发阶段：

```
nexora-platform

├── nexora-web
├── nexora-news
├── nexora-ai
├── nexora-crawler
├── nexora-search
├── nexora-user
└── nexora-common
```

部署：

```
                 Nginx

                   |

             Spring Boot

                   |

    --------------------------------

    MySQL Redis ES RocketMQ
```

优势：

* 开发效率高
* 调试简单
* 部署简单
* 事务简单

---

## Phase 2：热点服务拆分

当出现压力：

拆：

```
nexora-platform

        |

 ----------------------------

 news-service

 ai-service

 crawler-service

 search-service
```

---

## Phase 3：完整微服务

最终：

```
                    Gateway

                       |

 ------------------------------------------------

 |          |          |          |             |

User      News       AI       Search       Crawler


 |

Notification

 |

Recommendation

```

---

# 2. Maven工程设计

采用：

Maven Multi Module。

根工程：

```
nexora-parent
```

结构：

```
nexora

├── pom.xml

├── nexora-common

├── nexora-api

├── nexora-user

├── nexora-news

├── nexora-ai

├── nexora-search

├── nexora-crawler

└── nexora-app
```

---

# 3. 模块职责设计

## nexora-common

公共基础。

包含：

```
common

├── exception

├── response

├── constants

├── utils

├── enums

├── validator
```

例如：

统一返回：

```java
public class Result<T>{

    private Integer code;

    private String message;

    private T data;

}
```

---

# 4. nexora-api设计

这个模块非常重要。

它是未来：

> 微服务二方包。

例如：

```
nexora-news-api
```

里面：

```
api

├── dto

├── request

├── response

├── enums

├── event

└── client
```

例如：

新闻查询：

```java
public interface NewsApi {


    NewsDTO getNews(Long id);


}
```

---

Feign：

未来：

```
news-service

提供

NewsApi


user-service

依赖

news-api
```

---

# 5. 新闻模块设计

目录：

```
nexora-news


controller


manager


service


mapper


entity


dto


event


client


config
```

---

## Controller

负责：

HTTP。

例如：

```
NewsController
```

---

## Manager

可选。

例如：

新闻发布：

```
NewsPublishManager
```

流程：

```
发布新闻

↓

NewsService

↓

AiClient

↓

SearchClient

↓

NotificationClient
```

---

## Service

单领域。

例如：

```
NewsService
```

职责：

新闻业务规则。

例如：

```java
publish(newsId)
```

---

## Mapper

MyBatis Plus。

例如：

```java
@Mapper
public interface NewsMapper
        extends BaseMapper<NewsDO>{

}
```

---

# 6. AI服务设计

这是 Nexora 的核心。

目录：

```
nexora-ai


controller


service


provider


prompt


memory


tool


model

```

---

## AI Provider设计

这里使用接口。

因为：

真实存在多个实现。

例如：

```java
public interface LLMProvider {


    ChatResponse chat(
        ChatRequest request
    );


}
```

实现：

```
DeepSeekProvider

OpenAIProvider

QwenProvider

ClaudeProvider

```

---

## Prompt管理

不要写死：

错误：

```java
String prompt="总结新闻";
```

设计：

```
prompt


news-summary.yaml

news-analysis.yaml

daily-report.yaml

```

例如：

```yaml
role:
  system:
    你是新闻分析专家


task:
  请总结以下新闻
```

---

# 7. Crawler服务设计

负责：

新闻采集。

结构：

```
crawler


scheduler


collector


parser


cleaner


storage

```

---

## Collector

接口：

```java
public interface NewsCollector{


 List<RawNews> collect();

}
```

实现：

```
BBCCollector

ReutersCollector

RSSCollector

DomesticCollector

```

这里：

一接口多实现。

符合之前原则。

---

# 8. Search服务设计

负责：

ES。

结构：

```
search


controller


service


repository


index


query

```

---

能力：

全文搜索：

```
keyword

↓

ES

↓

News
```

语义搜索：

未来：

```
Embedding

↓

Vector Search

```

---

# 9. User服务设计

负责：

用户体系。

包括：

```
注册

登录

权限

订阅

关注
```

---

# 10. 数据存储设计

## MySQL

负责：

强一致业务。

例如：

用户：

```
user
```

新闻：

```
news
```

收藏：

```
favorite
```

---

## Redis

负责：

热点。

例如：

```
news:hot

user:session

news:view
```

---

## Elasticsearch

负责：

搜索。

---

## RocketMQ

负责：

异步。

---

# 11. 新闻处理链路设计

这是核心流程。

## 新闻进入系统

```
Crawler


↓

news_raw


↓

RocketMQ

topic:

news-collected


↓

AI Service


↓

生成:

summary

keywords

entities


↓

RocketMQ


↓

Search Service


↓

ES


↓

用户查询

```

---

# 12. 核心事件设计

采用事件驱动。

例如：

新闻采集完成：

```java
NewsCollectedEvent
```

内容：

```json
{

newsId:1001,

source:"BBC",

url:"xxx"

}
```

---

AI分析完成：

```java
NewsAnalyzedEvent
```

---

索引完成：

```java
NewsIndexedEvent
```

---

# 13. Gateway设计

采用：

Spring Cloud Gateway。

职责：

```
认证

限流

路由

日志

灰度
```

架构：

```
APP/Web

 |

Gateway

 |

Service
```

---

# 14. 配置中心

Spring Cloud Alibaba:

Nacos。

管理：

```
database

redis

rocketmq

ai key

```

---

# 15. 服务注册发现

未来微服务：

Nacos。

例如：

```
news-service

注册

↓

Nacos


gateway发现

```

---

# 16. 安全设计

## 用户认证

推荐：

Keycloak。

流程：

```
用户

↓

Keycloak

↓

JWT

↓

Gateway

↓

Service

```

---

## 数据权限

企业版：

增加：

RBAC。

---

# 17. Docker部署设计

每个服务：

独立镜像。

例如：

```
nexora-news:1.0


nexora-ai:1.0
```

基础：

```
eclipse-temurin:21-alpine
```

---

# 18. Kubernetes设计

最终：

```
Namespace:

nexora


Deployment:

news

ai

search


Service:

ClusterIP


Ingress:

Gateway
```

---

# 19. 可观测性

采用：

你熟悉的体系：

```
Prometheus

Grafana

SkyWalking

ELK
```

链路：

```
Request

↓

Gateway

↓

Service

↓

MySQL

↓

MQ

```

---

# 20. 当前推荐最终架构图

```
                         Web
                          |
                         APP
                          |
                          |
                   Spring Gateway
                          |
        --------------------------------
        |
        |
 -------------------------------------------------
 |          |          |          |              |
News       AI       Search     User        Crawler
 |          |          |          |              |
 |          |          |          |
MySQL     LLM        ES        MySQL
 |
Redis

 |
RocketMQ


```

---

# 下一阶段建议

接下来进入真正开发前最重要的一步：

## 《Nexora AI 数据库详细设计 v1.0》

包括：

1. 用户表设计
2. 新闻核心表
3. 新闻来源表
4. 新闻事件表
5. AI分析结果表
6. 标签体系
7. 用户兴趣模型
8. 收藏历史
9. 阅读行为
10. 未来推荐系统数据准备

这个阶段会直接决定后续：

* AI总结效果
* 搜索能力
* 推荐能力
* 数据扩展能力

我建议下一步直接设计数据库。
