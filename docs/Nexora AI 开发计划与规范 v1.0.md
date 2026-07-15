# Nexora AI 开发计划、规范与验收标准 v1.0

## Context

基于已完成的7份架构设计文档（产品需求、技术架构、数据库设计、新闻处理流水线、微服务工程落地、API+业务流程、前端架构+交互设计），制定 Nexora AI 第一阶段的完整开发计划。本项目从零开始，需要建立开发规范、质量标准、测试验收体系，确保团队可执行、可验收、可交付。

---

# 第一部分：开发计划 (Development Plan)

## 1.1 总体里程碑

| 阶段 | 时间 | 目标 | 交付物 |
|------|------|------|--------|
| **MVP (Phase 1)** | Sprint 1-6 (约12周) | Nexora News 核心功能上线 | 模块化单体应用 + Web 前端 |
| **Phase 2** | Sprint 7-12 | Daily、推送、语音播报 | 功能增强 + APP Beta |
| **Phase 3** | Sprint 13+ | Insight、企业版 | 微服务拆分 + 全平台 |

## 1.2 Sprint 详细计划

### Sprint 0：项目初始化与环境搭建（第1周）

**目标**：完成工程骨架、开发环境、CI/CD 管线

**任务清单**：

| ID | 任务 | 负责模块 | 优先级 | 预估工时 |
|----|------|---------|--------|---------|
| S0-1 | 创建 Git Monorepo 仓库结构 | 基础设施 | P0 | 0.5d |
| S0-2 | Maven 父工程 + 多模块骨架 (`nexora-parent`, `nexora-common`, `nexora-api`, `nexora-app`, 各 `nexora-module-*`) | Backend | P0 | 1d |
| S0-3 | Spring Boot 3 启动配置 (`nexora-app` 作为唯一启动模块) | Backend | P0 | 0.5d |
| S0-4 | Docker Compose 开发环境 (MySQL, Redis, Elasticsearch, RocketMQ, Nacos, MinIO) | 基础设施 | P0 | 1d |
| S0-5 | 数据库初始化脚本 — 创建 `nexora` 库，14张表 DDL (sys_user, sys_user_account, news_article, news_source, news_category, news_ai_analysis, news_event, news_event_relation, news_tag, news_article_tag, user_behavior, user_favorite, user_interest, news_hot_score, ai_prompt_record) | Backend | P0 | 1d |
| S0-6 | MyBatis-Plus 配置 + 代码生成器模板 | Backend | P0 | 0.5d |
| S0-7 | 统一返回体 `Result<T>` + 统一异常处理 `GlobalExceptionHandler` | Backend | P0 | 0.5d |
| S0-8 | Vue3 + Vite + TypeScript 项目初始化 (`nexora-web`) | Frontend | P0 | 1d |
| S0-9 | OpenAPI Generator 配置 (springdoc → TS Client 自动生成) | Backend + Frontend | P0 | 0.5d |
| S0-10 | ESLint + Prettier + EditorConfig 配置 | Backend + Frontend | P1 | 0.5d |
| S0-11 | GitLab CI / GitHub Actions 流水线骨架 (compile → test → build) | 基础设施 | P1 | 1d |
| S0-12 | 编写 `CODEBUDDY.md` / `CONTRIBUTING.md` 开发指南 | 文档 | P1 | 0.5d |

**验收标准**：
- [ ] `docker compose up` 一键启动全部中间件
- [ ] `mvn clean package` 编译全部模块通过
- [ ] `nexora-app` 启动成功，访问 `localhost:8080` 返回健康检查
- [ ] `npm run dev` 启动前端开发服务器
- [ ] OpenAPI JSON 可从 `/v3/api-docs` 获取
- [ ] CI 流水线可自动执行编译

---

### Sprint 1：用户模块（第2-3周）

**目标**：完成用户注册、登录、认证体系

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S1-1 | MySQL 用户表 `sys_user` + `sys_user_account` + 索引设计 | DB | P0 | 0.5d |
| S1-2 | User Entity (DO) + Mapper (MyBatis-Plus) | nexora-module-user | P0 | 0.5d |
| S1-3 | `POST /api/v1/auth/register` 用户注册接口 | nexora-module-user | P0 | 1d |
| S1-4 | `POST /api/v1/auth/login` 登录 + JWT Token 签发 | nexora-module-user | P0 | 1d |
| S1-5 | `POST /api/v1/auth/refresh` Token 刷新 | nexora-module-user | P0 | 0.5d |
| S1-6 | `GET /api/v1/user/profile` 获取个人信息 | nexora-module-user | P0 | 0.5d |
| S1-7 | `PUT /api/v1/user/profile` 更新个人信息 | nexora-module-user | P0 | 0.5d |
| S1-8 | `GET /api/v1/user/preferences` 获取偏好设置 | nexora-module-user | P1 | 0.5d |
| S1-9 | `PUT /api/v1/user/preferences` 更新偏好设置 | nexora-module-user | P1 | 0.5d |
| S1-10 | Gateway 层 JWT 验证 Filter | nexora-app (config/security) | P0 | 1d |
| S1-11 | Redis Session 管理 (`user:session:{id}`) | nexora-module-user | P0 | 0.5d |
| S1-12 | 密码加密 (BCrypt) + 输入校验 | nexora-module-user | P0 | 0.5d |
| S1-13 | Web 前端：登录/注册页面 (Vue3 + Element Plus) | nexora-web | P0 | 1.5d |
| S1-14 | Web 前端：路由守卫 + Token 存储 (Pinia authStore) | nexora-web | P0 | 1d |
| S1-15 | API 测试：Auth 模块全部接口 | Test | P0 | 1d |
| S1-16 | 浏览器测试：登录/注册流程 E2E | Test | P0 | 1d |

**验收标准**：
- [ ] 注册/登录/Token刷新全部 API 测试通过
- [ ] 未登录用户访问受保护接口返回 401
- [ ] 登录后前端正确跳转首页
- [ ] 浏览器 E2E 测试：注册→登录→查看个人信息 全流程通过

---

### Sprint 2：新闻核心模块（第4-5周）

**目标**：新闻数据模型、CRUD、详情查询

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S2-1 | MySQL 表：`news_article`, `news_source`, `news_category`, `news_tag`, `news_article_tag` + 索引 | DB | P0 | 1d |
| S2-2 | News Entity (DO) + NewsMapper + XML 复杂查询 | nexora-module-news | P0 | 1d |
| S2-3 | `GET /api/v1/news/list` 新闻分页列表 | nexora-module-news | P0 | 1d |
| S2-4 | `GET /api/v1/news/{id}` 新闻详情 (含 AI 分析结果) | nexora-module-news | P0 | 1d |
| S2-5 | `GET /api/v1/news/{id}/related` 相关新闻推荐 | nexora-module-news | P1 | 0.5d |
| S2-6 | `GET /api/v1/news/categories` 新闻分类列表 | nexora-module-news | P0 | 0.5d |
| S2-7 | 新闻详情 Redis 缓存 (`news:detail:{id}`, TTL 30min) | nexora-module-news | P0 | 0.5d |
| S2-8 | `POST /api/v1/news/{id}/favorite` 收藏新闻 | nexora-module-news | P0 | 0.5d |
| S2-9 | `DELETE /api/v1/news/{id}/favorite` 取消收藏 | nexora-module-news | P0 | 0.5d |
| S2-10 | `GET /api/v1/favorites` 收藏列表 | nexora-module-news | P0 | 0.5d |
| S2-11 | Web 前端：新闻列表页 + 新闻卡片组件 `NewsCard` | nexora-web | P0 | 1.5d |
| S2-12 | Web 前端：新闻详情页 (含 AI 分析展示) | nexora-web | P0 | 1.5d |
| S2-13 | Web 前端：分类导航 | nexora-web | P0 | 1d |
| S2-14 | Web 前端：收藏功能 (Pinia newsStore) | nexora-web | P0 | 1d |
| S2-15 | API 测试：News 模块全部接口 | Test | P0 | 1d |
| S2-16 | 浏览器测试：新闻浏览+收藏流程 E2E | Test | P0 | 1d |

**验收标准**：
- [ ] 新闻列表分页查询正常，支持分类筛选
- [ ] 新闻详情返回完整数据（含 AI 分析字段）
- [ ] 新闻详情缓存命中率 > 80%（压测验证）
- [ ] 收藏/取消收藏功能正常
- [ ] 全部 API 测试通过
- [ ] 浏览器 E2E 测试：浏览列表→查看详情→收藏→查看收藏列表 全流程通过

---

### Sprint 3：新闻采集模块（第6-7周）

**目标**：RSS/API 采集、原始新闻存储、清洗去重 Pipeline

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S3-1 | MySQL 表：`news_raw` + hash 索引 | DB | P0 | 0.5d |
| S3-2 | `NewsCollector` 接口 + `RSSCollector` 实现 | nexora-module-crawler | P0 | 1.5d |
| S3-3 | `NewsCollector` 接口 + `BBCCollector` / `ReutersCollector` 实现 | nexora-module-crawler | P0 | 1.5d |
| S3-4 | XXL-JOB 定时调度配置 (不同来源不同频率) | nexora-module-crawler | P0 | 1d |
| S3-5 | Content Cleaner (HTML 去除、广告过滤、内容标准化) | nexora-module-crawler | P0 | 1.5d |
| S3-6 | Duplicate Detector (URL Hash + SimHash 两级去重) | nexora-module-crawler | P0 | 1.5d |
| S3-7 | Quality Scorer (来源权重 + 内容长度 + 原创性) | nexora-module-crawler | P0 | 1d |
| S3-8 | Raw News → Cleaned News 完整 Pipeline 串联 | nexora-module-crawler | P0 | 1d |
| S3-9 | RocketMQ Producer: `news-collected` Topic 消息发送 | nexora-module-crawler | P0 | 1d |
| S3-10 | RocketMQ Consumer: AI 分析任务消费（骨架） | nexora-module-ai | P0 | 1d |
| S3-11 | RocketMQ Consumer: ES 索引任务消费（骨架） | nexora-module-search | P0 | 1d |
| S3-12 | 采集任务监控面板 (采集量、成功率、异常日志) | nexora-module-crawler | P1 | 1d |
| S3-13 | API 测试：采集 Pipeline Mock 测试 | Test | P0 | 1d |
| S3-14 | 集成测试：采集→清洗→去重→入库 全链路 | Test | P0 | 1d |

**验收标准**：
- [ ] 至少 3 个 RSS 源可正常采集并入库
- [ ] URL Hash 去重准确率 100%
- [ ] SimHash 相似度 > 0.9 的新闻被正确去重
- [ ] 采集完成后正确发送 MQ 消息
- [ ] Pipeline 全链路集成测试通过
- [ ] XXL-JOB 定时任务正常调度执行

---

### Sprint 4：AI 能力模块（第8-9周）

**目标**：AI Gateway、摘要生成、新闻分类、实体识别

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S4-1 | MySQL 表：`news_ai_analysis` (含 JSON 字段: keywords, entities) + `ai_prompt_record` (Prompt 调用记录) | DB | P0 | 0.5d |
| S4-2 | `LLMProvider` 接口 + `DeepSeekProvider` 实现 | nexora-module-ai | P0 | 1.5d |
| S4-3 | `OpenAIProvider` + `QwenProvider` 实现 | nexora-module-ai | P1 | 1d |
| S4-4 | AI Gateway 路由层 (模型选择、fallback、限流) | nexora-module-ai | P0 | 1.5d |
| S4-5 | Prompt 模板管理 (YAML 文件: `news-summary.yaml`, `news-classify.yaml`, `entity-extract.yaml`) | nexora-module-ai | P0 | 1d |
| S4-6 | `NewsAIManager` (编排: 获取新闻→调模型→保存结果→通知搜索) | nexora-module-ai | P0 | 1.5d |
| S4-7 | AI 摘要生成 (输入新闻正文 → 输出结构化摘要 JSON) | nexora-module-ai | P0 | 1.5d |
| S4-8 | AI 新闻分类 (科技/AI/财经/国际/国内/社会/体育) | nexora-module-ai | P0 | 1d |
| S4-9 | 实体识别 (公司/人物/国家/产品/组织) | nexora-module-ai | P1 | 1d |
| S4-10 | AI 任务幂等性 (Redis `ai:task:{newsId}`) | nexora-module-ai | P0 | 0.5d |
| S4-11 | AI 分析状态机 (PENDING → PROCESSING → COMPLETED/FAILED) | nexora-module-ai | P0 | 0.5d |
| S4-12 | RocketMQ Consumer 完整实现: 消费 `news-collected` → AI 分析 | nexora-module-ai | P0 | 1d |
| S4-13 | AI 调用监控 (成功率、耗时、Token 用量) | nexora-module-ai | P1 | 0.5d |
| S4-14 | API 测试：AI 接口 Mock LLM 响应 | Test | P0 | 1d |
| S4-15 | 集成测试：MQ消息→AI分析→结果保存 全链路 | Test | P0 | 1d |

**验收标准**：
- [ ] AI Gateway 可正确路由到不同 LLM Provider
- [ ] AI 摘要格式符合设计规范（summary + facts + background + impact）
- [ ] 新闻分类准确率 > 85%（人工抽样 100 条验证）
- [ ] AI 任务幂等：同一条新闻重复消费不重复调用 LLM
- [ ] AI 调用失败时状态正确标记为 FAILED 并记录错误日志
- [ ] 全部 API 测试 + 集成测试通过

---

### Sprint 5：搜索模块（第10-11周）

**目标**：Elasticsearch 索引、全文搜索、搜索 API

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S5-1 | ES Index Mapping 设计 (`news_index`) | nexora-module-search | P0 | 0.5d |
| S5-2 | ES Repository 封装 (Spring Data Elasticsearch) | nexora-module-search | P0 | 1d |
| S5-3 | IK 分词器配置 + 自定义词库 | nexora-module-search | P0 | 0.5d |
| S5-4 | 新闻索引服务 (创建索引、批量索引、更新、删除) | nexora-module-search | P0 | 1d |
| S5-5 | RocketMQ Consumer 完整实现: 消费 `news-index-task` → ES 索引 | nexora-module-search | P0 | 1d |
| S5-6 | `GET /api/v1/search/news?q=keyword` 关键词全文搜索 | nexora-module-search | P0 | 1d |
| S5-7 | `GET /api/v1/search/news/advanced` 高级搜索 (时间/来源/分类/国家) | nexora-module-search | P1 | 1d |
| S5-8 | `GET /api/v1/search/suggestions?q=prefix` 搜索建议/自动补全 | nexora-module-search | P1 | 0.5d |
| S5-9 | 搜索结果高亮 + 分页 | nexora-module-search | P0 | 0.5d |
| S5-10 | Web 前端：搜索页面 (搜索框 + 结果列表 + 高级筛选) | nexora-web | P0 | 1.5d |
| S5-11 | Web 前端：搜索结果高亮显示 | nexora-web | P0 | 0.5d |
| S5-12 | API 测试：Search 模块全部接口 | Test | P0 | 1d |
| S5-13 | 浏览器测试：搜索流程 E2E | Test | P0 | 1d |

**验收标准**：
- [ ] ES 索引创建成功，Mapping 字段正确
- [ ] 新闻发布后自动索引到 ES
- [ ] 关键词搜索返回相关结果，支持中文分词
- [ ] 搜索结果按相关度排序，支持分页
- [ ] 搜索建议/Autocomplete 可用
- [ ] 全部 API 测试 + 浏览器 E2E 测试通过

---

### Sprint 6：首页信息流 + 集成联调（第12-13周）

**目标**：首页热点信息流、用户订阅、端到端集成、性能优化

**任务清单**：

| ID | 任务 | 模块 | 优先级 | 预估工时 |
|----|------|------|--------|---------|
| S6-1 | MySQL 表：`user_subscription`, `user_interest`, `user_favorite`, `user_behavior`, `news_hot_score` | DB | P0 | 0.5d |
| S6-2 | `POST /api/v1/subscribe` 创建订阅 (TAG/ENTITY/COMPANY) | nexora-module-user | P0 | 0.5d |
| S6-3 | `DELETE /api/v1/subscribe/{id}` 取消订阅 | nexora-module-user | P0 | 0.5d |
| S6-4 | `GET /api/v1/subscribe` 订阅列表 | nexora-module-user | P0 | 0.5d |
| S6-5 | `GET /api/v1/feed/home` 首页信息流 (FeedManager) | nexora-module-news | P0 | 1.5d |
| S6-6 | 热点排序算法 (热度 + 时间衰减 + 用户兴趣权重) | nexora-module-news | P0 | 1.5d |
| S6-7 | Redis ZSet 热点榜 (`news:hot`) 定时刷新 | nexora-module-news | P0 | 1d |
| S6-8 | 个性化推荐（规则推荐：热度 + 用户关注 + 时间衰减 + 领域权重） | nexora-module-news | P1 | 1d |
| S6-9 | Web 前端：首页 (热点 TOP10 + 国内/国际/AI科技/财经/我的关注) | nexora-web | P0 | 2d |
| S6-10 | Web 前端：订阅管理页面 | nexora-web | P0 | 1d |
| S6-11 | Web 前端：个人中心页面 | nexora-web | P0 | 1d |
| S6-12 | Web 前端：国际化 (Vue-i18n, 中/英) | nexora-web | P1 | 1d |
| S6-13 | Web 前端：暗黑模式 | nexora-web | P1 | 1d |
| S6-14 | API 测试：Feed + Subscribe 全部接口 | Test | P0 | 1d |
| S6-15 | **端到端集成测试：采集→AI分析→索引→首页展示→搜索→收藏** | Test | P0 | 2d |
| S6-16 | **浏览器全流程 E2E 测试** | Test | P0 | 2d |
| S6-17 | 性能测试：首页接口响应时间 < 200ms (P95) | Test | P0 | 1d |
| S6-18 | Bug 修复 + 文档补全 | All | P0 | 2d |

**验收标准（MVP 最终验收）**：
- [ ] 全部 30+ API 接口测试通过
- [ ] 浏览器 E2E 全流程测试通过
- [ ] 首页接口 P95 响应时间 < 200ms
- [ ] 新闻采集 → AI 分析 → 首页展示 全链路可用
- [ ] 用户注册 → 登录 → 浏览 → 搜索 → 收藏 → 订阅 全流程可用
- [ ] Docker Compose 一键启动全部服务
- [ ] 所有模块代码覆盖率 > 70%

---

# 第二部分：开发规范 (Development Standards)

## 2.1 代码风格规范

### 2.1.1 Java 代码规范

**基础规范**：
- 遵循 **阿里巴巴 Java 开发手册** (最新版)
- JDK 版本：**21** (LTS)
- 字符编码：**UTF-8**
- 缩进：4 空格（禁止 Tab）
- 行最大宽度：120 字符
- 文件末尾必须有空行

**命名规范**：
```
类名：PascalCase        NewsService, NewsPublishManager
方法名：camelCase        getNewsDetail(), publishNews()
变量名：camelCase        newsId, aiSummary
常量名：UPPER_SNAKE      MAX_RETRY_COUNT, DEFAULT_PAGE_SIZE
包名：lowercase          com.nexora.module.news
Entity (DO)：{Entity}DO  NewsDO, UserDO
DTO：{Entity}DTO         NewsDTO, NewsQueryDTO
VO：{Entity}VO           NewsVO, HotNewsVO
Request：{Action}Request  NewsPageRequest, LoginRequest
Response：{Action}Response NewsDetailResponse
```

**注解规范**：
```java
// Controller: 统一使用 @RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "新闻模块", description = "新闻相关接口")
public class NewsController {
    private final NewsService newsService;
}

// Service: 接口 + 实现分离
public interface NewsService {
    NewsVO getDetail(Long id);
}

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    // ...
}
```

**必须遵守的规则**：
- [ ] Controller 禁止包含业务逻辑、SQL、MQ 发送
- [ ] Service 保持单领域职责，禁止跨领域直接调用 Mapper
- [ ] Manager 仅用于编排多领域 Service/Client 的复杂流程
- [ ] 禁止 Controller 直接调用 Mapper
- [ ] 复杂 SQL 必须写在 Mapper XML 中，禁止 Service 中拼接 SQL
- [ ] 所有外部系统调用必须通过 Client 层封装

### 2.1.2 TypeScript/Vue 代码规范

**基础规范**：
- 遵循 **Vue 3 官方风格指南** (Priority A + B 规则)
- TypeScript 严格模式 (`strict: true`)
- 缩进：2 空格
- 行最大宽度：100 字符
- 使用 Composition API (`<script setup lang="ts">`)

**命名规范**：
```
组件文件：PascalCase       NewsCard.vue, HotList.vue
组合式函数：useXxx         useAuth, useNews
Store：{name}Store        authStore, newsStore
类型/接口：PascalCase      NewsItem, UserProfile
API 函数：camelCase        getNewsDetail, searchNews
路由路径：kebab-case       /news-detail/:id
```

**目录结构规范**：
```
src/
├── api/
│   ├── generated/          # OpenAPI 自动生成，禁止手动修改
│   └── index.ts            # Axios 实例 + 拦截器
├── components/
│   ├── common/             # 通用组件 (NewsCard, LoadingSpinner)
│   └── layout/             # 布局组件 (Header, Sidebar, Footer)
├── pages/                  # 页面级组件 (与路由一一对应)
├── router/                 # 路由配置
├── stores/                 # Pinia 状态管理
├── hooks/                  # 可复用组合式函数
├── types/                  # 手动定义的补充类型
├── utils/                  # 工具函数
└── locales/                # 国际化文件
```

### 2.1.3 Dart/Flutter 代码规范

- 遵循 **Effective Dart** 规范
- 使用 `flutter_lints` 包
- 命名：文件 `snake_case`，类 `PascalCase`，变量/方法 `camelCase`
- 状态管理统一使用 **Riverpod**

---

## 2.2 Git 工作流规范

### 分支策略 (Git Flow 简化版)

```
main          # 生产环境，只接受 release 合并
  └── develop # 开发主线
       ├── feature/S0-01-project-init  # 功能分支
       ├── feature/S1-03-user-register
       ├── fix/news-detail-cache-bug   # 修复分支
       └── release/1.0.0              # 发布分支
```

### 分支命名规范

```
feature/{sprint}-{task-id}-{short-desc}    feature/S1-03-user-register
fix/{short-desc}                           fix/news-cache-null-pointer
hotfix/{short-desc}                        hotfix/login-security-patch
release/{version}                          release/1.0.0
```

### Commit 规范 (Conventional Commits)

```
<type>(<scope>): <subject>

类型 (type)：
  feat     新功能
  fix      Bug 修复
  docs     文档变更
  style    代码格式（不影响功能）
  refactor 重构（既非新功能也非修复）
  perf     性能优化
  test     测试相关
  chore    构建/工具变更
  ci       CI/CD 变更

范围 (scope)：
  user, news, ai, crawler, search, common, config, web, docs

示例：
  feat(user): 实现用户注册接口 POST /api/v1/auth/register
  feat(web): 添加登录页面和路由守卫
  fix(news): 修复新闻详情缓存未失效的问题
  test(news): 添加新闻模块 API 集成测试
  docs: 更新开发环境搭建指南
```

### PR/MR 规范

每个 PR 必须包含：
1. **标题**：符合 Commit 规范
2. **描述**：变更内容 + 关联 Issue/Task ID
3. **检查清单**：
   - [ ] 本地编译通过 (`mvn clean package`)
   - [ ] 相关 API 测试全部通过
   - [ ] 新增代码有单元测试覆盖
   - [ ] 前端变更已通过浏览器 E2E 测试
4. **Reviewer**：至少 1 人 Code Review 通过

---

## 2.3 API 设计规范

### URL 规范

```
基础路径：/api/v1/{module}/{resource}

示例：
  GET    /api/v1/news/{id}              查询新闻详情
  GET    /api/v1/news/list              新闻列表
  POST   /api/v1/auth/register          用户注册
  POST   /api/v1/auth/login             用户登录
  GET    /api/v1/feed/home              首页信息流
  GET    /api/v1/search/news            新闻搜索
  POST   /api/v1/news/{id}/favorite     收藏新闻
  DELETE /api/v1/news/{id}/favorite     取消收藏
```

### 统一响应格式

```json
// 成功
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": 1721000000000
}

// 失败
{
  "code": 30001,
  "message": "新闻不存在",
  "data": null,
  "timestamp": 1721000000000
}
```

### 错误码规范

```
系统级：
  0       成功
  10000   系统内部错误
  10001   参数校验失败
  10002   服务不可用

认证/用户 (20000)：
  20001   未登录
  20002   Token 过期
  20003   用户名或密码错误
  20004   邮箱已注册
  20005   用户不存在

新闻 (30000)：
  30001   新闻不存在
  30002   新闻已下架
  30003   收藏重复

AI (40000)：
  40001   AI 分析失败
  40002   AI 服务超时
  40003   AI 任务重复

搜索 (50000)：
  50001   搜索服务异常
  50002   索引重建中

外部服务 (60000)：
  60001   采集源不可达
  60002   采集解析失败
```

### OpenAPI 文档

- 使用 **springdoc-openapi** 自动生成 OpenAPI 3.0 规范
- 每个 Controller 必须有 `@Tag` 注解
- 每个接口必须有 `@Operation` 注解（summary + description）
- 每个参数必须有 `@Parameter` 注解（description + required + example）
- DTO 类必须有 `@Schema` 注解（description + example）
- OpenAPI JSON 作为前端 TS Client 自动生成的唯一数据源

---

## 2.4 数据库设计规范

### 表命名规范

```
业务表：{module}_{entity}
示例：news_article, news_raw, user_account, user_subscription

关联表：{module}_{entity_a}_{entity_b}
示例：news_article_category, user_favorite_news

字段命名：snake_case
示例：created_time, hot_score, ai_summary
```

### 必需的公共字段

每个业务表必须包含：
```sql
id          BIGINT PRIMARY KEY AUTO_INCREMENT,
created_by  BIGINT       COMMENT '创建人ID',
created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_by  BIGINT       COMMENT '更新人ID',
updated_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除'
```

### 索引规范

- 主键索引：`pk_{table_name}` → 自动
- 唯一索引：`uk_{table_name}_{column}` → `uk_user_account_email`
- 普通索引：`idx_{table_name}_{column}` → `idx_news_article_publish_time`
- 联合索引：`idx_{table_name}_{col1}_{col2}` → `idx_news_article_category_status`

### 数据库变更管理

- 推荐使用 **Flyway** 做数据库版本迁移
- SQL 脚本命名：`V{version}__{description}.sql`
  - 示例：`V1.0.0__init_user_table.sql`, `V1.1.0__add_news_ai_analysis.sql`
- 脚本放置于：`nexora-app/src/main/resources/db/migration/`
- 禁止手动修改已执行的迁移脚本

---

## 2.5 Redis 使用规范

### Key 命名规范

```
格式：{module}:{entity}:{identifier}
示例：
  news:detail:10001          新闻详情缓存
  news:hot                   热点排行榜 (ZSet)
  user:session:10001         用户 Session
  ai:task:10001              AI 任务幂等标记
  news:view:10001            新闻阅读计数
```

### 规范要求

- [ ] 所有 Redis Key 必须定义在 `RedisKeyConstants` 接口中，禁止硬编码
- [ ] 所有 Key 必须设置 TTL（过期时间），禁止永久 Key
- [ ] 热点数据 TTL：10-30 分钟
- [ ] Session 数据 TTL：24 小时
- [ ] 幂等标记 TTL：1 小时
- [ ] 禁止在业务代码中直接使用 `redisTemplate.opsFor*()` 拼接 Key

---

## 2.6 RocketMQ 使用规范

### Topic 命名规范

```
格式：nexora-{domain}-{action}
示例：
  nexora-news-collected      新闻采集完成
  nexora-news-ai-task        AI 分析任务
  nexora-news-index-task     ES 索引任务
  nexora-user-notification   用户通知
  nexora-news-event-task     事件聚合任务
```

### Consumer Group 命名

```
格式：{service}-{topic}-consumer
示例：
  ai-service-news-collected-consumer
  search-service-news-index-consumer
```

### 消息规范

- [ ] 消息体统一使用 JSON 格式
- [ ] 消息必须包含 `messageId` (UUID) 和 `timestamp`
- [ ] Consumer 必须实现幂等消费（检查 `messageId` 是否已处理）
- [ ] 消费失败时记录错误日志并发送告警
- [ ] 禁止在 Consumer 中执行耗时超过 30 秒的同步操作

---

## 2.7 异常处理规范

### 异常体系

```
BusinessException (业务异常，code + message)
  ├── UserNotFoundException
  ├── NewsNotFoundException
  ├── DuplicateFavoriteException
  └── AIAnalysisFailedException

SystemException (系统异常)
  ├── ExternalServiceException
  ├── DatabaseException
  └── CacheException
```

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(10000, "系统内部错误");
    }
}
```

### 规范要求

- [ ] 业务异常必须抛出 `BusinessException` 及其子类，带明确的错误码
- [ ] 禁止在 Controller 中 try-catch 后返回 `Result.error()`
- [ ] 禁止吞掉异常（空 catch 块）
- [ ] 异常日志必须包含完整堆栈和关键业务参数
- [ ] 对外异常信息禁止暴露内部实现细节（如 SQL 语句、堆栈信息）

---

## 2.8 日志规范

### 日志框架

- 使用 **SLF4J + Logback**
- 配置文件：`logback-spring.xml`

### 日志级别使用

```
ERROR   系统错误、需要人工介入的问题
WARN    潜在风险、降级操作、重试
INFO    关键业务流程节点（请求入口、AI调用、MQ发送/消费、定时任务执行）
DEBUG   调试信息（开发环境启用，生产环境关闭）
```

### 日志格式

```
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] [%level] [%logger{50}] [%X{traceId}] - %msg%n
```

### 规范要求

- [ ] 必须打印 TraceId（链路追踪），通过 SkyWalking 或 MDC 传递
- [ ] Controller 层使用 AOP 统一记录请求日志（URL、参数、耗时、响应码）
- [ ] 敏感信息（密码、Token、手机号）禁止打印或必须脱敏
- [ ] 禁止使用 `System.out.println`
- [ ] 禁止在循环中打印日志

---

## 2.9 安全规范

- [ ] 密码使用 **BCrypt** 加密存储（强度 >= 10）
- [ ] JWT Token 有效期：Access Token 2小时，Refresh Token 7天
- [ ] 所有用户输入必须做**参数校验**（`@Valid` / `@Validated`）
- [ ] SQL 注入防护：使用 MyBatis `#{}` 占位符，禁止 `${}`
- [ ] XSS 防护：前端输出必须转义
- [ ] CSRF 防护：API 使用 JWT Bearer Token，不依赖 Cookie
- [ ] API 限流：Gateway 层实现（Nacos + Sentinel）
- [ ] 敏感配置（密钥、密码）必须通过环境变量或配置中心注入，禁止硬编码

---

# 第三部分：测试与验收标准 (Testing & Acceptance)

## 3.1 测试金字塔

```
         /\
        /E2E\          浏览器端到端测试 (Playwright/Cypress)
       /------\
      /  API   \        API 集成测试 (Spring MockMvc / RestAssured)
     /----------\
    /  Service   \      Service 层单元测试 (JUnit5 + Mockito)
   /--------------\
  /    Unit Test   \    Mapper/Util 单元测试 (JUnit5)
 /------------------\
```

## 3.2 测试技术要求

### 单元测试 (Unit Test)

| 项目 | 要求 |
|------|------|
| 框架 | JUnit 5 + Mockito |
| 覆盖率目标 | Service 层 >= 80%，整体 >= 70% |
| 执行要求 | `mvn test` 全部通过 |
| Mock 策略 | Mapper、外部 Client 必须 Mock |
| 命名规范 | `{ClassName}Test.java`，方法 `should_{expected}_when_{condition}` |

### API 集成测试 (API Test)

| 项目 | 要求 |
|------|------|
| 框架 | Spring Boot Test + MockMvc (Controller 层) 或 RestAssured (全链路) |
| 覆盖要求 | **所有 API 端点** 必须有测试 |
| 验证内容 | 参数校验、业务逻辑、响应格式、错误码、边界条件 |
| 数据隔离 | 使用 `@Transactional` + `@Rollback` 或 H2 内存数据库 |
| 执行要求 | `mvn verify` 全部通过，**API 测试不通过 = 不可合并** |

### 浏览器端到端测试 (E2E Test)

| 项目 | 要求 |
|------|------|
| 框架 | Playwright (推荐) 或 Cypress |
| 覆盖要求 | **所有核心用户流程** 必须有 E2E 测试 |
| 测试环境 | 独立测试环境 (test profile + H2/test DB) |
| 执行要求 | `npm run test:e2e` 全部通过，**E2E 不通过 = 不可合并** |

## 3.3 必测核心流程 (Critical Path)

以下流程 **必须** 有 E2E 测试覆盖：

1. **用户注册→登录→首页**：注册新账号→登录→跳转首页→看到热点新闻
2. **浏览→阅读→收藏**：查看列表→点击新闻→阅读详情→收藏→查看收藏列表
3. **搜索→筛选→阅读**：输入关键词→查看搜索结果→高级筛选→阅读详情
4. **订阅管理**：创建订阅→查看订阅列表→取消订阅
5. **未登录拦截**：未登录访问收藏→被拦截→登录→正常访问

## 3.4 验收检查清单 (Definition of Done)

一个 Sprint 或 Feature 标记为 "完成" 必须满足：

```
□ 代码编译零错误零警告
□ 所有新增代码有对应的单元测试
□ 所有涉及的 API 端点有集成测试
□ API 测试 100% 通过 (mvn verify)
□ 所有核心用户流程 E2E 测试 100% 通过
□ Code Review 通过 (至少 1 位 Reviewer Approve)
□ 无未解决的 Blocker / Critical 级别 SonarQube 问题
□ API 文档 (OpenAPI JSON) 已同步更新
□ 数据库迁移脚本 (Flyway) 已提交
□ 相关技术文档已更新 (如有必要)
□ 功能在本地 Docker Compose 环境中验证通过
```

## 3.5 CI/CD 质量门禁

```yaml
Pipeline 阶段：
  Stage 1: Compile
    - Maven 编译 (所有模块)
    - ESLint/Prettier 检查 (前端)
    
  Stage 2: Test
    - 单元测试 (mvn test)
    - API 集成测试 (mvn verify)
    - 前端单元测试 (vitest)
    
  Stage 3: E2E Test
    - 启动测试环境 (Docker Compose)
    - 数据库迁移 (Flyway)
    - 浏览器 E2E 测试 (Playwright)
    - 清理测试环境
    
  Stage 4: Quality Gate
    - 代码覆盖率检查 (> 70%)
    - SonarQube 扫描 (无 Blocker/Critical)
    - 安全依赖扫描 (OWASP Dependency Check)
    
  Stage 5: Build & Push
    - Docker 镜像构建
    - 推送到镜像仓库

合并规则：
  ❌ Stage 2 任一项失败 → 禁止合并
  ❌ Stage 3 任一项失败 → 禁止合并
  ❌ Stage 4 覆盖率不达标 → 禁止合并
  ⚠️ Stage 4 SonarQube Major 问题 → 警告但不阻止（需 Reviewer 确认）
```

## 3.6 测试环境配置

| 环境 | 用途 | 数据库 | 中间件 | 部署方式 |
|------|------|--------|--------|---------|
| **dev** | 本地开发 | H2/本地 MySQL | Docker Compose | IDE 直接启动 |
| **test** | CI 自动化测试 | H2 内存数据库 | Testcontainers | CI 流水线 |
| **staging** | 预发布验证 | 独立 MySQL | 独立中间件集群 | K3s |
| **prod** | 生产环境 | 高可用 MySQL | 高可用集群 | K8s |

## 3.7 性能验收标准 (MVP)

| 指标 | 目标 | 测试方法 |
|------|------|---------|
| 首页接口 P95 响应时间 | < 200ms | JMeter / k6 |
| 新闻详情接口 P95 响应时间 | < 100ms (缓存命中) | JMeter / k6 |
| 搜索接口 P95 响应时间 | < 500ms | JMeter / k6 |
| AI 摘要生成时间 | < 10s (异步处理) | 监控打点 |
| 并发用户数 (MVP) | 支持 100 并发 | JMeter |
| 系统可用性 | > 99.5% | Prometheus 监控 |

---

# 第四部分：项目管理

## 4.1 日报规范

每个开发者每日提交：
```
日期：2026-07-15
今日完成：
  - [S1-03] 完成用户注册接口开发
  - [S1-04] 完成登录接口开发
明日计划：
  - [S1-05] 完成 Token 刷新接口
  - [S1-10] 开始 Gateway JWT Filter
阻塞项：无
```

## 4.2 技术文档要求

每个模块必须维护：
- `README.md`：模块概述、依赖、启动方式
- `api-usage.md`：接口使用说明（非 OpenAPI 自动生成的部分）
- `architecture.md`：模块内部架构说明（可选，复杂模块必须）

## 4.3 Code Review 检查清单

Reviewer 必须检查：
- [ ] 代码符合命名规范和目录规范
- [ ] Controller 不包含业务逻辑
- [ ] Service 保持单领域
- [ ] 异常处理正确（不吞异常，错误码正确）
- [ ] SQL 使用 `#{}` 而非 `${}`
- [ ] Redis Key 定义在常量类中
- [ ] 日志输出适当（含 TraceId，敏感信息脱敏）
- [ ] 新增代码有测试覆盖
- [ ] API 文档已更新

---

# 第五部分：数据库表参考 (Database Schema Reference)

基于《Nexora AI 数据库详细设计 v1.0》，第一阶段 MVP 共 **14 张核心表**（+ 1 张 AI 监控表），统一使用单库 `nexora`。

## 5.1 用户域 (2 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `sys_user` | 用户基础信息 | id, username, email, password, nickname, avatar, language, status |
| `sys_user_account` | 第三方登录关联 | user_id, provider (google/apple/wechat), open_id |

## 5.2 新闻域 (5 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `news_article` | 新闻文章主体 | id, title, content, summary, source_id, language, category_id, publish_time, status, hot_score |
| `news_source` | 新闻来源 | id, name, country, language, url, type |
| `news_category` | 新闻分类 (树形) | id, name, parent_id, sort |
| `news_tag` | 标签字典 | id, name, type |
| `news_article_tag` | 新闻-标签关联 | news_id, tag_id |

## 5.3 AI 分析域 (2 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `news_ai_analysis` | AI 分析结果 (JSON 灵活字段) | news_id, model, summary, keywords (JSON), entities (JSON), sentiment, impact |
| `ai_prompt_record` | Prompt 调用记录 (成本监控) | business_type, model, prompt, response, cost_token |

## 5.4 事件域 (2 张，Phase 2 核心)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `news_event` | 新闻事件聚合 | title, description, event_time, importance |
| `news_event_relation` | 事件-新闻关联 | event_id, news_id |

## 5.5 用户行为域 (4 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `user_favorite` | 用户收藏 | user_id, news_id, created_time |
| `user_subscription` | 用户订阅 | user_id, type (TAG/ENTITY/CATEGORY), target |
| `user_interest` | 用户兴趣画像 (推荐基础) | user_id, tag_id, weight |
| `user_behavior` | 用户行为日志 (VIEW/LIKE/FAVORITE/SHARE/SEARCH) | user_id, news_id, behavior_type, duration |

## 5.6 热点域 (1 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `news_hot_score` | 新闻热度分 (独立于文章表) | news_id, score, calculate_time |

## 5.7 关键设计决策

- **AI 分析结果使用 JSON 字段** (`keywords JSON`, `entities JSON`)：AI 输出结构变化快，JSON 避免频繁 DDL
- **热度分独立存储** (`news_hot_score`)：热度计算与新闻主体解耦，支持多时间窗口
- **新闻与 AI 分析分离**：同一新闻可被不同模型重新分析，历史结果可追溯
- **行为数据预留** (`user_behavior`)：Phase 1 收集数据，Phase 2 驱动推荐算法
- **事件模型预留** (`news_event`, `news_event_relation`)：Phase 1 建表，Phase 2 实现事件聚类
- **未来分库分表**：`news_article` 按月份分表 (`news_article_202601`)，`user_behavior` 迁入 ClickHouse
