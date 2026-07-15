package com.nexora.config;

import com.nexora.common.constants.MQTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * RocketMQ 配置 — 开发环境自动创建 Topic
 */
@Slf4j
@Configuration
@Profile("!test")
public class RocketMQConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @PostConstruct
    public void initTopics() {
        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(nameServer);
        admin.setInstanceName("nexora-admin-" + System.currentTimeMillis());

        List<String> topics = List.of(
                MQTopics.NEWS_COLLECTED,
                MQTopics.NEWS_AI_TASK,
                MQTopics.NEWS_INDEX_TASK
        );

        try {
            admin.start();
            for (String topic : topics) {
                try {
                    admin.createTopic(topic, topic, 4, java.util.Map.of());
                    log.info("RocketMQ Topic created: {}", topic);
                } catch (Exception e) {
                    // Topic may already exist — this is expected on restart
                    if (e.getMessage() != null && e.getMessage().contains("exist")) {
                        log.debug("Topic already exists: {}", topic);
                    } else {
                        log.info("Topic {} may already exist: {}", topic, e.getMessage().split("\n")[0]);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("RocketMQ admin failed to start (MQ not running?): {}", e.getMessage());
        } finally {
            admin.shutdown();
        }
    }
}
