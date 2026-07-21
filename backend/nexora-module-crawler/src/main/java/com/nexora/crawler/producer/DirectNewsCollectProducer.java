package com.nexora.crawler.producer;

import com.nexora.ai.service.AIAnalysisService;
import com.nexora.common.event.NewsAITaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 直接 AI 分析触发器 — RocketMQ 不可用时，绕过 MQ 直接调用 AIAnalysisService。
 * 当 {@code nexora.rocketmq.enabled=false} 时激活。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "false")
public class DirectNewsCollectProducer {

    private final AIAnalysisService aiAnalysisService;

    public DirectNewsCollectProducer(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * 异步触发 AI 分析（不阻塞采集主流程）
     */
    @Async("aiAnalysisExecutor")
    public void sendAITask(NewsAITaskEvent event) {
        log.info("Direct AI task for article: {}", event.getArticleId());
        try {
            aiAnalysisService.process(event);
        } catch (Exception e) {
            log.error("Direct AI analysis failed for article {}: {}", event.getArticleId(), e.getMessage());
        }
    }

    /**
     * 采集完成（精简模式下仅打日志）
     */
    public void sendCollected(Long articleId, String title) {
        log.debug("Article collected (slim mode): id={}, title={}", articleId,
                title != null && title.length() > 60 ? title.substring(0, 60) : title);
    }
}
