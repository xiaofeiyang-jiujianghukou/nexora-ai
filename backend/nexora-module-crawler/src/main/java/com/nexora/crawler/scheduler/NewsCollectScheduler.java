package com.nexora.crawler.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.crawler.cleaner.ContentCleaner;
import com.nexora.crawler.collector.RSSCollector;
import com.nexora.crawler.pipeline.DuplicateDetector;
import com.nexora.crawler.pipeline.QualityScorer;
import com.nexora.crawler.producer.NewsCollectProducer;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 新闻采集定时调度器 — 定时扫描 RSS 源 → 清洗 → 去重 → 入库 → 发送 AI 分析任务
 * <p>
 * Phase 1 使用 Spring @Scheduled，后续可迁移到 XXL-JOB。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${nexora.crawler.scheduler.enabled:true} and ${nexora.rocketmq.enabled:true}")
public class NewsCollectScheduler {

    private final NewsSourceMapper sourceMapper;
    private final NewsArticleMapper articleMapper;
    private final ContentCleaner contentCleaner;
    private final DuplicateDetector duplicateDetector;
    private final QualityScorer qualityScorer;
    private final NewsCollectProducer newsCollectProducer;

    /**
     * 每 10 分钟执行一次 RSS 采集
     */
    @Scheduled(fixedDelayString = "${nexora.crawler.scheduler.interval-ms:600000}")
    public void collectRSS() {
        log.info("RSS 采集定时任务启动");

        // 1. 查询所有启用的 RSS 源
        List<NewsSourceDO> sources = sourceMapper.selectList(
                new LambdaQueryWrapper<NewsSourceDO>()
                        .eq(NewsSourceDO::getType, "RSS")
                        .eq(NewsSourceDO::getStatus, 1)
        );

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

                    // 2. 去重：检查 URL 是否已存在
                    if (isDuplicate(url, title)) {
                        continue;
                    }

                    // 3. 内容清洗
                    String cleanedContent = contentCleaner.clean(rawContent);

                    // 4. 质量评分（低于 20 分跳过）
                    double score = qualityScorer.score(source.getName(), cleanedContent, url);
                    if (score < 20) {
                        log.debug("低质量文章跳过: title={}, score={}", title, score);
                        continue;
                    }

                    // 5. 入库
                    NewsArticleDO article = new NewsArticleDO();
                    article.setTitle(title);
                    article.setContent(cleanedContent);
                    article.setSourceUrl(url);
                    article.setSourceId(source.getId());
                    article.setLanguage(source.getLanguage());
                    article.setPublishTime(parsePublishTime(item.get("publishTime")));
                    article.setStatus(1); // 1 = 已发布
                    article.setHotScore(score);
                    article.setViewCount(0);
                    article.setLikeCount(0);
                    articleMapper.insert(article);

                    totalNew++;

                    // 6. 发送 AI 分析任务到 MQ
                    try {
                        newsCollectProducer.sendAITask(
                                new NewsAITaskEvent(article.getId()));
                    } catch (Exception e) {
                        log.warn("MQ 发送失败（MQ 未运行？），AI 分析将延迟: articleId={}", article.getId(), e);
                    }

                    log.info("新文章入库: id={}, title={}, score={}", article.getId(),
                            title.substring(0, Math.min(40, title.length())), score);
                }
            } catch (Exception e) {
                log.error("RSS 源采集失败: source={}, url={}", source.getName(), source.getUrl(), e);
            }
        }

        log.info("RSS 采集完成: 扫描={}, 新增={}/{}", sources.size(), totalNew, totalCollected);
    }

    private boolean isDuplicate(String url, String title) {
        // URL 去重
        Long count = articleMapper.selectCount(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getSourceUrl, url)
        );
        if (count > 0) {
            log.debug("URL 重复跳过: {}", url);
            return true;
        }
        // SimHash 去重
        if (title != null) {
            long simHash = duplicateDetector.simHash(title);
            // 这里简化处理：如果 title 的 simHash 和已有文章 title 相似度过高则跳过
            // 完整实现可查询所有已有 title 的 simHash 并比较汉明距离
            // Phase 1 仅做 URL 去重
        }
        return false;
    }

    private LocalDateTime parsePublishTime(String time) {
        if (time == null || time.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(time);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
