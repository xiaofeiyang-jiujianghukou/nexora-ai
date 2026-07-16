package com.nexora.ai.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.ai.service.AIAnalysisService;
import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.LangBackfillEvent;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.mapper.NewsArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 语言增量回填消费者 — 接收批量回填任务，线程池并行执行。
 * 消费 Topic: nexora-news-lang-backfill
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = MQTopics.LANG_BACKFILL,
        consumerGroup = "nexora-lang-backfill-consumer-group",
        selectorExpression = "*"
)
public class LangBackfillConsumer implements RocketMQListener<MessageExt> {

    private final AIAnalysisService aiAnalysisService;
    private final NewsArticleMapper articleMapper;
    private final ThreadPoolExecutor backfillExecutor;

    public LangBackfillConsumer(AIAnalysisService aiAnalysisService,
                                NewsArticleMapper articleMapper,
                                ThreadPoolExecutor backfillExecutor) {
        this.aiAnalysisService = aiAnalysisService;
        this.articleMapper = articleMapper;
        this.backfillExecutor = backfillExecutor;
    }

    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received backfill task: {}", body);

        LangBackfillEvent event;
        try {
            event = JsonUtils.fromJson(body, LangBackfillEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse backfill event: {}", e.getMessage());
            return;
        }

        String langCode = event.getLangCode();
        int batchSize = Math.min(event.getBatchSize(), 200); // cap at 200

        // 查询缺失文章
        List<NewsArticleDO> articles = articleMapper.selectList(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getStatus, 1)
                        .notExists("SELECT 1 FROM news_article_i18n i WHERE i.article_id = news_article.id AND i.lang_code = '" + langCode + "'")
                        .orderByDesc(NewsArticleDO::getId)
                        .last("LIMIT " + batchSize));

        if (articles.isEmpty()) {
            log.info("Backfill [{}]: no missing articles, all done!", langCode);
            return;
        }

        log.info("Backfill [{}]: {} articles to process with {} parallel threads",
                langCode, articles.size(), backfillExecutor.getCorePoolSize());

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        // 线程池并行处理
        List<CompletableFuture<Void>> futures = articles.stream()
                .map(article -> CompletableFuture.runAsync(() -> {
                    try {
                        aiAnalysisService.generateMissingLang(article.getId(), langCode);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Backfill [{}] failed for article {}: {}", langCode, article.getId(), e.getMessage());
                        failed.incrementAndGet();
                    }
                }, backfillExecutor))
                .toList();

        // 等待全部完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long elapsed = System.currentTimeMillis() - start;
        log.info("Backfill [{}] complete: success={}, failed={}, elapsed={}s, throughput={}/s",
                langCode, success.get(), failed.get(), elapsed / 1000,
                elapsed > 0 ? Math.round(success.get() * 1000.0 / elapsed) : success.get());
    }
}
