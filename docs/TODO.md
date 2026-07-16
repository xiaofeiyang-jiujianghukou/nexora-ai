# Nexora AI — 开发日志

> 最后更新：2026-07-16 (MVP P0/P1/P2 全部完成，推荐引擎上线)    
> GitHub：https://github.com/xiaofeiyang-jiujianghukou/nexora-ai.git

---

## ✅ 已完成

### Sprint 0：项目骨架
- Maven 10 模块工程（common / api / app / user / news / ai / search / crawler / notification）
- Docker Compose 7 个中间件（MySQL / Redis / ES / RocketMQ / Nacos / MinIO）
- Vue3 + Vite + TypeScript + Element Plus + Pinia 前端骨架
- Spring Security + JWT 认证体系
- Flyway 数据库迁移
- 统一返回体 `Result<T>`、全局异常处理、27 个错误码

### Sprint 1：用户模块
- 注册 / 登录 / Token 刷新 / 个人信息 CRUD（6 个端点）
- BCrypt 密码加密、JWT 双 Token 机制
- 前端登录 + 注册页面、路由守卫、authStore

### Sprint 2：新闻核心模块
- 新闻分页列表 / 详情 / 相关新闻 / 分类（4 个端点）
- 收藏添加 / 删除 / 列表（3 个端点）
- NewsCard 组件、首页、详情页、收藏页

### Sprint 3：采集模块
- ContentCleaner、DuplicateDetector、QualityScorer
- RSSCollector（Rome 库通用 RSS/Atom 采集）

### Sprint 4：AI 模块
- LLMProvider → DeepSeekProvider（真实调用）
- PromptManager（YAML 模板 + 变量渲染）
- NewsAIManager（AI 分析编排：摘要 → 分类 → 实体识别）

### Sprint 5：搜索模块
- MySQL LIKE 全文搜索 + 搜索建议（2 个端点）
- 前端搜索页面

### Sprint 6：Feed + 订阅 + 国际化
- FeedManager + FeedController（首页热点 + 按分类分组）
- 订阅创建 / 删除 / 列表（3 个端点）
- 暗黑模式 / 中英文切换 / 个人中心 / 响应式适配

---

## 🔧 MVP 增强（2026-07-16）

### P0 — 数据库清理 + 性能优化

| 事项 | 说明 |
|------|------|
| **数据库清理** | V1.0.8 DROP `ai_result` + `ai_languages` 列，全部迁移到 `news_article_i18n` 表 |
| **SearchServiceImpl 重构** | `a.getAiResult()` → i18n 批量查询 `NewsArticleI18nMapper` |
| **首页 Redis 缓存** | `news:list:{page}:{size}:{cat}:{lang}`, TTL 5min，可选注入兼容测试环境 |

### P1 — 前端滚动优化 + E2E 恢复

| 事项 | 说明 |
|------|------|
| **el-scrollbar 迁移** | `home/index.vue`：`<el-scrollbar>` 包裹新闻列表，组件内滚动替代 window scroll |
| **Playwright E2E 恢复** | 5 spec 文件 + playwright.config.ts 从 git 恢复，`@playwright/test` 已安装 |
| **E2E 覆盖** | auth / news / search / lang-switch / visual 全流程 |

### P2 — 个性化推荐引擎

| 事项 | 说明 |
|------|------|
| **推荐算法** | `finalScore = tanh(hotScore/50) × exp(-daysOld/7) × (1 + 0.5 × interestRatio)` |
| **兴趣向量** | 基于用户收藏历史的分类占比（收藏 < 3 → 热门兜底） |
| **新增端点** | `GET /api/v1/news/recommendations?limit=20`（匿名返回热门） |
| **修改文件** | `NewsService.java` + `NewsServiceImpl.java` + `NewsController.java` |

---

## 📊 测试总览

| 层 | 数量 | 状态 |
|---|------|------|
| Crawler 单元测试 | 18 | ✅ |
| Auth API 集成测试 | 8 | ✅ |
| News API 集成测试 | 5 | ✅ |
| Search API 集成测试 | 2 | ✅ |
| Feed API 集成测试 | 2 | ✅ |
| Playwright E2E | 5 | ✅ 已恢复 |
| **合计** | **35 + 5 E2E** | **0 failures** |

---

## 🚀 运行方式

```bash
# 1. 中间件
docker start nexora-mysql nexora-redis nexora-es nexora-rmq-namesrv nexora-rmq-broker nexora-minio nexora-nacos

# 2. 后端
cd backend && mvn clean install -DskipTests && cd nexora-app && mvn spring-boot:run '-Dspring-boot.run.profiles=dev'

# 3. 前端
cd frontend-web && npx vite --port 5173 --host

# 4. E2E 测试
cd frontend-web && npx playwright test
```

---

## 📋 接下来要做

### P3 — 缓存失效 ✅

| 事项 | 说明 |
|------|------|
| **缓存失效策略** | `NewsCacheManager` 统一管理，新文章清除全部，AI 完成清除分类 |

### P4 — DevOps

| 事项 | 说明 |
|------|------|
| CI/CD 流水线 | GitHub Actions（compile → test → e2e → build） |
| 生产环境配置 | application-prod.yml |
| Prometheus + Grafana | 监控大盘 |
| Flutter APP 初始化 | - |
