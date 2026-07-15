# Nexora AI — 待办事项

> 最后更新：2026-07-15

## 当前状态

| Sprint | 内容 | 测试 |
|--------|------|------|
| ✅ Sprint 0 | 项目骨架 (Maven 10模块 + Docker Compose + Vue3) | 编译通过 |
| ✅ Sprint 1 | 用户模块 (注册/登录/JWT/个人信息) | 8 API tests |
| ✅ Sprint 2 | 新闻核心模块 (CRUD/收藏/分类/前端页面) | 5 API tests |
| ✅ Sprint 3 | 采集核心逻辑 (清洗/去重/评分/RSS/ContentCleaner/DuplicateDetector/QualityScorer) | 18 unit tests |
| ✅ Sprint 4 | AI 模块 (DeepSeek/Mock Provider + PromptManager + NewsAIManager) | 架构就绪 |
| ✅ Sprint 5 | 搜索模块 (MySQL全文搜索/建议/前端) | 2 API tests |
| ✅ Sprint 6 | Feed + 订阅 (首页信息流/FeedManager/订阅管理) | 2 API tests |
| ✅ E2E | Playwright 浏览器端到端测试 (注册/登录/浏览/分类/搜索) | 4 E2E tests |
| ✅ 基础设施 | Docker Compose 7容器全部启动 (MySQL/Redis/ES/RocketMQ/Nacos/MinIO) | healthy |
| ✅ 数据库 | Flyway 自动迁移 15张表 + 8条测试新闻数据 | 已验证 |

**总测试: 39 passed, 0 failures ✅**

---

## 运行环境

```
Docker Compose:   7/7 healthy (全部使用阿里云个人镜像仓库)
Spring Boot:      http://localhost:8080
Vue3 Frontend:    http://localhost:5173
Swagger UI:       http://localhost:8080/swagger-ui/index.html

MySQL:    jdbc:mysql://localhost:3306/nexora (root/root)
Redis:    localhost:6379
ES:       localhost:9200
RocketMQ: localhost:9876 (namesrv) / :10911 (broker)
Nacos:    localhost:8848
MinIO:    localhost:9000 / :9001

LLM:      $env:LLM_API_KEY="sk-..." 设置后自动启用 DeepSeekProvider
```

---

## 存储过程

### 插入测试数据（UTF-8 安全方式）
```powershell
Get-Content deploy\seed-data.sql -Raw | docker exec -i nexora-mysql mysql -uroot -proot --default-character-set=utf8mb4 nexora
```

### 启动全栈
```bash
# 1. 中间件
cd deploy && docker compose -f docker-compose-dev.yml up -d

# 2. 后端
cd backend && mvn clean install -DskipTests -q
mvn spring-boot:run -pl nexora-app "-Dspring-boot.run.profiles=dev"

# 3. 前端
cd frontend-web && npm install && npm run dev
```

### 运行测试
```bash
# 全部后端测试
cd backend && mvn test

# E2E 浏览器测试
cd frontend-web && npx playwright test
```

---

## 剩余待办

### Sprint 3: 采集对接 (核心逻辑已完成，待 MQ 层)
- [ ] RocketMQ Topic 创建
- [ ] XXL-JOB 调度中心部署
- [ ] NewsCollectedEvent 消息发送
- [ ] NewsRawConsumer 消费 → 入库
- [ ] 真实 RSS 源 URL 配置

### Sprint 4: AI 对接 (代码已完成，待 API Key)
- [ ] 设置 `$env:LLM_API_KEY` → 自动启用 DeepSeek
- [ ] 真实新闻 AI 摘要/分类/实体识别验证
- [ ] NewsAIConsumer MQ 消费

### 前端增强
- [ ] 暗黑模式切换按钮 (CSS变量已准备)
- [ ] 语言切换按钮 (i18n已配置)
- [ ] 个人中心/设置页面完善
- [ ] 响应式适配移动端

### 基础设施完善
- [ ] RocketMQ Topic 创建脚本
- [ ] ES IK 分词器安装 + Mapping 创建
- [ ] XXL-JOB Admin 部署
- [ ] application-staging.yml / application-prod.yml
- [ ] Flutter APP 项目初始化
- [ ] CI/CD 流水线 (GitHub Actions)
- [ ] 监控: Prometheus + Grafana Dashboard
