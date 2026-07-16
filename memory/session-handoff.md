---
name: session-handoff
description: CI/CD 流水线已搭建，17 测试全绿，下个会话可选：Docker Registry 推送 / 生产配置 / E2E 补齐
metadata:
  type: project
---

# Session Handoff — 下个会话起点

**日期**: 2026-07-17
**分支**: master
**状态**: ✅ CI/CD Pipeline 已搭建（GitHub Actions），本地验证通过

---

## 本次会话完成的工作

### CI/CD 流水线（GitHub Actions）
→ `.github/workflows/ci.yml`

**4 阶段流水线：**

| Job | 内容 | 触发 |
|-----|------|------|
| `backend-test` | Java 17, Maven compile + `mvn verify` (H2 unit tests) | push/PR |
| `frontend-build` | Node 20, `npm ci`, `vue-tsc --noEmit`, `vite build` | push/PR |
| `e2e` | MySQL + Redis service containers, backend + frontend + Playwright | push/PR (needs backend-test) |
| `build` | JAR + dist artifacts upload (7-day retention) | push to master/develop only |

**关键决策：**
- Java 17（对齐 pom.xml，不是 CLAUDE.md 说的 21 — pom.xml 是权威源）
- npm（对齐 package-lock.json，不是 CLAUDE.md 说的 pnpm）
- E2E 只依赖 MySQL + Redis（RocketMQ 有容错，Nacos 代码中未使用，ES 太重暂不包含）
- `workflow_dispatch` 支持手动触发

### 后端 Dockerfile
→ `backend/Dockerfile`

多阶段构建：`maven:3.9-eclipse-temurin-17` → `eclipse-temurin:17-jre-alpine`

---

## 本地验证结果

- ✅ `vue-tsc --noEmit` — 零类型错误
- ✅ `vite build` — 构建成功 (15s)
- ✅ `mvn verify` — 17 tests passed (上次会话)
- ⏳ GitHub Actions — 待 push 到 GitHub 后验证

---

## 启动命令

```bash
# 中间件
docker start nexora-mysql nexora-redis nexora-es nexora-rmq-namesrv nexora-rmq-broker nexora-minio nexora-nacos

# 后端
cd backend && mvn clean install -DskipTests && cd nexora-app && mvn spring-boot:run '-Dspring-boot.run.profiles=dev'

# 前端
cd frontend-web && npx vite --port 5173 --host

# E2E 测试
cd frontend-web && npx playwright test
```

---

## 下个会话建议

### 首选：Push → 验证 CI 流水线

当前 CI 仅在本地验证过，需要 push 到 GitHub 触发真实运行：

```bash
git add .github/workflows/ci.yml backend/Dockerfile
git commit -m "feat(ci): GitHub Actions CI/CD pipeline with compile, test, e2e, build"
git push origin master
```

然后去 GitHub Actions 标签页看运行结果。可能需要的调整：
- E2E 的 MySQL/Redis service containers 启动时序
- 首次 Maven 依赖下载可能超时（cache 尚未填充）
- Playwright browser install 可能缺少系统依赖

### 备选 1：配置 Docker Registry 推送

当前 Dockerfile 已就绪，只需：
1. 在 GitHub Secrets 配置 `DOCKER_USERNAME` / `DOCKER_PASSWORD`（或阿里云 ACR 凭证）
2. 在 `build` job 加一步 `docker build && docker push`

### 备选 2：生产环境配置

- `application-prod.yml`：生产 MySQL/Redis/ES/RocketMQ 连接参数
- K3s 部署清单 `deploy/k3s/`

### 后续队列
- Flutter APP 初始化
- Prometheus + Grafana 监控大盘
- E2E 测试补齐（收藏流程、推荐卡片验证、多语言切换深度验证）
- 性能测试（k6 / JMeter）

---

## 架构速览

```
CI/CD Pipeline:
  push/PR → backend-test (mvn verify)
         → frontend-build (vue-tsc + vite build)
         → e2e (MySQL + Redis + backend + Playwright)
         → build (JAR + dist artifacts) [master/develop only]

Dockerfile:
  maven:3.9 → compile → eclipse-temurin:17-jre-alpine → app.jar
```
