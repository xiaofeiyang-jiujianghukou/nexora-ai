package com.nexora.crawler.producer;

import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 新闻采集消息生产者 — 发送 articleId 到 MQ（不传内容，避免 JSON 序列化问题）
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class NewsCollectProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public NewsCollectProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送 AI 分析任务 — 只传 articleId
     */
    public void sendAITask(NewsAITaskEvent event) {
        log.info("Sending AI task for article: {}", event.getArticleId());
        try {
            String json = JsonUtils.toJson(event);
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            Message msg = new Message(MQTopics.NEWS_AI_TASK, body);
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            producer.send(msg);
            log.info("AI task sent for article: {}", event.getArticleId());
        } catch (Exception e) {
            log.error("Failed to send AI task for article {}: {}", event.getArticleId(), e.getMessage());
        }
    }

    /**
     * 发送采集完成事件
     */
    public void sendCollected(Long articleId, String title) {
        try {
            String json = "{\"articleId\":" + articleId + "}";
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            Message msg = new Message(MQTopics.NEWS_COLLECTED, body);
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            producer.send(msg);
        } catch (Exception e) {
            log.debug("Failed to send collected event: {}", e.getMessage());
        }
    }
}
