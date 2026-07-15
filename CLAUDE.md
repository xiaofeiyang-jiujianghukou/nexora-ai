# CLAUDE.md — Nexora AI 项目开发指南

> **GitHub**: https://github.com/xiaofeiyang-jiujianghukou/nexora-ai.git

## 项目概述

**Nexora AI** 是一个全球智能信息平台，第一阶段产品为 **Nexora News** — AI 驱动的新闻理解平台。

> 核心使命：用 AI 快速发现、理解和追踪全球重要信息。

## 技术栈

| 层 | 技术 |
|---|------|
| **后端** | Java 21, Spring Boot 3.x, Spring Cloud Alibaba, MyBatis-Plus |
| **前端 Web** | Vue 3, TypeScript, Vite, Pinia, Element Plus |
| **APP** | Flutter, Dart, Riverpod |
| **数据库** | MySQL 8, Redis, Elasticsearch |
| **消息队列** | RocketMQ |
| **AI** | AI Gateway → DeepSeek / OpenAI / Qwen / Claude |
| **部署** | Docker, Kubernetes/K3s |
| **监控** | Prometheus, Grafana, SkyWalking, ELK |

## 架构演进

```
Phase 1: 模块化单体 → Phase 2: 热点服务拆分 → Phase 3: 完整微服务
```

当前阶段：**Phase 1 — 模块化单体（单 Spring Boot 应用，Maven 多模块）**

## Maven 模块结构

```
nexora-backend/
├── nexora-common          # 公共基础（异常、返回体、工具类、枚举）
├── nexora-api             # DTO/Feign 接口（未来微服务二方包）
├── nexora-app             # 唯一启动模块（@SpringBootApplication）
├── nexora-module-news     # 新闻模块
├── nexora-module-ai       # AI 模块（AI Gateway + LLM Provider）
├── nexora-module-search   # 搜索模块（Elasticsearch）
├── nexora-module-user     # 用户模块
├── nexora-module-crawler  # 采集模块（RSS/API/Crawler）
└── nexora-module-notification  # 通知模块
```

## 内部代码架构（Lean DDD）

```
controller → manager (可选，仅复杂流程) → service → mapper → entity
```

- **Controller**：HTTP 入口，参数校验，调用业务，**禁止**包含 SQL/业务逻辑/MQ
- **Manager**：仅编排多领域 Service/Client 的复杂流程（如 NewsPublishManager）
- **Service**：单一领域职责，接口+实现分离
- **Mapper**：MyBatis-Plus BaseMapper
- **DO**（entity）→ **DTO**（模块通信）→ **VO**（返回前端）严格分离

## 关键设计原则

1. 不为了架构而架构
2. 不为了 DDD 增加无价值层
3. 不为了微服务提前拆分
4. 业务复杂度驱动架构复杂度
5. 从模块化单体平滑演进到微服务

## Git 工作流

```
main ← release/* ← develop ← feature/{sprint}-{task}-{desc}
```

Commit 规范：`<type>(<scope>): <subject>`（feat/fix/docs/refactor/test/chore）

## 测试与验收

- **API 测试 100% 通过** → 所有端点必须覆盖（`mvn verify`）
- **浏览器 E2E 测试 100% 通过** → 核心用户流程（Playwright）
- **代码覆盖率 > 70%**（Service 层 > 80%）
- CI 任一项失败 → **禁止合并**

## 文档索引

| 文档 | 路径 |
|------|------|
| 产品需求架构设计 | `docs/designs/Nexora AI 产品需求架构设计 v1.0.md` |
| 技术架构详细设计 | `docs/designs/Nexora AI 技术架构详细设计 v1.0.md` |
| 数据库详细设计 | `docs/designs/Nexora AI 数据库详细设计 v1.0.md` |
| 新闻处理流水线设计 | `docs/designs/Nexora AI 新闻处理流水线详细设计.md` |
| 微服务工程落地方案 | `docs/designs/Nexora AI 微服务详细设计与工程落地方案.md` |
| API + 业务流程设计 | `docs/designs/Nexora AI API详细设计 + 核心业务流程设计 v1.0.md` |
| 前端架构 + 交互设计 | `docs/designs/Nexora AI 前端架构设计 + WebApp交互设计 v1.0.md` |
| 开发计划与规范 | `docs/Nexora AI 开发计划与规范 v1.0.md` |

## 快速启动（开发环境）

```bash
# 1. 启动中间件
cd deploy && docker compose -f docker-compose-dev.yml up -d

# 2. 启动后端
cd backend && mvn clean install -DskipTests
cd nexora-app && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 启动前端
cd frontend-web && pnpm install && pnpm run dev
```

## 当前进度快照 (2026-07-15)

> **详细任务清单见** `docs/TODO.md`

### 已完成
- ✅ Sprint 0~6：项目骨架、用户、新闻、采集、AI、搜索、Feed+订阅
- ✅ P0：LLM_API_KEY 已配置 / 种子数据已灌入（4 用户 8 文章） / 39 测试全过
- ✅ 中间件全部运行（MySQL / Redis / ES / RocketMQ / Nacos / MinIO）

### 🔜 下个会话：P1 — 消息流水线对接

按推荐顺序执行：
1. **RocketMQ Topic 创建**（3 个 Topic）
2. **AI Consumer 对接 MQ**（消费 → 自动 AI 分析）
3. **XXL-JOB 调度中心部署**（定时采集）
4. **RSS 源 URL 配置**（端到端验证）
5. **ES IK 分词器 + Mapping**（搜索升级）

### 关键环境信息
| 项 | 值 |
|----|----|
| LLM_API_KEY | 已设为全局环境变量 ✅ |
| MySQL | `nexora-mysql` 容器运行中，端口 3306 |
| 数据库 | `nexora`，18 张表（Flyway 管理） |
| 测试 | 39 tests / 0 failures |
| 前端测试 | Playwright E2E 4 个全部通过 |

### 会话关闭前必做
在结束会话前，需要更新所有相关文档（TODO.md / CLAUDE.md / memory/），并明确记录下个会话的起点。
