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

## AI 协作铁律

1. **禁止降级**：当某个工具/能力不可用时（如 MCP、Playwright 浏览器），**绝不**使用 CLI 替代方案或其他降级手段。必须诊断并修复根因，让该工具正常工作。验证手段必须与设计目标一致 — 浏览器验证就用浏览器工具，不要退而求其次用 curl 或 CLI 测试凑合。
2. **先诊断后修复**：遇到问题先找到根因，再动手。不要绕过问题。
3. **卡住超过 10s 立即求助**：分析问题、查找根因时，如果超过 10 秒还没找到明确方向，立即停下来向用户求助，不要陷入长时间的自我推理循环。

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

