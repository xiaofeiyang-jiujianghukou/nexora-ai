# Nexora AI 微服务详细设计与工程落地方案 v1.0

目标：

> 将 Nexora AI 从产品设计落地为一个可持续开发、可扩展、可演进的企业级项目。

本阶段重点解决：

* 工程结构
* 服务边界
* API规范
* 代码规范
* 中间件使用规范
* 本地开发环境
* 未来微服务演进路线

---

# 1. 总体工程策略

我们最终采用：

> **模块化单体优先，微服务平滑演进**

原因：

Nexora 第一阶段：

核心复杂度在：

* 数据采集
* AI处理
* 新闻理解

而不是：

服务数量。

所以：

第一阶段：

一个 Spring Boot 应用。

但是：

代码边界按照未来微服务设计。

---

# 2. Git仓库设计

推荐：

## Monorepo

仓库：

```
nexora-ai
```

结构：

```
nexora-ai

├── backend
│
├── frontend-web
│
├── app
│
├── deploy
│
└── docs
```

---

## backend

```
backend

├── pom.xml

├── nexora-common

├── nexora-api

├── nexora-app

├── nexora-module-news

├── nexora-module-ai

├── nexora-module-search

├── nexora-module-user

├── nexora-module-crawler

└── nexora-module-notification
```

---

# 3. Maven模块设计

## 根pom

```
nexora-backend
```

职责：

统一：

* JDK版本
* Spring Boot版本
* 依赖版本
* 插件版本

例如：

```xml
<properties>

<java.version>21</java.version>

<spring.boot.version>3.4.x</spring.boot.version>

<mybatis.plus.version>3.5.x</mybatis.plus.version>

</properties>
```

---

# 4. 模块职责

## nexora-common

公共基础。

包含：

```
common

├── exception

├── response

├── enums

├── utils

├── constants

└── json
```

---

例如：

统一异常：

```java
public class BusinessException 
        extends RuntimeException {


    private Integer code;

}
```

---

统一返回：

```java
public class Result<T>{


    private Integer code;


    private String message;


    private T data;

}
```

---

# 5. nexora-api

未来微服务通信接口。

例如：

新闻服务：

```
nexora-news-api
```

提供：

DTO：

```
NewsDTO

NewsQueryDTO

NewsResponse
```

事件：

```
NewsCreatedEvent
```

Feign接口：

```
NewsClient
```

---

注意：

第一阶段：

模块直接依赖。

未来：

拆服务后：

变成二方包。

---

# 6. nexora-app

启动模块。

唯一 SpringBootApplication。

结构：

```
nexora-app


NexoraApplication.java


config

security

swagger

```

---

其它模块：

不允许：

```
@SpringBootApplication
```

---

# 7. 新闻模块设计

## nexora-module-news

目录：

```
news

├── controller

├── manager

├── service

├── mapper

├── entity

├── dto

├── vo

├── event

├── consumer

└── client

```

---

# 8. Controller规范

职责：

只有：

* 参数接收
* 参数校验
* 调用业务
* 返回结果

禁止：

❌ SQL

❌ 业务判断

❌ MQ发送

---

示例：

```java
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {


private final NewsService newsService;



@GetMapping("/{id}")
public Result<NewsVO> detail(
@PathVariable Long id
){

return Result.success(
newsService.detail(id)
);

}

}
```

---

# 9. Service规范

Service：

一个领域。

例如：

```
NewsService
```

负责：

* 新闻状态
* 新闻规则
* 新闻查询

---

例如：

```java
public interface NewsService {


    NewsVO detail(Long id);


    void publish(Long id);


}
```

---

实现：

```
NewsServiceImpl
```

---

# 10. Manager规范

非常严格控制。

只有：

复杂流程。

例如：

新闻发布：

```
NewsPublishManager
```

代码：

```java
@Service
@RequiredArgsConstructor
public class NewsPublishManager {


private final NewsService newsService;


private final AIClient aiClient;


private final SearchClient searchClient;



public void publish(Long id){


newsService.publish(id);


aiClient.analyse(id);


searchClient.index(id);


}

}
```

---

# 11. Mapper规范

使用：

MyBatis-Plus。

例如：

```java
@Mapper
public interface NewsMapper
        extends BaseMapper<NewsDO>{


}
```

---

DO：

数据库对象。

```
entity

↓

NewsDO
```

---

VO：

返回前端。

```
NewsVO
```

---

DTO：

模块通信。

```
NewsDTO
```

---

# 12. 数据访问规范

禁止：

Controller直接Mapper。

禁止：

Service写复杂SQL。

---

复杂查询：

Mapper XML。

例如：

```
NewsMapper.xml
```

---

简单CRUD：

BaseMapper。

---

# 13. Redis规范

统一：

Key管理。

禁止：

业务随便写：

```java
redisTemplate.opsForValue()
```

---

设计：

```
RedisKeyConstants
```

例如：

```java
public interface RedisKeys {


String NEWS_DETAIL =
"news:detail:%s";


String HOT_NEWS =
"news:hot";


}
```

---

# 14. Redis使用场景

## 新闻详情缓存

```
news:detail:{id}
```

TTL：

30分钟。

---

## 热点榜

ZSet：

```
news:hot
```

score：

热度。

---

## 用户Session

```
user:session:{id}
```

---

# 15. RocketMQ规范

采用：

事件驱动。

---

Producer：

```
event publisher
```

---

例如：

新闻采集完成：

```java
NewsCollectedEvent
```

发送：

Topic：

```
news-collected
```

---

Consumer：

AI模块：

监听：

```
news-collected
```

执行：

```
AI分析
```

---

# 16. API规范

采用：

RESTful。

---

新闻：

```
GET

/api/news/{id}
```

列表：

```
GET

/api/news/list
```

搜索：

```
GET

/api/search?q=AI
```

---

# 17. OpenAPI规范

使用：

springdoc-openapi。

生成：

```
OpenAPI.json
```

未来：

自动生成：

Vue TypeScript Client。

符合你之前项目习惯。

---

# 18. 前端架构

## Web

推荐：

```
Vue3

TypeScript

Vite

Pinia

Element Plus
```

---

目录：

```
frontend


src

├── api

├── views

├── components

├── stores

└── router

```

---

# 19. APP架构

推荐：

Flutter。

结构：

```
lib

├── api

├── pages

├── widgets

├── model

└── state

```

---

# 20. 本地开发环境

Docker Compose。

```
nexora-dev


mysql

redis

elasticsearch

rocketmq

nacos

minio

```

---

# 21. 开发启动流程

开发者：

启动：

```
docker compose up
```

启动：

```
SpringBoot
```

访问：

```
localhost:8080
```

---

# 22. CI/CD设计

Git流程：

```
feature

↓

develop

↓

release

↓

main
```

---

流水线：

```
Git Push

↓

GitLab CI

↓

Maven Test

↓

Docker Build

↓

Docker Registry

↓

K8s Deploy

```

---

# 23. 生产部署

K8s：

```
namespace nexora


Deployment:

nexora-app


Service:

ClusterIP


Ingress:

gateway
```

---

# 24. 监控体系

采用：

你熟悉的：

```
Prometheus

Grafana

SkyWalking

ELK
```

---

指标：

业务：

```
新闻数量

AI成功率

摘要耗时

搜索耗时
```

系统：

```
CPU

Memory

GC

MQ堆积
```

---

# 25. 最终第一阶段架构

```
                 Web
                  |
                 APP
                  |
             Gateway
                  |
            nexora-app


 ------------------------------------------------

 |          |          |          |              |

 News       AI      Search      User        Crawler


 |

MySQL

Redis

ES

RocketMQ


```

---

# 26. 第一阶段开发顺序（非常重要）

不要同时开发所有模块。

推荐：

## Sprint 1

基础工程：

* Maven
* Spring Boot
* MySQL
* Redis
* 用户

---

## Sprint 2

新闻核心：

* 新闻表
* 新闻CRUD
* 新闻详情

---

## Sprint 3

采集：

* RSS
* Crawler
* Raw News

---

## Sprint 4

AI：

* AI Gateway
* 摘要
* 分类

---

## Sprint 5

搜索：

* ES
* 分词
* 搜索

---

## Sprint 6

首页：

* 热点
* 推荐
* APP

---

# 当前 Nexora 架构已经具备：

✅ 产品定位
✅ 业务边界
✅ 数据模型
✅ 技术架构
✅ 工程规范
✅ 开发路线

下一步我建议进入：

# 《Nexora AI API详细设计 + 核心业务流程设计》

重点：

1. 用户登录流程
2. 新闻采集流程
3. AI摘要流程
4. 新闻发布流程
5. 首页热点生成流程
6. 搜索流程
7. 收藏/订阅流程
8. APP接口设计

这一步之后，就可以开始创建 Spring Boot 项目骨架。
