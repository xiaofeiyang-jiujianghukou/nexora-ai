---
name: session-handoff
description: 下个会话起点 — 浏览器验证 7 种语言全流程
metadata:
  type: project
---

# Session Handoff — 下次从此开始

**日期**: 2026-07-16
**分支**: master
**状态**: 后端 7 语言 + 前端下拉菜单就绪，待浏览器全流程验证

## 下次会话起点

**直接浏览器验证 7 种语言**:

1. 首页已打开 → 下拉菜单验证（7 种语言已确认显示）
2. 逐语言切换 → 截图检查 UI 翻译 + AI 摘要内容
3. 进入详情页 → 切换语言 → 验证标题/摘要/事实/背景/影响联动
4. 无限滚动加载更多
5. 搜索页 + 登录页功能验证

## 语言下拉菜单（已验证）

| # | 显示 | locale |
|---|------|--------|
| 1 | 中文 ✓ | zh-CN |
| 2 | EN | en-US |
| 3 | 日本語 | ja-JP |
| 4 | 한국어 | ko-KR |
| 5 | Français | fr-FR |
| 6 | Deutsch | de-DE |
| 7 | Русский | ru-RU |

## 启动命令

```bash
# Docker 中间件
docker start nexora-mysql nexora-redis nexora-es nexora-rmq-namesrv nexora-rmq-broker nexora-minio nexora-nacos

# 后端
cd backend/nexora-app && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 前端
cd frontend-web && npx vite --port 5173 --host
```

## 新增语言步骤（备忘）

后端: `NewsAIManager.TARGET_LANGUAGES` 加 `LangDef`
前端: `config.ts` SUPPORTED_LOCALES + DISPLAY_NAMES + `locales/xx.json` + `index.ts` import
