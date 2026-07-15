package com.nexora.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 分析任务事件 — MQ 消息只传 articleId，消费者从 DB 读取完整内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsAITaskEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新闻文章 ID */
    private Long articleId;
}
