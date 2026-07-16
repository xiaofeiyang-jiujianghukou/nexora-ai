---
name: no-downgrade-rule
description: 禁止降级 — 工具不可用时必须修复根因，不用 CLI 或低阶方案替代
metadata:
  type: feedback
---

# AI 协作铁律：禁止降级

当 MCP 工具、Playwright 浏览器或其他高阶能力不可用时，**绝不**用 CLI 替代方案或其他降级手段凑合。

- 浏览器验证 → 必须用 Playwright MCP（`browser_navigate`/`browser_take_screenshot`），不能退到 `curl` 或 `npx playwright test`
- MCP 不可用 → 诊断根因并修复，不能绕过
- 任何验证手段必须与设计目标一致

**Why:** 用户多次强调"不要降级"，降级方案无法提供与原始设计等同的验证质量（如可视化截图 vs 纯文本 curl）。

**How to apply:** 遇到工具不可用时，先排查配置、权限、版本等根因，修复它。如果确实需要重启会话才能生效，明确告知用户并在 handoff 中记录。
