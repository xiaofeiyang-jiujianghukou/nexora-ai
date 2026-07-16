package com.nexora.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.ai.service.AIAnalysisService;
import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.LangBackfillEvent;
import com.nexora.common.response.Result;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.mapper.NewsArticleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AI 管理端点 — 语言增量回填
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai")
@RequiredArgsConstructor
@Tag(name = "AI 管理", description = "AI 分析管理端点")
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class AdminAIController {

    private final AIAnalysisService aiAnalysisService;
    private final NewsArticleMapper newsArticleMapper;
    private final RocketMQTemplate rocketMQTemplate;

    @GetMapping("/missing-stats")
    @Operation(summary = "查询某语言的缺失统计")
    public Result<Map<String, Object>> missingStats(@RequestParam String langCode) {
        long missing = aiAnalysisService.countMissingLang(langCode);
        long total = newsArticleMapper.selectCount(null);
        return Result.success(Map.of(
                "langCode", langCode,
                "missing", missing,
                "total", total,
                "coverage", total > 0 ? Math.round((1.0 - (double) missing / total) * 10000) / 100.0 : 100.0
        ));
    }

    @PostMapping("/backfill/{langCode}")
    @Operation(summary = "启动增量语言回填（同步处理，建议小批量）")
    public Result<Map<String, Object>> backfill(
            @PathVariable String langCode,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("开始回填 [{}], limit={}", langCode, limit);
        long start = System.currentTimeMillis();
        int success = 0;
        int skipped = 0;

        // 查询缺失该语言的文章（NOT EXISTS 子查询）
        var wrapper = new LambdaQueryWrapper<NewsArticleDO>()
                .eq(NewsArticleDO::getStatus, 1)
                .notExists("SELECT 1 FROM news_article_i18n i WHERE i.article_id = news_article.id AND i.lang_code = '" + langCode + "'")
                .orderByDesc(NewsArticleDO::getId)
                .last("LIMIT " + limit);

        var articles = newsArticleMapper.selectList(wrapper);
        log.info("回填 [{}]: 找到 {} 篇待补全文章", langCode, articles.size());

        for (var article : articles) {
            try {
                aiAnalysisService.generateMissingLang(article.getId(), langCode);
                success++;
            } catch (Exception e) {
                log.error("回填失败 [{}] articleId={}: {}", langCode, article.getId(), e.getMessage());
                skipped++;
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("回填 [{}] 完成: success={}, skipped={}, elapsed={}ms", langCode, success, skipped, elapsed);

        return Result.success(Map.of(
                "langCode", langCode,
                "success", success,
                "skipped", skipped,
                "elapsedMs", elapsed
        ));
    }

    @PostMapping("/backfill/{langCode}/batch")
    @Operation(summary = "完整批量回填某语言（处理所有缺失文章，长耗时）")
    public Result<Map<String, Object>> backfillAll(
            @PathVariable String langCode,
            @RequestParam(defaultValue = "100") int batchSize) {

        log.info("开始完整回填 [{}], batchSize={}", langCode, batchSize);
        long start = System.currentTimeMillis();
        int totalSuccess = 0;
        int totalSkipped = 0;
        int batches = 0;

        while (true) {
            var wrapper = new LambdaQueryWrapper<NewsArticleDO>()
                    .eq(NewsArticleDO::getStatus, 1)
                    .notExists("SELECT 1 FROM news_article_i18n i WHERE i.article_id = news_article.id AND i.lang_code = '" + langCode + "'")
                    .orderByDesc(NewsArticleDO::getId)
                    .last("LIMIT " + batchSize);

            var articles = newsArticleMapper.selectList(wrapper);
            if (articles.isEmpty()) break;
            batches++;

            for (var article : articles) {
                try {
                    aiAnalysisService.generateMissingLang(article.getId(), langCode);
                    totalSuccess++;
                } catch (Exception e) {
                    log.error("回填失败 [{}] articleId={}: {}", langCode, article.getId(), e.getMessage());
                    totalSkipped++;
                }
            }

            log.info("回填 [{}] 第{}批完成: success={}", langCode, batches, totalSuccess);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("完整回填 [{}] 完成: totalSuccess={}, totalSkipped={}, batches={}, elapsed={}s",
                langCode, totalSuccess, totalSkipped, batches, elapsed / 1000);

        return Result.success(Map.of(
                "langCode", langCode,
                "totalSuccess", totalSuccess,
                "totalSkipped", totalSkipped,
                "batches", batches,
                "elapsedSeconds", elapsed / 1000
        ));
    }

    @PostMapping("/backfill/{langCode}/force")
    @Operation(summary = "强制重生成某语言（删已有行后重新调用LLM，调试用）")
    public Result<Map<String, Object>> backfillForce(
            @PathVariable String langCode,
            @RequestParam(defaultValue = "3") int limit) {
        log.info("Force backfill [{}]: limit={}", langCode, limit);
        long start = System.currentTimeMillis();
        int success = 0;
        var wrapper = new LambdaQueryWrapper<NewsArticleDO>()
                .eq(NewsArticleDO::getStatus, 1)
                .orderByDesc(NewsArticleDO::getId)
                .last("LIMIT " + limit);
        var articles = newsArticleMapper.selectList(wrapper);
        for (var a : articles) {
            aiAnalysisService.generateMissingLang(a.getId(), langCode, true);
            success++;
        }
        long elapsed = System.currentTimeMillis() - start;
        return Result.success(Map.of("langCode", langCode, "success", success, "elapsedMs", elapsed));
    }

    @PostMapping("/backfill/{langCode}/article/{articleId}")
    @Operation(summary = "为指定文章强制重生成某语言")
    public Result<Map<String, Object>> backfillArticle(
            @PathVariable String langCode,
            @PathVariable Long articleId) {
        aiAnalysisService.generateMissingLang(articleId, langCode, true);
        return Result.success(Map.of("langCode", langCode, "articleId", articleId, "force", true));
    }

    @PostMapping("/backfill/{langCode}/async")
    @Operation(summary = "MQ 异步回填（fire-and-forget，线程池并行处理）")
    public Result<Map<String, Object>> backfillAsync(
            @PathVariable String langCode,
            @RequestParam(defaultValue = "200") int batchSize) {

        long missing = aiAnalysisService.countMissingLang(langCode);
        if (missing == 0) {
            return Result.success(Map.of("accepted", false, "reason", "all articles already have " + langCode));
        }

        int actualBatch = Math.min(batchSize, (int) missing);

        // 发送 MQ 消息，立即返回
        LangBackfillEvent event = new LangBackfillEvent(langCode, actualBatch);
        String json = JsonUtils.toJson(event);
        try {
            Message msg = new Message(MQTopics.LANG_BACKFILL, json.getBytes(StandardCharsets.UTF_8));
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            producer.send(msg);
            log.info("Backfill async [{}] sent to MQ: batchSize={}, missing={}", langCode, actualBatch, missing);
        } catch (Exception e) {
            log.error("Failed to send backfill MQ message [{}]", langCode, e);
            return Result.success(Map.of("accepted", false, "reason", "MQ send failed: " + e.getMessage()));
        }

        return Result.success(Map.of(
                "accepted", true,
                "langCode", langCode,
                "batchSize", actualBatch,
                "estimatedMissing", missing,
                "mode", "async (MQ + thread-pool, 5 parallel threads)"
        ));
    }
}
