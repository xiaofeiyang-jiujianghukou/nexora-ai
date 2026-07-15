package com.nexora.search.consumer;

import com.nexora.common.constants.MQTopics;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.search.index.NewsDocument;
import com.nexora.search.repository.NewsSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * ES 索引任务消费者 — 消费 nexora-news-index-task，将文章索引写入 Elasticsearch
 */
@Slf4j
@Component
@ConditionalOnExpression("${nexora.elasticsearch.enabled:false} and ${nexora.rocketmq.enabled:true}")
@RocketMQMessageListener(
        topic = MQTopics.NEWS_INDEX_TASK,
        consumerGroup = "nexora-index-consumer-group",
        selectorExpression = "*"
)
public class IndexTaskConsumer implements RocketMQListener<String> {

    private final NewsArticleMapper articleMapper;
    private final NewsSourceMapper sourceMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsSearchRepository searchRepository;

    public IndexTaskConsumer(NewsArticleMapper articleMapper,
                             NewsSourceMapper sourceMapper,
                             NewsCategoryMapper categoryMapper,
                             NewsSearchRepository searchRepository) {
        this.articleMapper = articleMapper;
        this.sourceMapper = sourceMapper;
        this.categoryMapper = categoryMapper;
        this.searchRepository = searchRepository;
    }

    @Override
    public void onMessage(String message) {
        Long articleId;
        try {
            articleId = Long.parseLong(message.replaceAll("\\D+", ""));
        } catch (Exception e) {
            log.warn("无法解析索引任务消息: {}", message);
            return;
        }

        log.debug("收到 ES 索引任务: articleId={}", articleId);

        try {
            NewsArticleDO article = articleMapper.selectById(articleId);
            if (article == null) {
                log.warn("文章不存在，跳过 ES 索引: articleId={}", articleId);
                return;
            }

            String sourceName = "";
            if (article.getSourceId() != null) {
                NewsSourceDO source = sourceMapper.selectById(article.getSourceId());
                sourceName = source != null ? source.getName() : "";
            }

            String categoryName = "";
            if (article.getCategoryId() != null) {
                NewsCategoryDO category = categoryMapper.selectById(article.getCategoryId());
                categoryName = category != null ? category.getName() : "";
            }

            NewsDocument doc = NewsDocument.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .summary(article.getSummary())
                    .sourceName(sourceName)
                    .categoryName(categoryName)
                    .language(article.getLanguage())
                    .hotScore(article.getHotScore())
                    .publishTime(article.getPublishTime())
                    .build();

            searchRepository.save(doc);
            log.info("ES 索引成功: articleId={}", articleId);

        } catch (Exception e) {
            log.error("ES 索引失败: articleId={}, error={}", articleId, e.getMessage(), e);
        }
    }
}
