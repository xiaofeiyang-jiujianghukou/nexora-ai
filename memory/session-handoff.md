---
name: session-handoff
description: Phase 1 MVP 95% 完成 — 全栈 Docker Compose + K3s 双轨，E2E 13/13，5 语言多语言
metadata:
  type: project
---

# Session Handoff — 下个会话起点

**日期**: 2026-07-17
**分支**: master
**状态**: 🟢 Phase 1 MVP 基本完成（~95%）

---

## Phase 1 MVP 成果速览

```
Docker Compose 生产栈     ✅ 8/8 healthy
K3s 集群全栈部署          ✅ 11 pods Running
E2E 测试                   ✅ 13/13 通过
5 语言多语言               ✅ zh/en/ja/ko/de
ACR 镜像全量迁移           ✅ 构建 20min→3min
ES IK 中文分词             ✅ :8.15.0-ik
推荐引擎                   ✅ 算法上线
CI/CD                      ✅ GitHub Actions → ACR
监控                        ✅ Prometheus + Grafana
```

## 关键发现

1. **Dockerfile JAR Bug**: 项目未用 `spring-boot-starter-parent`，`repackage` goal 必须显式配置
2. **ES IK 插件**: 需要自定义镜像 `elasticsearch:8.15.0-ik`
3. **K3s 镜像导入**: `imagePullPolicy: Always` → `IfNotPresent` + `k3d image import`
4. **LLM 多语言**: DeepSeek 对中文源文章倾向于中文输出，ja/ko/de 需要更强 system prompt

---

## 下次会话起点 → P9：生产环境打磨

### 首选：K3s Ingress
k3d 集群端口 80/443 已映射到 loadbalancer，但缺 ingress controller（Traefik 已禁用）。
```bash
# 安装 ingress-nginx 或启用 Traefik
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/.../deploy.yaml
# → 外部即可通过 localhost:80 直接访问前端
```

### 备选 1：Elasticsearch 全文搜索
当前用 MySQL LIKE，需切换到 ES。App 已配好 ES，后端 `newsSearchRepository` 只需索引数据。

### 备选 2：Flutter APP 跑起来
```bash
cd app && flutter pub get && flutter run
```

### 备选 3：LLM 多语言摘要优化
ja/ko/de 当前回退到 English（en 是默认第二语言）。需要优化 prompt 或换模型，让 LLM 对中文文章也能输出日语/韩语/德语摘要。

---

## 启动命令速查

```bash
# Docker Compose
docker compose -f deploy/docker-compose.yml up -d

# K3s (k3d) 集群
k3d cluster create nexora --config deploy/k3d-config.yaml --image rancher/k3s:v1.34.9-k3s1
k3d image import crpi-.../nexora-app:latest crpi-.../nexora-frontend:latest ... -c nexora
kubectl apply -f deploy/k3s/

# E2E
cd frontend-web && npx playwright test

# 构建 + 推送
docker build -t crpi-.../nexora-app:latest -f backend/Dockerfile backend/
docker push crpi-.../nexora-app:latest
```
