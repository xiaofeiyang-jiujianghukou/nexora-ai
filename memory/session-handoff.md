---
name: session-handoff
description: 全部镜像迁移到 ACR，Docker Compose 生产栈就绪，Dockerfile JAR 通配符 bug 待验证
metadata:
  type: project
---

# Session Handoff — 下个会话起点

**日期**: 2026-07-17
**分支**: master
**状态**: 🟡 镜像全部迁移到 ACR，Dockerfile JAR 修复待 CI 重建验证

---

## 本次会话完成

### 1. CI/CD 流水线（GitHub Actions）
→ `.github/workflows/ci.yml`

| Job | 内容 |
|-----|------|
| `backend-compile` | Java 17, `mvn clean install -DskipTests` |
| `frontend-build` | Node 22, `npm ci`, `vue-tsc --noEmit`, `vite build` |
| `build` | 编译 JAR → 构建 Docker 镜像 → 推送到 **ACR** |

### 2. 镜像仓库 — 全部迁移到 ACR
```
crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/
├── nexora-app:latest          ← CI 自动推
├── nexora-frontend:latest     ← CI 自动推
├── mysql:8.0
├── redis:7-alpine
├── elasticsearch:8.15.0
├── rocketmq:5.2.0
├── nacos-server:v2.3.2
├── minio:latest
├── prometheus:v3.3.0
├── grafana:11.6.0
└── + k3s 系统镜像 × 5
```

GitHub Secrets 已配：`ACR_REGISTRY` / `ACR_USERNAME` / `ACR_PASSWORD`

### 3. 生产配置 + K3s 清单
- `backend/.../application-prod.yml` — 生产环境配置
- `deploy/k3s/` — 10 个 K8s 清单 + `deploy.sh` 一键部署
- `deploy/k3d-config.yaml` — k3d 集群配置
- `deploy/docker-compose.yml` — **生产栈：10 个服务全套**

### 4. Docker Compose 生产栈
```bash
docker compose -f deploy/docker-compose.yml up -d
```
| 层 | 服务 |
|----|------|
| 数据 | mysql, redis, elasticsearch |
| 消息 | rocketmq-namesrv, rocketmq-broker |
| 应用 | nexora-backend, nexora-frontend |
| 监控 | prometheus, grafana |

### 5. Flutter APP
→ `app/` — Riverpod + GoRouter + Dio + freezed
- 需装 Flutter SDK 后 `flutter pub get && flutter run`

### 6. 监控
→ `deploy/monitoring/`
- Prometheus v3.3.0 + Grafana 11.6.0
- JVM/HTTP/DB/GC 仪表盘
- 已集成到 docker-compose.yml 和 K3s 清单

### 7. E2E 测试
→ `frontend-web/e2e/` — 8 个 spec
- auth, news, search, verify-lang-switch, verify-visual
- favorites, recommendations, multilang-deep（新增）

### 8. 集成测试
→ Testcontainers + 真实 MySQL（ACR 镜像），17 个测试

---

## Dockerfile JAR 通配符 Bug（当前卡点）

**根因**：`COPY --from=build /app/nexora-app/target/nexora-app-*.jar app.jar`

Spring Boot 打包产生两个文件：
- `nexora-app-1.0.0-SNAPSHOT.jar` — fat JAR（~50MB）
- `nexora-app-1.0.0-SNAPSHOT.jar.original` — 原始 JAR（~28KB）

通配符 `*.jar` 同时匹配两者，Docker COPY 可能选到 `.original`（无 Main-Class）。

**已修复** → `backend/Dockerfile` line 14-15：
```dockerfile
RUN mvn clean package -DskipTests -pl nexora-app -am -q && \
    cd /app/nexora-app/target && \
    mv nexora-app-*.jar app.jar && \
    rm -f *.original
COPY --from=build /app/nexora-app/target/app.jar app.jar
```

---

## 下个会话起点

### 首选：验证 Dockerfile 修复 + 部署全栈

```bash
# 1. 重建并推送后端镜像到 ACR
#    （等 CI 跑完或本地 docker build && docker push）

# 2. 一键启动全套
docker compose -f deploy/docker-compose.yml up -d

# 3. 健康检查
curl http://localhost:8080/actuator/health
curl http://localhost:80

# 4. 监控
open http://localhost:3000  # Grafana admin/admin
open http://localhost:9090  # Prometheus

# 5. E2E 验证
cd frontend-web && npx playwright test
```

### 备选 1：端到端验证 + E2E 全绿
- 启动完整栈 → 运行全部 8 个 E2E spec → 确认全绿

### 备选 2：K3s 实际部署
- 用 k3d 创建集群 → 导入 ACR 镜像 → `bash deploy/k3s/deploy.sh` → 验证

### 后续队列
- Flutter APP 跑起来（需装 Flutter SDK）
- Sentry / ELK 日志收集
- k6 性能测试

---

## 启动命令速查

```bash
# 生产栈（Docker Compose）
docker compose -f deploy/docker-compose.yml up -d

# 开发模式（本地编译）
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd frontend-web && npx vite --port 5173 --host

# K3s
bash deploy/k3s/deploy.sh

# E2E
cd frontend-web && npx playwright test
```
