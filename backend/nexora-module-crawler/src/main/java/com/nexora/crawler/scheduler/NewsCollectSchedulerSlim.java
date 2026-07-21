package com.nexora.crawler.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.crawler.cleaner.ContentCleaner;
import com.nexora.crawler.collector.RSSCollector;
import com.nexora.crawler.pipeline.DuplicateDetector;
import com.nexora.crawler.pipeline.QualityScorer;
import com.nexora.crawler.producer.DirectNewsCollectProducer;
import com.nexora.news.cache.NewsCacheManager;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 新闻采集定时调度器（Slim 版）— RocketMQ 不可用时，直接调用 AI 分析。
 * 当 {@code nexora.rocketmq.enabled=false} 时激活。
 */
@Slf4j
@Component
@ConditionalOnExpression("${nexora.crawler.scheduler.enabled:true} and !${nexora.rocketmq.enabled:true}")
public class NewsCollectSchedulerSlim {

    private final NewsSourceMapper sourceMapper;
    private final NewsArticleMapper articleMapper;
    private final ContentCleaner contentCleaner;
    private final DuplicateDetector duplicateDetector;
    private final QualityScorer qualityScorer;
    private final DirectNewsCollectProducer directProducer;
    private final NewsCacheManager newsCacheManager;

    public NewsCollectSchedulerSlim(NewsSourceMapper sourceMapper,
                                    NewsArticleMapper articleMapper,
                                    ContentCleaner contentCleaner,
                                    DuplicateDetector duplicateDetector,
                                    QualityScorer qualityScorer,
                                    DirectNewsCollectProducer directProducer,
                                    NewsCacheManager newsCacheManager) {
        this.sourceMapper = sourceMapper;
        this.articleMapper = articleMapper;
        this.contentCleaner = contentCleaner;
        this.duplicateDetector = duplicateDetector;
        this.qualityScorer = qualityScorer;
        this.directProducer = directProducer;
        this.newsCacheManager = newsCacheManager;
    }

    @Scheduled(fixedDelayString = "${nexora.crawler.scheduler.interval-ms:600000}")
    public void collectRSS() {
        log.info("RSS 采集定时任务启动 (slim mode)");

        List<NewsSourceDO> sources = sourceMapper.selectList(
                new LambdaQueryWrapper<NewsSourceDO>()
                        .eq(NewsSourceDO::getType, "RSS")
                        .eq(NewsSourceDO::getStatus, 1));

        if (sources.isEmpty()) {
            log.debug("没有启用的 RSS 源，跳过采集");
            return;
        }

        int totalCollected = 0;
        int totalNew = 0;

        for (NewsSourceDO source : sources) {
            try {
                RSSCollector collector = new RSSCollector(source.getName(), source.getUrl());
                List<Map<String, String>> items = collector.collect();

                for (Map<String, String> item : items) {
                    totalCollected++;
                    String url = item.get("url");
                    String title = item.get("title");
                    String rawContent = item.get("content");

                    if (isDuplicate(url, title)) continue;

                    String cleanedContent = contentCleaner.clean(rawContent);
                    double score = qualityScorer.score(source.getName(), cleanedContent, url);
                    if (score < 20) {
                        log.debug("低质量文章跳过: title={}, score={}", title, score);
                        continue;
                    }

                    NewsArticleDO article = new NewsArticleDO();
                    article.setTitle(title);
                    article.setContent(cleanedContent);
                    article.setSourceUrl(url);
                    article.setSourceId(source.getId());
                    article.setLanguage(source.getLanguage());
                    article.setPublishTime(parsePublishTime(item.get("publishTime")));
                    article.setStatus(1);
                    article.setHotScore(score);
                    article.setViewCount(0);
                    article.setLikeCount(0);
                    articleMapper.insert(article);
                    totalNew++;

                    newsCacheManager.evictAll();

                    // 直接异步调用 AI 分析（不经过 MQ）
                    directProducer.sendAITask(new NewsAITaskEvent(article.getId()));
                    directProducer.sendCollected(article.getId(), title);

                    log.info("新文章入库 (slim): id={}, title={}, score={}", article.getId(),
                            title.substring(0, Math.min(40, title.length())), score);
                }
            } catch (Exception e) {
                log.error("RSS 源采集失败: source={}, url={}", source.getName(), source.getUrl(), e);
            }
        }

        log.info("RSS 采集完成 (slim): 扫描={}, 新增={}/{}", sources.size(), totalNew, totalCollected);
    }

    private boolean isDuplicate(String url, String title) {
        Long count = articleMapper.selectCount(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getSourceUrl, url));
        return count > 0;
    }

    private LocalDateTime parsePublishTime(String time) {
        if (time == null || time.isBlank()) return LocalDateTime.now();
        try { return LocalDateTime.parse(time); } catch (Exception e) { return LocalDateTime.now(); }
    }
}
