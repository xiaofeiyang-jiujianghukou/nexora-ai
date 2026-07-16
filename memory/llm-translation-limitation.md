---
name: llm-translation-limitation
description: DeepSeek-chat 对中→俄等技术术语翻译有盲区，极端 case 反复重试仍返回原文
metadata:
  type: reference
---

DeepSeek-chat 对中国技术术语→俄语翻译存在能力盲区。测试发现 id=439 文章（"Slack 推出基于智能体的端到端测试..."）俄语翻译反复重试 3 次仍返回中文原文。

其他 6 种语言（zh/en/ja/ko/fr/de）在强制回填后全部通过质量验证（0/20 bad）。

**回退保护**：`getLocalizedContent()` 的 ru→en→null 链确保俄语用户看到英文而非中文。

**可能的改进方向**：
- 两段式翻译：中文→英文→小语种（调用两次 LLM，成本翻倍但翻译质量更高）
- `ai_quality_flag` 标记：对 LLM 翻译失败的文章标记跳过，避免无限重试浪费 token
