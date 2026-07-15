package com.nexora.ai.provider;

/**
 * LLM Provider 接口 — 统一多模型调用
 */
public interface LLMProvider {

    /**
     * 同步聊天调用
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return AI 响应文本
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Provider 名称
     */
    String getProviderName();

    /**
     * 使用的模型名称
     */
    String getModelName();
}
