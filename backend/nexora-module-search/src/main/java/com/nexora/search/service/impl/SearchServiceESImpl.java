package com.nexora.search.service.impl;

import com.nexora.common.response.PageResult;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleI18nDO;
import com.nexora.news.mapper.NewsArticleI18nMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.vo.NewsSummaryVO;
import com.nexora.search.index.NewsDocument;
import com.nexora.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch 搜索服务实现 — 使用 IK 分词器进行中文全文搜索
 * 当 {@code nexora.elasticsearch.enabled=true} 时激活
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "nexora.elasticsearch.enabled", havingValue = "true")
public class SearchServiceESImpl implements SearchService {

    private final ElasticsearchOperations esOps;
    private final NewsArticleI18nMapper i18nMapper;
    private final NewsCategoryMapper categoryMapper;

    public SearchServiceESImpl(ElasticsearchOperations esOps,
                               NewsArticleI18nMapper i18nMapper,
                               NewsCategoryMapper categoryMapper) {
        this.esOps = esOps;
        this.i18nMapper = i18nMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<NewsSummaryVO> search(String keyword, Integer page, Integer size, Long categoryId) {
        try {
            return doSearch(keyword, page, size, categoryId);
        } catch (Exception e) {
            log.error("ES 搜索失败，keyword={}, categoryId={}, error={}", keyword, categoryId, e.getMessage(), e);
            return PageResult.of(List.of(), 0L, page, size);
        }
    }

    private PageResult<NewsSummaryVO> doSearch(String keyword, Integer page, Integer size, Long categoryId) {
        NativeQuery query;
        if (categoryId != null) {
            // 有分类过滤
            var category = categoryMapper.selectById(categoryId);
            String catName = category != null ? category.getName() : "";
            query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .must(m -> m.multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "summary^2", "content^1")
                            ))
                            .filter(f -> f.term(t -> t
                                    .field("categoryName")
                                    .value(catName)
                            ))
                    ))
                    .withPageable(PageRequest.of(page - 1, size))
                    .build();
        } else {
            query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .must(m -> m.multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "summary^2", "content^1")
                            ))
                    ))
                    .withPageable(PageRequest.of(page - 1, size))
                    .build();
        }

        SearchHits<NewsDocument> hits = esOps.search(query, NewsDocument.class);

        // 批量加载 i18n 内容
        List<Long> articleIds = hits.getSearchHits().stream()
                .map(h -> h.getContent().getId())
                .toList();
        Map<Long, Map<String, Object>> i18nMap = batchLoadI18n(articleIds);

        List<NewsSummaryVO> list = hits.getSearchHits().stream()
                .map(hit -> docToVO(hit.getContent(), i18nMap))
                .collect(Collectors.toList());

        return PageResult.of(list, hits.getTotalHits(), page, size);
    }

    @Override
    public List<String> suggest(String prefix) {
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.matchPhrasePrefix(mpp -> mpp
                            .field("title")
                            .query(prefix)
                    ))
                    .withPageable(PageRequest.of(0, 10))
                    .build();

            SearchHits<NewsDocument> hits = esOps.search(query, NewsDocument.class);
            return hits.getSearchHits().stream()
                    .map(h -> h.getContent().getTitle())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("ES 搜索建议失败，prefix={}, error={}", prefix, e.getMessage(), e);
            return List.of();
        }
    }

    /** 将 ES 文档转为 VO */
    private NewsSummaryVO docToVO(NewsDocument doc, Map<Long, Map<String, Object>> i18nMap) {
        Map<String, Object> aiResult = i18nMap.getOrDefault(doc.getId(), Collections.emptyMap());
        return NewsSummaryVO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .summary(doc.getSummary())
                .sourceName(doc.getSourceName())
                .language(doc.getLanguage())
                .categoryName(doc.getCategoryName())
                .hotScore(doc.getHotScore())
                .viewCount(0)
                .tags(extractTags(aiResult))
                .aiResult(aiResult.isEmpty() ? null : aiResult)
                .publishTime(doc.getPublishTime())
                .build();
    }

    /** 批量加载文章的多语言 AI 内容 */
    private Map<Long, Map<String, Object>> batchLoadI18n(List<Long> articleIds) {
        if (articleIds.isEmpty()) return Collections.emptyMap();

        List<NewsArticleI18nDO> rows = i18nMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NewsArticleI18nDO>()
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
        } catch (Exception ex) {
            log.debug("Failed to extract tags from aiResult", ex);
        }
        return Collections.emptyList();
    }
}
