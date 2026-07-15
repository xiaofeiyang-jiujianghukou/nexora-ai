package com.nexora.search.config;

import com.nexora.search.index.NewsDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * ES 索引初始化器 — 启动时自动创建索引和 Mapping
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "nexora.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ESIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    public ESIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(NewsDocument.class);

            if (indexOps.exists()) {
                log.info("ES 索引 news_index 已存在");
            } else {
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping());
                log.info("ES 索引 news_index 创建成功（ik_max_word 分词器）");
            }
        } catch (Exception e) {
            log.warn("ES 索引初始化失败（ES 未运行？IK 分词器未安装？）: {}", e.getMessage());
        }
    }
}
