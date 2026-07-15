package com.nexora.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.ai.manager.NewsAIManager;
import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AI 分析服务 — 从 DB 读取文章内容，调用 AI，保存结果，触发 ES 索引
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class AIAnalysisService {

    private final NewsAIManager newsAIManager;
    private final NewsArticleMapper articleMapper;
    private final NewsCategoryMapper categoryMapper;
    private final RocketMQTemplate rocketMQTemplate;

    public AIAnalysisService(NewsAIManager newsAIManager,
                             NewsArticleMapper articleMapper,
                             NewsCategoryMapper categoryMapper,
                             RocketMQTemplate rocketMQTemplate) {
        this.newsAIManager = newsAIManager;
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 处理 AI 分析任务：从 DB 读文章 → AI 分析 → 保存 → 触发 ES 索引
     */
    @Transactional
    public void process(NewsAITaskEvent event) {
        log.info("Processing AI analysis for article: {}", event.getArticleId());

        // 1. 从 DB 查询文章（数据干净，避免 MQ JSON 序列化问题）
        NewsArticleDO article = articleMapper.selectById(event.getArticleId());
        if (article == null) {
            log.warn("Article not found: {}", event.getArticleId());
            return;
        }

        // 2. 调用 AI 分析
        Map<String, Object> result = newsAIManager.analyze(
                article.getTitle(),
                article.getContent()
        );

        // 3. 解析分类名称 → 分类 ID
        String categoryName = (String) result.getOrDefault("category", "科技");
        NewsCategoryDO category = categoryMapper.selectOne(
                new LambdaQueryWrapper<NewsCategoryDO>()
                        .eq(NewsCategoryDO::getName, categoryName)
        );
        if (category == null) {
            category = categoryMapper.selectOne(
                    new LambdaQueryWrapper<NewsCategoryDO>()
                            .eq(NewsCategoryDO::getCode, categoryName.toLowerCase())
            );
        }

        // 4. 更新文章
        // 保存主摘要（中文为默认）和完整多语言 AI 结果 JSON
        Map<String, Object> zh = (Map<String, Object>) result.get("zh");
        article.setSummary(zh != null ? (String) zh.getOrDefault("summary", "") : "");
        article.setAiResult(JsonUtils.toJson(result));
        if (category != null) {
            article.setCategoryId(category.getId());
        }
        if (article.getHotScore() == null || article.getHotScore() == 0) {
            article.setHotScore(50.0);
        }
        articleMapper.updateById(article);

        log.info("AI analysis saved for article {}: summary={}chars, category={}",
                event.getArticleId(),
                result.getOrDefault("summary", "").toString().length(),
                categoryName);

        // 5. 发送 ES 索引任务
        try {
            String idJson = String.valueOf(event.getArticleId());
            byte[] body = idJson.getBytes(StandardCharsets.UTF_8);
            Message msg = new Message(MQTopics.NEWS_INDEX_TASK, body);
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            producer.send(msg);
        } catch (Exception e) {
            log.warn("ES 索引任务发送失败: articleId={}", event.getArticleId(), e);
        }
    }
}
