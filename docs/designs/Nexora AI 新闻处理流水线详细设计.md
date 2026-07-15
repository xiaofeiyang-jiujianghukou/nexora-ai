# Nexora AI 新闻处理流水线详细设计 v1.0

这是 Nexora AI 最核心的技术能力。

传统新闻系统：

```text
新闻源
  |
  v
数据库
  |
  v
展示
```

只能解决：

> 信息搬运。

---

Nexora：

目标：

> 从海量信息中提取高价值事件，并帮助用户理解。

整体 Pipeline：

```text
                 新闻来源

                    |

          +-------------------+

          |                   |

        RSS/API            Crawler

          |                   |

          +---------+---------+

                    |

                    v

              Raw News Pool

                    |

                    v

              新闻清洗 Pipeline

                    |

                    v

              新闻理解 Pipeline

                    |

                    v

             Event Intelligence

                    |

                    v

              Search / Feed

                    |

                    v

                用户端
```

---

# 1. 新闻来源层（News Source Layer）

## 目标

持续获取全球信息。

来源分为：

---

## 1.1 RSS

优先级：

★★★★★

优点：

* 合规
* 稳定
* 成本低
* 结构化

例如：

```text
BBC RSS

Reuters RSS

官方博客 RSS
```

---

## 1.2 News API

例如：

* 新闻供应商 API
* 企业合作 API

优势：

结构标准：

```json
{
"title":"",
"description":"",
"url":"",
"time":""
}
```

---

## 1.3 Crawler

用于：

没有 API 的网站。

架构：

```text
crawler-service

      |

Collector

      |

Parser

      |

Normalizer

```

---

# 2. Crawler服务设计

目录：

```text
crawler-service


collector

parser

scheduler

pipeline

storage

```

---

## Collector接口

符合：

一接口多实现。

```java
public interface NewsCollector {


    List<RawNews> collect();


}
```

实现：

```text
BBCCollector

ReutersCollector

GovCollector

TechCollector

```

---

## Scheduler

负责：

定时任务。

技术：

推荐：

XXL-JOB

例如：

```text
BBC

每10分钟


财经

每5分钟


AI博客

每30分钟
```

---

# 3. 原始新闻模型

Raw News：

不是业务新闻。

区别：

Raw：

采集结果。

News：

处理后的产品数据。

---

表：

news_raw

字段：

```text
id

source

title

content

url

publish_time

hash

created_time

```

---

# 4. 新闻清洗 Pipeline

流程：

```text
Raw News

 |

 v

Content Cleaner

 |

 v

Duplicate Detector

 |

 v

Quality Scorer

 |

 v

News Article

```

---

# 5. 内容清洗

处理：

## HTML去除

例如：

```html
<p>新闻内容</p>
```

变：

```text
新闻内容
```

---

## 广告过滤

去掉：

```text
点击购买

相关推荐

猜你喜欢
```

---

## 内容标准化

统一：

```text
标题

正文

作者

时间

来源

```

---

# 6. 新闻去重设计（非常重要）

同一个事件：

可能：

100家媒体报道。

例如：

```text
OpenAI发布新模型
```

新闻：

```text
BBC

Reuters

新华社

新浪

腾讯

```

不能显示100条。

---

## 第一层：URL Hash

简单。

```text
url

↓

hash

↓

判断
```

---

## 第二层：文本指纹

计算：

SimHash。

流程：

```text
新闻正文

↓

分词

↓

SimHash

↓

相似度比较

```

---

## 第三层：Embedding相似度

未来：

```text
新闻文本

↓

Embedding

↓

Vector DB

↓

相似新闻
```

---

最终：

三级去重。

---

# 7. 新闻质量评分

不是所有新闻都进入首页。

建立：

Quality Score。

公式：

```
Quality Score =

SourceWeight

+

ContentLength

+

Originality

+

Authority

```

---

例如：

来源权重：

```text
Reuters        95

BBC            90

新华社          90

普通博客        40

```

---

# 8. AI理解 Pipeline

这是 Nexora核心。

输入：

```text
News Article
```

输出：

```text
AI Analysis
```

---

流程：

```text
News

 |

 v

AI Classify

 |

 v

AI Summary

 |

 v

Entity Extract

 |

 v

Keyword Extract

 |

 v

Impact Analysis

 |

 v

Embedding

```

---

# 9. AI分类

输出：

```json
{
"category":"AI",
"subCategory":"LLM"
}
```

分类：

第一阶段：

```text
国内

国际

科技

AI

财经

社会

体育

```

---

# 10. AI摘要

Prompt：

角色：

```
你是一名专业新闻编辑
```

要求：

输出：

```json
{
"summary":"",
"facts":[],
"background":"",
"impact":""
}
```

---

示例：

原文：

5000字。

输出：

300字。

---

# 11. 实体识别

Entity Extraction。

识别：

```text
公司

人物

国家

产品

组织

```

---

例如：

新闻：

```
OpenAI宣布GPT-6
```

实体：

```json
[
{
"type":"company",
"name":"OpenAI"
},
{
"type":"product",
"name":"GPT-6"
}
]
```

---

# 12. 新闻事件聚类

这是 Nexora Insight 基础。

目标：

把：

100篇新闻

变：

1个事件。

---

流程：

```text
News

 |

Embedding

 |

Vector Similarity

 |

Cluster

 |

Event

```

---

例如：

输入：

```
Tesla降价

Tesla价格调整

Model Y降价
```

生成：

```
Tesla价格调整事件
```

---

# 13. Embedding设计

用途：

## 语义搜索

用户：

搜索：

```
人工智能芯片
```

找到：

```
GPU

AI Accelerator

NPU
```

---

## 推荐

用户喜欢：

AI。

推荐：

类似事件。

---

技术：

第一阶段：

使用：

LLM Embedding API。

未来：

自建模型。

---

# 14. RocketMQ消息设计

采用事件驱动。

---

## Topic设计

新闻采集完成：

```
news-collected
```

消息：

```json
{
"newsId":10001
}
```

---

AI任务：

```
news-ai-task
```

---

索引任务：

```
news-index-task
```

---

事件聚合：

```
news-event-task
```

---

# 15. 完整处理流程

最终：

```text
                    RSS/API/Crawler

                          |

                          v

                    news_raw


                          |

                          v

                 Clean & Deduplicate


                          |

                          v

                  news_article


                          |

                    RocketMQ

                          |

        +-----------------+----------------+

        |                                  |

        v                                  v

   AI Analysis                       ES Index


        |

        v

  Event Cluster


        |

        v

  News Feed


        |

        v

      APP/Web

```

---

# 16. 第一阶段实现建议

不要一开始实现全部 AI。

MVP：

实现：

## 必须：

✅ 新闻采集

✅ 清洗

✅ AI摘要

✅ 分类

✅ ES搜索

✅ 热点排序

---

## 第二阶段：

增加：

✅ 实体识别

✅ Embedding

✅ 相似新闻

✅ 事件聚类

---

## 第三阶段：

增加：

✅ Insight

✅ AI研究助手

✅ 企业情报

---

# 17. 技术实现建议

结合你的 Java 背景：

## 后端

全部 Java：

```
Spring Boot 3

Spring AI

MyBatis-Plus

RocketMQ
```

完全可行。

---

## AI部分

不要自己训练模型。

采用：

```
AI Gateway

↓

DeepSeek/Qwen/OpenAI
```

---

## Python是否需要？

后期：

如果增加：

* NLP模型
* 推荐模型
* 知识图谱

可以引入：

```
Python AI Service

FastAPI
```

但是：

第一阶段：

Java 足够。

---

# 下一阶段

目前我们已经完成：

✅ 产品设计
✅ 架构设计
✅ 数据库设计
✅ 新闻处理Pipeline设计

下一步进入非常关键的：

# 《Nexora AI 微服务详细设计与工程落地方案》

内容：

1. Maven工程结构
2. Spring Boot模块划分
3. API设计
4. Controller/Service/Manager规范
5. MyBatis-Plus规范
6. Redis Key设计
7. RocketMQ生产消费设计
8. AI Gateway实现
9. OpenAPI接口规范
10. Web/App前后端协作设计
11. Docker Compose开发环境
12. K8s生产部署规划

这一部分会开始接近真正编码阶段。
