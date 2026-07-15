# Nexora AI — 开发日志

> 最后更新：2026-07-15  
> GitHub：https://github.com/xiaofeiyang-jiujianghukou/nexora-ai.git

---

## ✅ 已完成

### Sprint 0：项目骨架
- Maven 10 模块工程（common / api / app / user / news / ai / search / crawler / notification）
- Docker Compose 7 个中间件（MySQL / Redis / ES / RocketMQ / Nacos / MinIO），全部使用阿里云个人镜像仓库
- Vue3 + Vite + TypeScript + Element Plus + Pinia 前端骨架
- Spring Security + JWT 认证体系
- Flyway 数据库迁移（15 张表自动建表）
- 统一返回体 `Result<T>`、全局异常处理、27 个错误码
- `.gitignore` 覆盖 node_modules / target / test-results / .idea

### Sprint 1：用户模块
- 注册 / 登录 / Token 刷新 / 个人信息 CRUD（6 个端点）
- BCrypt 密码加密、JWT 双 Token 机制
- 前端登录 + 注册页面、路由守卫、authStore

### Sprint 2：新闻核心模块
- 新闻分页列表 / 详情 / 相关新闻 / 分类（4 个端点）
- 收藏添加 / 删除 / 列表（3 个端点）
- NewsCard 组件、首页、详情页、收藏页

### Sprint 3：采集核心逻辑（纯逻辑层，MQ 层待对接）
- ContentCleaner（HTML 去除 / 广告过滤 / 空白标准化）
- DuplicateDetector（URL Hash + SimHash + 汉明距离）
- QualityScorer（来源权重 + 内容长度 + 域名权威度）
- RSSCollector（Rome 库通用 RSS/Atom 采集）

### Sprint 4：AI 模块（代码就绪，待 API Key）
- LLMProvider 接口 + DeepSeekProvider（真实调用）+ MockLLMProvider（默认降级）
- PromptManager（YAML 模板加载 + 变量渲染）
- NewsAIManager（AI 分析编排：摘要 → 分类 → 实体识别）
- 配置：`$env:LLM_API_KEY` 设置后自动启用 DeepSeek

### Sprint 5：搜索模块
- MySQL LIKE 全文搜索 + 搜索建议（2 个端点，ES 骨架已预留）
- 前端搜索页面

### Sprint 6：Feed + 订阅
- FeedManager + FeedController（首页热点 + 按分类分组）
- 订阅创建 / 删除 / 列表（3 个端点）
- 前端订阅管理页面

### E2E 测试
- Playwright 4 个浏览器测试：注册登录 / 新闻浏览 / 分类筛选 / 搜索

---

## 📊 测试总览

| 层 | 数量 | 状态 |
|---|------|------|
| Crawler 单元测试 | 18 | ✅ |
| Auth API 集成测试 | 8 | ✅ |
| News API 集成测试 | 5 | ✅ |
| Search API 集成测试 | 2 | ✅ |
| Feed API 集成测试 | 2 | ✅ |
| Playwright E2E | 4 | ✅ |
| **合计** | **39** | **0 failures** |

---

## 🚀 运行方式

```bash
# 1. 中间件
cd deploy && docker compose -f docker-compose-dev.yml up -d

# 2. 后端
cd backend && mvn clean install -DskipTests -q
mvn spring-boot:run -pl nexora-app "-Dspring-boot.run.profiles=dev"

# 3. 前端
cd frontend-web && npm install && npm run dev

# 访问
# 前端:  http://localhost:5173
# 后端:  http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

---

## 📋 接下来要做

### P0 — 立即可做

| 事项 | 说明 |
|------|------|
| **设置 LLM_API_KEY** | `$env:LLM_API_KEY="sk-..."` 然后重启后端，AI 摘要/分类/实体识别自动启用 |
| **插入测试数据** | `Get-Content deploy\seed-data.sql -Raw \| docker exec -i nexora-mysql mysql -uroot -proot --default-character-set=utf8mb4 nexora` |
| **运行全量测试** | `cd backend && mvn test` + `cd frontend-web && npx playwright test` |

### P1 — 需要额外配置

| 事项 | 依赖 |
|------|------|
| **RocketMQ Topic 创建** | `nexora-news-collected` / `nexora-news-ai-task` / `nexora-news-index-task` |
| **XXL-JOB 调度中心部署** | 用于定时 RSS 采集 |
| **ES IK 分词器 + Mapping** | 切换搜索为 Elasticsearch |
| **RSS 源 URL 配置** | 在 `news_collection_source` 表录入真实 RSS 地址 |
| **AI Consumer 对接 MQ** | 消费 `news-ai-task` → 自动 AI 分析 |

### P2 — 前端增强

| 事项 | 说明 |
|------|------|
| 暗黑模式切换按钮 | CSS 变量已准备，只需加个 toggle |
| 中/英文切换按钮 | i18n 已配置 60+ 翻译条 |
| 个人中心页面完善 | 当前为占位页 |
| 响应式移动端适配 | - |

### P3 — DevOps

| 事项 | 说明 |
|------|------|
| CI/CD 流水线 | GitHub Actions（compile → test → e2e → build） |
| 生产环境配置 | application-prod.yml |
| Prometheus + Grafana | 监控大盘 |
| Flutter APP 初始化 | - |
