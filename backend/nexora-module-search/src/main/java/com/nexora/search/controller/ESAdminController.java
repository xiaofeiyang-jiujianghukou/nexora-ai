package com.nexora.search.controller;

import com.nexora.common.response.Result;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.search.index.NewsDocument;
import com.nexora.search.repository.NewsSearchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ES 管理端点 — 索引维护
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/es")
@ConditionalOnProperty(name = "nexora.elasticsearch.enabled", havingValue = "true")
@Tag(name = "ES 管理", description = "Elasticsearch 索引重建和维护")
public class ESAdminController {

    private final NewsArticleMapper articleMapper;
    private final NewsSourceMapper sourceMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsSearchRepository searchRepository;

    public ESAdminController(NewsArticleMapper articleMapper,
                             NewsSourceMapper sourceMapper,
                             NewsCategoryMapper categoryMapper,
                             NewsSearchRepository searchRepository) {
        this.articleMapper = articleMapper;
        this.sourceMapper = sourceMapper;
        this.categoryMapper = categoryMapper;
        this.searchRepository = searchRepository;
    }

    @PostMapping("/reindex")
    @Operation(summary = "全量重建 ES 索引 — 将 MySQL 中所有已发布文章同步到 ES")
    public Result<Map<String, Object>> reindex() {
        log.info("开始全量重建 ES 索引...");

        // 预先加载 source 和 category 映射
        List<NewsSourceDO> sources = sourceMapper.selectList(null);
        Map<Long, String> sourceMap = sources.stream()
                .collect(java.util.stream.Collectors.toMap(NewsSourceDO::getId, NewsSourceDO::getName));
        List<NewsCategoryDO> categories = categoryMapper.selectList(null);
        Map<Long, NewsCategoryDO> categoryMap = categories.stream()
                .collect(java.util.stream.Collectors.toMap(NewsCategoryDO::getId, c -> c));

        // 分批读取所有已发布文章
        int batchSize = 200;
        int offset = 0;
        int totalIndexed = 0;
        int totalSkipped = 0;

        while (true) {
            List<NewsArticleDO> batch = articleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NewsArticleDO>()
                            .eq(NewsArticleDO::getStatus, 1)
                            .last("LIMIT " + offset + "," + batchSize));

            if (batch.isEmpty()) break;

            List<NewsDocument> docs = new ArrayList<>(batch.size());
            for (NewsArticleDO article : batch) {
                if (article.getTitle() == null || article.getTitle().isBlank()) {
                    totalSkipped++;
                    continue;
                }
                String sourceName = sourceMap.getOrDefault(article.getSourceId(), "");
                String categoryName = "";
                NewsCategoryDO cat = categoryMap.get(article.getCategoryId());
                if (cat != null) categoryName = cat.getName();

                docs.add(NewsDocument.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .content(truncate(article.getContent(), 10000))
                        .summary(truncate(article.getSummary(), 1000))
                        .sourceName(sourceName)
                        .categoryName(categoryName)
                        .language(article.getLanguage())
                        .hotScore(article.getHotScore())
                        .publishTime(article.getPublishTime())
                        .build());
            }

            if (!docs.isEmpty()) {
                searchRepository.saveAll(docs);
                totalIndexed += docs.size();
            }

            offset += batchSize;
            log.info("ES 索引进度: {} 条已索引", totalIndexed);
        }

        log.info("ES 全量索引完成: 已索引 {} 条, 跳过 {} 条", totalIndexed, totalSkipped);

        return Result.success(Map.of(
                "indexed", totalIndexed,
                "skipped", totalSkipped,
                "message", "ES 全量索引完成"
        ));
    }

    @DeleteMapping("/index")
    @Operation(summary = "删除 ES 索引（news_index）")
    public Result<Map<String, String>> deleteIndex() {
        try {
            searchRepository.deleteAll();
            log.info("ES 索引已清空");
            return Result.success(Map.of("message", "索引已清空"));
        } catch (Exception e) {
            log.error("删除 ES 索引失败", e);
            return Result.error(500, "删除索引失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @Operation(summary = "查询 ES 索引文档数")
    public Result<Map<String, Object>> count() {
        long count = searchRepository.count();
        return Result.success(Map.of("count", count));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
