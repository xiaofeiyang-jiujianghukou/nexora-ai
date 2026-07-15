# Nexora AI API详细设计 + 核心业务流程设计 v1.0

目标：

> 定义 Nexora AI 第一阶段核心业务链路，使后续 Spring Boot 开发可以直接按照接口和流程实现。

设计原则继续遵循：

* Controller 不强制经过 Manager
* 简单业务：Controller → Service
* 复杂业务：Controller → Manager → 多领域 Service/Client
* Service 保持单领域职责
* Manager 只做业务编排
* 外部系统通过 Client 封装

---

# 一、接口整体规划

API 前缀：

```
/api/v1
```

模块：

```
/api/v1/auth        用户认证

/api/v1/user        用户中心

/api/v1/news        新闻

/api/v1/search      搜索

/api/v1/feed        信息流

/api/v1/subscribe   订阅

/api/v1/ai          AI能力

```

---

# 二、认证模块设计

## 1. 用户注册

接口：

```
POST /api/v1/auth/register
```

请求：

```json
{
  "email":"test@nexora.ai",
  "password":"123456",
  "nickname":"小明"
}
```

响应：

```json
{
  "code":0,
  "data":{
      "userId":10001
  }
}
```

---

流程：

```text
Controller

↓

UserService

↓

UserMapper

↓

MySQL

```

无需 Manager。

原因：

只是：

创建用户。

---

# 三、登录设计

## 登录

```
POST /api/v1/auth/login
```

请求：

```json
{
"email":"xxx",
"password":"xxx"
}
```

响应：

```json
{
"token":"xxxx",

"user":{
"id":10001,
"name":"xxx"
}

}
```

---

流程：

```text
AuthController

↓

AuthService

↓

UserService

↓

Redis

↓

JWT

```

---

# 四、新闻首页信息流设计

这是 Nexora 最重要入口。

接口：

```
GET /api/v1/feed/home
```

返回：

```json
{
"hot":[
{
"id":1001,

"title":"OpenAI发布新模型",

"summary":"AI摘要",

"category":"AI",

"hotScore":98
}
],

"domestic":[],

"international":[],

"technology":[]

}
```

---

## 业务流程

这里是否需要 Manager？

答案：

需要。

原因：

首页需要组合：

* 新闻Service
* 热点Service
* 用户兴趣Service

所以：

```text
FeedController

↓

FeedManager

        |
        |
        +---- NewsService

        |
        +---- HotService

        |
        +---- UserInterestService

```

---

# 五、新闻详情设计

接口：

```
GET /api/v1/news/{id}
```

返回：

```json
{
"id":10001,

"title":"xxx",

"content":"",

"aiAnalysis":{

"summary":"",

"background":"",

"impact":""

},

"source":{

"name":"BBC"

}

}
```

---

流程：

```text
NewsController

↓

NewsService

↓

MySQL

↓

AIAnalysisService

```

---

这里有一个设计点：

是否调用 AI Service？

我的建议：

不要。

因为：

AI结果已经生成。

查询：

直接：

```text
NewsService

↓

news_ai_analysis

```

否则：

每次打开新闻：

调用AI。

成本不可控。

---

# 六、新闻采集流程设计

这是内部流程。

不是前端API。

---

## 采集任务

定时：

XXL-JOB。

流程：

```text
XXL-JOB

↓

CrawlerService

↓

Collector

↓

news_raw

↓

RocketMQ

```

---

消息：

Topic：

```
news-collected
```

内容：

```json
{
"rawNewsId":10001
}
```

---

# 七、AI分析流程

Consumer：

监听：

```
news-collected
```

流程：

```text
NewsAIConsumer


↓

AIAnalysisManager


↓

AIService


↓

LLM Client


↓

保存分析结果

↓

发送事件

```

---

这里需要 Manager。

因为：

一个流程：

包含：

1. 获取新闻
2. 调用模型
3. 保存结果
4. 更新状态
5. 通知搜索

---

代码：

```java
@Service
public class NewsAIManager {


private final AIService aiService;

private final NewsService newsService;

private final SearchClient searchClient;


public void analyze(Long newsId){

}


}
```

---

# 八、搜索设计

接口：

```
GET /api/v1/search/news?q=OpenAI
```

响应：

```json
{
"total":100,

"list":[

{
"title":"xxx",

"summary":"xxx"

}

]

}
```

---

流程：

简单：

```text
SearchController

↓

SearchService

↓

Elasticsearch

```

无需Manager。

---

# 九、收藏设计

接口：

收藏：

```
POST /api/v1/news/{id}/favorite
```

取消：

```
DELETE /api/v1/news/{id}/favorite
```

---

流程：

```text
FavoriteController

↓

FavoriteService

↓

Mapper

```

---

# 十、用户订阅设计

例如：

用户订阅：

```
OpenAI
AI
新能源
```

接口：

创建：

```
POST /api/v1/subscribe
```

请求：

```json
{
"type":"TAG",
"target":"AI"
}
```

---

流程：

简单：

```text
SubscribeController

↓

SubscriptionService

```

---

# 十一、每日简报 Daily设计（第二阶段）

接口：

```
GET /api/v1/daily/today
```

返回：

```json
{
"title":"Nexora Daily",

"date":"2026-07-15",

"items":[

]

}
```

---

生成流程：

需要 Manager。

```text
DailyJob


↓

DailyManager


       |

       +---- NewsService

       +---- AIService

       +---- UserInterestService


↓

生成日报

```

---

# 十二、AI Chat能力设计（未来）

Nexora Insight。

接口：

```
POST /api/v1/ai/chat
```

请求：

```json
{
"question":
"英伟达未来趋势如何?"
}
```

流程：

```text
AIController

↓

AIChatManager


      |

      + SearchService

      + KnowledgeService

      + LLMClient


```

---

这里：

必须 Manager。

因为：

AI Agent：

天然多工具编排。

---

# 十三、核心业务流程总览

## 新闻进入

```text
新闻源

↓

Crawler

↓

news_raw

↓

MQ

↓

AI分析

↓

news_article

↓

ES

↓

Feed

↓

用户
```

---

## 用户阅读

```text
APP

↓

Gateway

↓

FeedController

↓

FeedManager

↓

NewsService

↓

Redis/MySQL

```

---

## AI问答

```text
用户

↓

AIController

↓

AIChatManager

↓

Search

↓

Knowledge

↓

LLM

```

---

# 十四、接口权限设计

采用：

JWT。

权限：

```text
游客：

浏览热点


普通用户：

收藏

订阅


会员：

AI增强

日报


企业：

情报分析

```

---

# 十五、异常规范

统一：

```json
{
"code":10001,

"message":"新闻不存在",

"data":null
}
```

---

异常分类：

```
10000 系统

20000 用户

30000 新闻

40000 AI

50000 外部服务

```

---

# 十六、幂等设计

重点：

AI任务。

例如：

MQ重复消费。

解决：

Redis。

Key：

```
ai:task:{newsId}
```

流程：

```text
消费

↓

判断key

↓

存在

return

↓

执行

↓

set key

```

---

# 十七、接口数量规划（MVP）

第一阶段：

约：

30个接口。

其中核心：

| 模块 | 接口数量 |
| -- | ---: |
| 认证 |    5 |
| 用户 |    5 |
| 新闻 |    8 |
| 搜索 |    3 |
| 收藏 |    4 |
| 订阅 |    3 |
| AI |    2 |

---

# 十八、当前阶段架构确认

目前 Nexora 第一阶段：

后端核心：

```text
Spring Boot 3

        |

Controller

        |

Service

        |

Mapper


复杂流程：

Controller

        |

Manager

        |

Service + Client


```

---

# 下一步

现在已经完成：

✅ 产品定位
✅ 技术架构
✅ 数据库设计
✅ 新闻Pipeline
✅ API设计
✅ 业务流程

下一步进入真正编码前最后一个设计：

# 《Nexora AI 前端架构设计 + Web/App交互设计》

包括：

1. Web页面结构
2. APP页面结构
3. Vue3工程设计
4. Flutter工程设计
5. OpenAPI自动生成TS客户端
6. 状态管理
7. 登录鉴权
8. 新闻流交互
9. AI交互体验设计

完成后，我们就可以开始创建：

`nexora-ai` 第一版代码仓库。
