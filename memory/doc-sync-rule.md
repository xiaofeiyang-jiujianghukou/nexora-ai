---
name: doc-sync-rule
description: Before ending session, sync all related docs (TODO/plan/CLAUDE/memory)
metadata:
  type: feedback
---

# 会话关闭前必须同步所有相关文档

用户要求：每次会话结束前，必须更新所有关联文档，不能只更新某一个。

**关联文档包括但不限于：**
- `docs/TODO.md` — 开发日志和进度
- `CLAUDE.md` — 项目开发指南（含进度快照）
- `memory/` — 会话记忆（handoff、用户偏好等）
- 任何 `docs/designs/` 下的设计文档（如有变更）
- 任何 `docs/*.md` 下的计划文档

**Why:** 用户不希望下次会话时文档之间信息不一致，或遗漏关键上下文。所有文档要作为一个整体保持同步。
**How to apply:** 完成开发工作后、会话结束前，先检查本次会话改动了什么，再逐一更新受影响的文档，最后写 memory 记录下个会话起点。
