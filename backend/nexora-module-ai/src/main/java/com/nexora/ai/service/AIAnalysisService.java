package com.nexora.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.ai.manager.NewsAIManager;
import com.nexora.common.constants.MQTopics;
import com.nexora.common.event.NewsAITaskEvent;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.cache.NewsCacheManager;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsArticleI18nDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.mapper.NewsArticleI18nMapper;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 分析服务 — 从 DB 读取文章内容，调用 AI，保存结果到 i18n 表，触发 ES 索引。
 * 每种语言独立一行，支持水平扩展。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "nexora.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class AIAnalysisService {

    private final NewsAIManager newsAIManager;
    private final NewsArticleMapper articleMapper;
    private final NewsArticleI18nMapper i18nMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsCacheManager newsCacheManager;
    private final RocketMQTemplate rocketMQTemplate;

    public AIAnalysisService(NewsAIManager newsAIManager,
                             NewsArticleMapper articleMapper,
                             NewsArticleI18nMapper i18nMapper,
                             NewsCategoryMapper categoryMapper,
                             NewsCacheManager newsCacheManager,
                             RocketMQTemplate rocketMQTemplate) {
        this.newsAIManager = newsAIManager;
        this.articleMapper = articleMapper;
        this.i18nMapper = i18nMapper;
        this.categoryMapper = categoryMapper;
        this.newsCacheManager = newsCacheManager;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /** 目标分析语言列表（与 NewsAIManager.TARGET_LANGUAGES 保持同步） */
    private static final List<String> TARGET_LANG_CODES = List.of("zh", "en");

    /**
     * 处理 AI 分析任务：从 DB 读文章 → AI 分析 → 批量写入 i18n 表 → 触发 ES 索引
     */
    @Transactional
    public void process(NewsAITaskEvent event) {
        log.info("Processing AI analysis for article: {}", event.getArticleId());

        // 1. 从 DB 查询文章
        NewsArticleDO article = articleMapper.selectById(event.getArticleId());
        if (article == null) {
            log.warn("Article not found: {}", event.getArticleId());
            return;
        }

        // 2. 调用 AI 分析（全量 7 语言）
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

        // 4. 批量写入 i18n 表（每种语言一行）
        String mainSummary = "";
        List<NewsArticleI18nDO> i18nRows = new ArrayList<>();
        for (String langCode : TARGET_LANG_CODES) {
            Object sectionObj = result.get(langCode);
            if (sectionObj instanceof Map<?, ?> section) {
                NewsArticleI18nDO row = buildI18nRow(event.getArticleId(), langCode, section);
                i18nRows.add(row);
                // 主摘要取第一个可用
                if (mainSummary.isEmpty()) {
                    Object s = section.get("summary");
                    if (s instanceof String str && !str.isBlank()) {
                        mainSummary = str;
                    }
                }
            }
        }
        // 先删后插（应对重跑场景）
        i18nMapper.delete(new LambdaQueryWrapper<NewsArticleI18nDO>()
                .eq(NewsArticleI18nDO::getArticleId, event.getArticleId()));
        for (NewsArticleI18nDO row : i18nRows) {
            i18nMapper.insert(row);
        }

        // 5. 更新文章主体（分类 + 摘要 + 热度）
        article.setSummary(mainSummary);
        if (category != null) {
            article.setCategoryId(category.getId());
        }
        if (article.getHotScore() == null || article.getHotScore() == 0) {
            article.setHotScore(50.0);
        }
        articleMapper.updateById(article);

        log.info("AI analysis saved for article {}: {} i18n rows, category={}",
                event.getArticleId(), i18nRows.size(), categoryName);

        // 缓存失效：AI 分析完成 → 清除该分类的列表缓存
        newsCacheManager.evictByCategory(article.getCategoryId());

        // 6. 发送 ES 索引任务
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

    /**
     * 为指定文章补充生成缺失语言的 AI 内容（增量语言支持核心方法）。
     * 单语言生成 → 单行 INSERT，不影响其他语言。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateMissingLang(Long articleId, String langCode) {
        generateMissingLang(articleId, langCode, false);
    }

    /**
     * @param force 如果 true，先删已有行再重新生成（用于修复 LLM 翻译质量）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateMissingLang(Long articleId, String langCode, boolean force) {
        log.info("增量生成 [{}]: articleId={}, force={}", langCode, articleId, force);
        NewsArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            log.warn("Article not found: {}", articleId);
            return;
        }

        if (force) {
            i18nMapper.delete(new LambdaQueryWrapper<NewsArticleI18nDO>()
                    .eq(NewsArticleI18nDO::getArticleId, articleId)
                    .eq(NewsArticleI18nDO::getLangCode, langCode));
        } else {
            // 已有该语言则跳过
            Long exists = i18nMapper.selectCount(new LambdaQueryWrapper<NewsArticleI18nDO>()
                    .eq(NewsArticleI18nDO::getArticleId, articleId)
                    .eq(NewsArticleI18nDO::getLangCode, langCode));
            if (exists != null && exists > 0) {
                log.info("Article {} already has {}, skip", articleId, langCode);
                return;
            }
        }

        // 调用 AI 只生成单语言
        Map<String, Object> newSection = newsAIManager.analyzeForLang(
                article.getTitle(), article.getContent(), langCode);

        // 写入 i18n 表（单行 INSERT）
        NewsArticleI18nDO row = buildI18nRow(articleId, langCode, newSection);
        i18nMapper.insert(row);

        log.info("增量生成 [{}] 完成: articleId={}", langCode, articleId);
    }

    /**
     * 查询缺失某语言的新闻数量（用 NOT EXISTS，不依赖 ai_languages 列）
     */
    public long countMissingLang(String langCode) {
        return articleMapper.selectCount(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getStatus, 1)
                        .notExists("SELECT 1 FROM news_article_i18n i WHERE i.article_id = news_article.id AND i.lang_code = '" + langCode + "'")
        );
    }

    // ---- 私有辅助方法 ----

    @SuppressWarnings("unchecked")
    private NewsArticleI18nDO buildI18nRow(Long articleId, String langCode, Map<?, ?> section) {
        NewsArticleI18nDO row = new NewsArticleI18nDO();
        row.setArticleId(articleId);
        row.setLangCode(langCode);
        row.setTitle(section.get("title") instanceof String t ? t : null);
        row.setSummary(section.get("summary") instanceof String s ? s : null);
        row.setBackground(section.get("background") instanceof String b ? b : null);
        row.setImpact(section.get("impact") instanceof String i ? i : null);

        Object factsObj = section.get("facts");
        if (factsObj instanceof List<?> factsList) {
            row.setFacts(JsonUtils.toJson(factsList));
        } else if (factsObj != null) {
            row.setFacts(factsObj.toString());
        }
        return row;
    }
}
