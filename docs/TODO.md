# Nexora AI — 开发日志

> 最后更新：2026-07-15 (P1+P2 completed, AI bilingual done)    
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

### P0 — 立即可做 ✅ (已完成)

| 事项 | 说明 | 状态 |
|------|------|------|
| **设置 LLM_API_KEY** | `$env:LLM_API_KEY` 已配置为全局环境变量 | ✅ |
| **插入测试数据** | 已验证：4 用户 / 8 文章 / 5 来源 / 7 分类 | ✅ |
| **运行全量测试** | 后端 35 + E2E 4 = 39 tests，0 failures | ✅ |

## 🔜 下个会话从这里开始 → P2

### P1 — 消息流水线 ✅ (已完成)

**于 2026-07-15 完成：**

| # | 事项 | 状态 | 说明 |
|---|------|------|------|
| 1 | **RocketMQ Topic 创建** | ✅ | `RocketMQConfig` 启动时自动创建 3 个 Topic；Maven 依赖+配置完整 |
| 2 | **AI Consumer 对接 MQ** | ✅ | `AITaskConsumer` → `NewsAIManager.analyze()` → 写回文章摘要+分类 → 发送 ES 索引任务 |
| 3 | **调度中心** | ✅ | 使用 Spring `@Scheduled`（替代 XXL-JOB），`NewsCollectScheduler` 每 10 分钟扫描 RSS |
| 4 | **RSS 源配置** | ✅ | Flyway V1.0.1 插入 11 个 RSS 源（TechCrunch/BBC/Reuters/36氪等） |
| 5 | **ES IK 分词器** | ✅ | `IndexTaskConsumer` → ES 索引；`ESIndexInitializer` 启动建索引；`install-es-ik.ps1` |

**消息流水线链路已完整打通：**
```
RSS采集 → MQ(collected) → 清洗入库 → MQ(ai-task) → AI分析 → DB更新 → MQ(index-task) → ES索引
```

### P2 — 前端增强 ✅ (已完成)

| 事项 | 说明 | 状态 |
|------|------|------|
| **暗黑模式切换按钮** | 头部 Moon/Sun 图标一键切换；`data-theme` + `html.dark` 双通道；Element Plus 深色同步 | ✅ |
| **中/英文切换按钮** | 头部 EN/中文 按钮；`el-config-provider` 同步 Element Plus 国际化；**内容摘要切换待完善** | ⚠️ 部分完成 |
| **个人中心页面完善** | 用户信息展示 + 编辑昵称；主题/语言设置卡片；ElSwitch 切换 | ✅ |
| **共享布局组件** | `AppLayout.vue` 统一头部（Logo + 搜索 + 导航 + 主题 + 语言 + 用户菜单） | ✅ |
| **响应式移动端适配** | 全部页面添加 `@media (max-width: 768px)` 断点；导航文字隐藏；搜索栏折叠 | ✅ |

**新增/修改文件：**
| 文件 | 说明 |
|------|------|
| `src/stores/settingsStore.ts` | 主题 + 语言管理（localStorage 持久化） |
| `src/components/layout/AppLayout.vue` | 共享布局组件（头部 + slot） |
| `src/App.vue` | `el-config-provider` 动态国际化；auth 页面排除布局 |
| `src/main.ts` | Element Plus 暗黑 CSS；移除硬编码 locale |
| `src/env.d.ts` | Element Plus locale MJS 类型声明 |
| `src/pages/home/index.vue` | 移除自有 header，使用共享布局 |
| `src/pages/user/profile.vue` | 完整个人信息 + 主题/语言设置 |
| `src/pages/news/detail.vue` | 移除自有 header，添加响应式 |
| `src/pages/search/index.vue` | 移除自有 header，添加搜索引导 |
| `src/pages/user/favorites.vue` | 移除自有 header，添加响应式 |
| `src/pages/user/subscriptions.vue` | 移除自有 header，添加响应式 |
| `src/router/index.ts` | auth 页面添加 `plain: true` meta |

### AI 双语摘要 ✅

- `ai_result` JSON 列：`{"zh":{...},"en":{...}}`，可无限扩展语言
- 中英文 prompt 各有 schema 定义 + 示例输出
- `responseLen` 从 2 字符修复到 200~578 字符

### P3 — DevOps

| 事项 | 说明 |
|------|------|
| CI/CD 流水线 | GitHub Actions（compile → test → e2e → build） |
| 生产环境配置 | application-prod.yml |
| Prometheus + Grafana | 监控大盘 |
| Flutter APP 初始化 | - |
