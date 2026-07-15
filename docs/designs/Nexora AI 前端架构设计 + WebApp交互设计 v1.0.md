# Nexora AI 前端架构设计 + Web/App交互设计 v1.0

目标：

> 设计一套同时满足 Web、APP、未来企业端扩展的前端体系，并与后端 Spring Boot + OpenAPI 自动生成体系衔接。

核心原则：

1. **Web 和 APP 不共享 UI，但共享业务模型**
2. **API 强约束，通过 OpenAPI 自动生成客户端**
3. **前端不承载业务规则**
4. **用户体验围绕“快速获取信息”设计**

---

# 1. 前端整体架构

整体：

```text
                    Nexora AI Backend


                         |

                  OpenAPI Specification

                         |

        -----------------------------------

        |                                 |

      Web端                              APP端


 Vue3 + TS                          Flutter


```

---

# 2. Web端定位

## Nexora Web

定位：

> 深度阅读 + 搜索 + AI洞察入口。

适合：

* 桌面用户
* 技术人员
* 投资研究人员

---

# 3. Web技术选型

推荐：

```text
Vue 3

+

TypeScript

+

Vite

+

Pinia

+

Vue Router

+

Element Plus

+

Axios

+

ECharts

```

---

原因：

你已有：

* Vue
* TypeScript
* Vite
* OpenAPI Generator

经验。

---

# 4. Web工程结构

项目：

```text
nexora-web


src


├── api

│    └── generated

│

├── assets

│

├── components

│

├── layouts

│

├── pages

│

├── router

│

├── stores

│

├── utils

│

├── hooks

│

├── types

│

└── main.ts

```

---

# 5. API自动生成设计

后端：

Springdoc:

生成：

```text
openapi.json
```

前端：

OpenAPI Generator：

输入：

```text
openapi.json
```

输出：

```text
src/api/generated

```

例如：

生成：

```typescript
NewsApi

UserApi

SearchApi

```

---

调用：

```typescript
newsApi.getNewsDetail(id)
```

---

避免：

手写：

```typescript
axios.get("/news")
```

---

# 6. Web页面设计

整体：

```text
Nexora Web


首页

├── 热点

├── 推荐

├── 分类


新闻详情


搜索


我的关注


日报


AI Insight


个人中心

```

---

# 7. 首页设计

目标：

5秒知道世界发生什么。

布局：

```
-------------------------------------------------

Nexora AI


今日热点


🔥 Top10


-------------------------------------------------

国内热点


新闻卡片


-------------------------------------------------

国际热点


新闻卡片


-------------------------------------------------

AI科技


新闻卡片


-------------------------------------------------

```

---

# 8. 新闻卡片设计

组件：

```text
NewsCard
```

展示：

```
标题

AI摘要

来源

时间

热度

标签

```

---

例如：

```
OpenAI发布新模型


AI摘要：

xxx


来源:
Reuters

热度:
98


#AI
#OpenAI

```

---

# 9. 新闻详情页

结构：

```
标题


来源 时间


----------------


AI摘要


----------------


核心事实


----------------


事件背景


----------------


影响分析


----------------


相关新闻


----------------


原文

```

---

# 10. AI能力入口

页面：

```
AI Insight

```

第一阶段：

简单：

右侧 AI助手。

类似：

ChatGPT。

---

布局：

```
新闻内容


       |

       |

AI助手


问：

为什么重要？


答：

xxx


```

---

# 11. 搜索页面

支持：

普通搜索：

```
OpenAI
```

高级：

```
时间

来源

领域

国家

```

---

未来：

语义搜索：

```
找最近AI芯片突破
```

---

# 12. 用户中心

页面：

```
我的


头像


我的关注


我的收藏


阅读历史


订阅管理


设置

```

---

# 13. APP定位

Nexora APP：

定位：

> 随时获取全球热点。

核心：

移动优先。

---

# 14. APP技术选型

推荐：

Flutter。

原因：

## 一套代码：

```
Android

+

iOS

```

## UI一致

## 性能足够

---

# 15. Flutter工程结构

```text
nexora-app


lib


├── api


├── common


├── config


├── models


├── pages


├── routes


├── services


├── state


├── widgets


└── main.dart

```

---

# 16. APP页面设计

底部 Tab：

```text
--------------------------------

首页

热点

搜索

日报

我的


--------------------------------

```

---

# 17. APP首页

设计：

信息流。

类似：

新闻 + AI。

```
今日热点


🔥 全球TOP10


新闻1


新闻2


新闻3


```

---

# 18. 快速阅读模式

APP核心。

提供：

两种模式：

---

## 新闻模式

原文：

```
标题

正文

图片

来源

```

---

## AI模式

默认：

```
3分钟读懂


发生：

xxx


原因：

xxx


影响：

xxx


```

---

# 19. 语音阅读设计

未来：

Nexora Daily。

流程：

```
新闻文本


|

TTS


|

语音


|

APP播放

```

---

国内优先：

推荐：

第一：

阿里云

第二：

腾讯云

第三：

火山引擎

---

# 20. 前端状态管理

Web：

Pinia。

管理：

```text
userStore

authStore

newsStore

settingStore

```

---

APP：

Flutter：

推荐：

Riverpod。

---

# 21. 登录流程

统一：

JWT。

流程：

```
APP/Web


 |

登录


 |

Auth API


 |

JWT Token


 |

Storage


 |

请求Header


Authorization:

Bearer xxx

```

---

# 22. 前端缓存设计

## Web

浏览器：

```
localStorage

sessionStorage

IndexedDB

```

---

## APP

使用：

```
SharedPreferences

SQLite

```

---

# 23. 新闻流性能设计

首页：

不要一次返回100条。

采用：

分页。

接口：

```
GET

/feed/home?page=1
```

返回：

```json
{
"items":[
],

"hasMore":true
}

```

---

# 24. 图片处理

新闻图片：

不要直接存数据库。

流程：

```
图片

|

对象存储

|

MinIO/OSS

|

CDN

|

前端

```

---

# 25. 文件存储设计

推荐：

第一阶段：

MinIO。

未来：

云：

* 阿里OSS
* 腾讯COS

结构：

```
nexora-media


/news/

 /2026/

   /07/

      xxx.jpg

```

---

# 26. 国际化设计

虽然第一阶段：

中文 + 英文。

但是：

从第一天设计。

前端：

Vue-i18n。

结构：

```
locales

├── zh-CN.json

└── en-US.json

```

---

# 27. 暗黑模式

新闻产品强烈建议支持。

原因：

阅读场景：

晚上。

设计：

CSS变量：

```css
--bg-color

--text-color

```

---

# 28. SEO设计（Web重要）

新闻类产品：

SEO非常重要。

采用：

Vue SSR。

建议：

第二阶段引入：

Nuxt

原因：

搜索引擎收录。

---

# 29. 前后端完整链路

例如：首页打开：

```
用户

 |

Web/App


 |

Gateway


 |

FeedController


 |

FeedManager


 |

NewsService


 |

Redis


 |

MySQL


 |

返回


 |

NewsCard渲染

```

---

# 30. 最终前端技术方案

## Web

```
Vue3

TypeScript

Vite

Pinia

Element Plus

OpenAPI Generator

```

---

## APP

```
Flutter

Dart

Riverpod

OpenAPI Client

```

---

# 31. Nexora 第一阶段完整技术栈确认

现在已经形成：

```
                    Nexora AI


          Web(Vue3)       APP(Flutter)


                  Gateway


              Spring Boot 3


 ------------------------------------------------


 News      AI       Search      User      Crawler


 MySQL    Redis     ES        MQ


                Docker/K8s


```

---

# 下一阶段

目前所有架构设计已经闭环：

✅ 产品
✅ 后端
✅ 数据库
✅ AI Pipeline
✅ API
✅ Web
✅ APP

下一步进入：

# 《Nexora AI 第一版代码工程初始化设计》

会开始真正落地：

1. 创建 Maven 父工程
2. 多模块 pom设计
3. Spring Boot 3初始化
4. MyBatis-Plus配置
5. Redis配置
6. MySQL初始化脚本
7. Docker Compose开发环境
8. Git仓库规范
9. 第一个用户模块实现

也就是从“架构设计”进入“项目开发阶段”。
