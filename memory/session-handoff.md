---
name: session-handoff
description: MVP 全部完成 + 推荐卡片 + 浏览历史 + 缓存失效，17 测试全绿
metadata:
  type: project
---

# Session Handoff — 下个会话起点

**日期**: 2026-07-16
**分支**: master
**状态**: ✅ MVP S0-S6 + P0/P1/P2 + 推荐卡片 + 浏览历史 + 缓存失效 全部完成，17 测试全绿

---

## 本次会话完成的工作

### 首页推荐卡片（前端）
- `home/index.vue`：顶部 "为你推荐 / For You" 横向滚动区
- 复用 `NewsCard`，260px 宽卡片，scroll-snap 对齐，移动端 220px
- `newsStore.fetchRecommendations(limit)` → `GET /api/v1/news/recommendations`
- 匿名用户显示热门，登录用户显示个性化
- i18n：`home.forYou` / `home.basedOnInterests`

### 用户浏览历史引擎
- **Flyway V1.0.9**: `user_browsing_history` 表（user_id + article_id UNIQUE, category_id 冗余）
- **Entity + Mapper**: `UserBrowsingHistoryDO` + `UserBrowsingHistoryMapper`
- **记录时机**: `getDetail(id, userId)` → 登录用户自动 upsert 浏览历史
- **兴趣向量增强**: `buildInterestVector(favorites, userId)` → 收藏 1.0x + 浏览 0.5x → 归一化
- **容错**: 浏览记录失败不影响主流程（try-catch + warn 日志）

---

## 兴趣向量算法

```
buildInterestVector(favorites, userId):
  raw[cat] += favoriteCount × 1.0    // 显式正向信号
  raw[cat] += browseCount × 0.5      // 隐式兴趣信号（最近 100 条）
  vector[cat] = raw[cat] / Σ raw     // 归一化
```

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

### 缓存失效策略 ✅
- `NewsCacheManager`：统一管理 Redis 缓存失效
- `evictAll()`：新文章入库 → 清除 `news:list:*`（注入到 `NewsCollectScheduler`）
- `evictByCategory()`：AI 分析完成 → 清除 `news:list:*:{cat}:*`（注入到 `AIAnalysisService`）
- Redis 不可用时自动降级（log debug + skip）

---

## 下个会话起点

### P3 DevOps
- CI/CD 流水线（GitHub Actions：compile → test → e2e → build）
- 生产环境配置 application-prod.yml
- Prometheus + Grafana 监控大盘
- Flutter APP 初始化
```

---

## 架构速览

```
浏览历史记录:
  GET /api/v1/news/{id} (已登录)
    → NewsController.detail(id)
      → NewsServiceImpl.getDetail(id, userId)
        → recordBrowsingHistory(userId, articleId, categoryId)  // upsert

推荐引擎（增强）:
  GET /api/v1/news/recommendations?limit=20
    → NewsController.recommendations()
      → NewsService.getRecommendations(userId, limit)
        → 冷启动 (< 3 favs): getHotList()
        → 正常推荐: buildInterestVector(favorites + browsing) → computeScore() → top N
```
