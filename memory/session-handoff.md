---
name: session-handoff
description: 下个会话起点 — 中英文内容切换完善
metadata:
  type: project
---

# Session Handoff — 下个会话起点

**日期**: 2026-07-15 (end of session)
**分支**: master
**状态**: P1 完成, P2 部分完成, 中英文切换有遗留

## 当前问题

**中英文切换不完整**：导航标签等 UI 文字能切换，但新闻卡片和详情页的摘要始终显示中文。
用户切换 EN 后，期望看到英文摘要。

## 根因

AI 已产出双语摘要存入 `ai_result` JSON 列：
```json
{"zh":{"summary":"中文摘要..."}, "en":{"summary":"English summary..."}, "category":"...", ...}
```

但前端没有读取和切换这个字段：
- NewsCard: `summaryText` 检查 `item.summaryEn`（已不存在的列）
- NewsDetail: 没有从 `ai_result` 取对应语言
- 后端 API 返回的 VO 没有包含 `aiResult` 字段

## 需要做的事

1. **后端** — 修改新闻列表/详情 API，返回 `ai_result` 中当前语言对应的 summary
   - 方案A: API 返回 `aiResult` JSON，前端解析
   - 方案B: 后端根据 `Accept-Language` header 返回对应语言内容
2. **前端** — NewsCard + NewsDetail 根据 `locale` 从 `ai_result` 取对应语言摘要
3. **验证** — 切换 EN 后新闻卡片和详情页显示英文摘要

## P1 已完成（已验证）

- RocketMQ Topic 创建 ✅
- AI Consumer 对接 MQ ✅
- Spring @Scheduled RSS 采集 ✅
- RSS 源种子数据 ✅
- ES IK 分词器 + Mapping ✅
- 端到端管道：RSS → MQ → AI → DB → MQ → ES ✅
- AI 双语摘要产出 `ai_result` JSON ✅
- 130 篇文章 / 117 篇有双语摘要 ✅

## P2 部分完成

- 暗黑模式切换 ✅
- 个人中心 ✅
- 共享 AppLayout ✅
- 响应式 ✅
- 中英文切换 — UI 标签 ✅，内容切换 ❌

## 下个会话从这里开始

1. 后端 API 返回 `aiResult` 或按语言返回对应摘要
2. 前端 NewsCard 根据 locale 显示对应语言摘要
3. 前端 NewsDetail 根据 locale 显示对应语言内容
4. 浏览器验证完整中英文切换效果

## 已推送

- commit `923bf0c` on master
- 46 files, +1898/-429 lines
