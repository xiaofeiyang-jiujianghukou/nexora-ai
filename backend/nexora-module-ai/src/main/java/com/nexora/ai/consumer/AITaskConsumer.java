package com.nexora.ai.consumer;

import com.nexora.ai.service.AIAnalysisService;
import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * AI 任务消费者 — 消费 nexora-news-ai-task，自动执行 AI 分析
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = MQTopics.NEWS_AI_TASK,
        consumerGroup = "nexora-ai-consumer-group",
        selectorExpression = "*"
)
public class AITaskConsumer implements RocketMQListener<MessageExt> {

    private final AIAnalysisService aiAnalysisService;

    public AITaskConsumer(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.debug("Received AI task ({} bytes): {}", body.length(),
                body.length() > 120 ? body.substring(0, 120) + "..." : body);
        try {
            NewsAITaskEvent event = JsonUtils.fromJson(body, NewsAITaskEvent.class);
            aiAnalysisService.process(event);
        } catch (Exception e) {
            log.error("Failed to parse AI task: {}", e.getMessage());
            log.debug("Raw body: {}", body.length() > 200 ? body.substring(0, 200) : body);
        }
    }
}
