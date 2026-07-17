package com.nexora.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexora.common.response.PageResult;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsArticleI18nDO;
import com.nexora.news.mapper.NewsArticleI18nMapper;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.news.vo.NewsSummaryVO;
import com.nexora.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现 — MySQL LIKE 搜索（ES 未启用时的回退方案）
 * 当 {@code nexora.elasticsearch.enabled=false} 或未配置时激活
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "nexora.elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class SearchServiceImpl implements SearchService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsArticleI18nMapper i18nMapper;
    private final NewsSourceMapper sourceMapper;
    private final NewsCategoryMapper categoryMapper;

    @Override
    public PageResult<NewsSummaryVO> search(String keyword, Integer page, Integer size, Long categoryId) {
        LambdaQueryWrapper<NewsArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsArticleDO::getStatus, 1)
                .and(w -> w.like(NewsArticleDO::getTitle, keyword)
                        .or().like(NewsArticleDO::getContent, keyword)
                        .or().like(NewsArticleDO::getSummary, keyword));

        if (categoryId != null) {
            wrapper.eq(NewsArticleDO::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(NewsArticleDO::getHotScore);

        Page<NewsArticleDO> pageResult = newsArticleMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量加载 i18n 内容
        List<Long> articleIds = pageResult.getRecords().stream()
                .map(NewsArticleDO::getId).toList();
        Map<Long, Map<String, Object>> aiResultByArticleId = batchLoadI18n(articleIds);

        List<NewsSummaryVO> list = pageResult.getRecords().stream()
                .map(a -> {
                    String src = null;
                    String catName = null;
                    String catCode = null;
                    if (a.getSourceId() != null) {
                        var s = sourceMapper.selectById(a.getSourceId());
                        if (s != null) src = s.getName();
                    }
                    if (a.getCategoryId() != null) {
                        var c = categoryMapper.selectById(a.getCategoryId());
                        if (c != null) { catName = c.getName(); catCode = c.getCode(); }
                    }
                    Map<String, Object> aiResult = aiResultByArticleId.getOrDefault(a.getId(), Collections.emptyMap());
                    return NewsSummaryVO.builder()
                            .id(a.getId()).title(a.getTitle()).summary(a.getSummary())
                            .sourceName(src).categoryName(catName).categoryCode(catCode)
                            .hotScore(a.getHotScore()).viewCount(a.getViewCount())
                            .tags(extractTags(aiResult))
                            .aiResult(aiResult.isEmpty() ? null : aiResult)
                            .publishTime(a.getPublishTime()).build();
                })
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), page, size);
    }

    /** 批量加载文章的多语言 AI 内容 */
    private Map<Long, Map<String, Object>> batchLoadI18n(List<Long> articleIds) {
        if (articleIds.isEmpty()) return Collections.emptyMap();

        List<NewsArticleI18nDO> rows = i18nMapper.selectList(
                new LambdaQueryWrapper<NewsArticleI18nDO>()
                        .in(NewsArticleI18nDO::getArticleId, articleIds));

        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (NewsArticleI18nDO row : rows) {
            Map<String, Object> aiResult = result.computeIfAbsent(row.getArticleId(), k -> new LinkedHashMap<>());
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("title", row.getTitle());
            section.put("summary", row.getSummary());
            section.put("facts", parseFactsJson(row.getFacts()));
            section.put("background", row.getBackground());
            section.put("impact", row.getImpact());
            aiResult.put(row.getLangCode(), section);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseFactsJson(String factsJson) {
        if (factsJson == null || factsJson.isBlank()) return List.of();
        try {
            return JsonUtils.fromJson(factsJson, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** 从 aiResult.entities 中提取实体名称作为标签 */
    @SuppressWarnings("unchecked")
    private List<String> extractTags(Map<String, Object> aiResult) {
        if (aiResult == null) return Collections.emptyList();
        try {
            Object entitiesObj = aiResult.get("entities");
            if (entitiesObj instanceof Map) {
                Map<String, Object> entitiesMap = (Map<String, Object>) entitiesObj;
                Object items = entitiesMap.get("items");
                if (items instanceof List) {
                    return ((List<Map<String, Object>>) items).stream()
                            .filter(e -> e.containsKey("name") && e.get("name") != null)
                            .map(e -> e.get("name").toString())
                            .limit(8)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract tags from aiResult", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> suggest(String prefix) {
        LambdaQueryWrapper<NewsArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(NewsArticleDO::getTitle)
                .eq(NewsArticleDO::getStatus, 1)
                .likeRight(NewsArticleDO::getTitle, prefix)
                .orderByDesc(NewsArticleDO::getHotScore)
                .last("LIMIT 10");

        return newsArticleMapper.selectList(wrapper).stream()
                .map(NewsArticleDO::getTitle)
                .collect(Collectors.toList());
    }
}
