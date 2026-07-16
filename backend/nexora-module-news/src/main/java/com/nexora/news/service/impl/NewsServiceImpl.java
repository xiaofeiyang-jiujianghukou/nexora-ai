package com.nexora.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexora.common.enums.GlobalErrorCode;
import com.nexora.common.exception.BusinessException;
import com.nexora.common.response.PageResult;
import com.nexora.common.utils.JsonUtils;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsArticleI18nDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.entity.UserBrowsingHistoryDO;
import com.nexora.news.entity.UserFavoriteDO;
import com.nexora.news.mapper.NewsArticleI18nMapper;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.news.mapper.UserBrowsingHistoryMapper;
import com.nexora.news.mapper.UserFavoriteMapper;
import com.nexora.news.service.NewsService;
import com.nexora.news.vo.CategoryVO;
import com.nexora.news.vo.NewsDetailVO;
import com.nexora.news.vo.NewsSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新闻服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsArticleI18nMapper i18nMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsSourceMapper sourceMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final UserBrowsingHistoryMapper browsingHistoryMapper;

    /** Redis 可选 — 测试环境或 nexora.redis.enabled=false 时为 null */
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "news:list";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    @SuppressWarnings("unchecked")
    public PageResult<NewsSummaryVO> listNews(Integer page, Integer size, Long categoryId, String language) {
        // 缓存 key: news:list:{page}:{size}:{categoryId}:{language}
        String cacheKey = String.format("%s:%d:%d:%s:%s",
                CACHE_KEY_PREFIX, page, size,
                categoryId != null ? categoryId : "all",
                language != null ? language : "all");

        // 1. 尝试从 Redis 读取（测试环境 Redis 为 null 则跳过）
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached instanceof PageResult) {
                    log.debug("Cache hit: {}", cacheKey);
                    return (PageResult<NewsSummaryVO>) cached;
                }
            } catch (Exception e) {
                log.warn("Redis read failed for key {}, falling back to DB", cacheKey, e);
            }
        }

        // 2. 缓存未命中，查询数据库
        LambdaQueryWrapper<NewsArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsArticleDO::getStatus, 1); // 已发布
        if (categoryId != null) {
            wrapper.eq(NewsArticleDO::getCategoryId, categoryId);
        }
        if (language != null) {
            wrapper.eq(NewsArticleDO::getLanguage, language);
        }
        wrapper.orderByDesc(NewsArticleDO::getPublishTime);

        Page<NewsArticleDO> pageResult = newsArticleMapper.selectPage(new Page<>(page, size), wrapper);

        List<NewsSummaryVO> list = pageResult.getRecords().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        PageResult<NewsSummaryVO> result = PageResult.of(list, pageResult.getTotal(), page, size);

        // 3. 写入缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
                log.debug("Cache set: {} (TTL {}s)", cacheKey, CACHE_TTL.toSeconds());
            } catch (Exception e) {
                log.warn("Redis write failed for key {}", cacheKey, e);
            }
        }

        return result;
    }

    @Override
    public NewsDetailVO getDetail(Long id, Long userId) {
        NewsArticleDO article = newsArticleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException(GlobalErrorCode.NEWS_NOT_FOUND);
        }

        // 增加阅读数
        article.setViewCount(article.getViewCount() + 1);
        newsArticleMapper.updateById(article);

        // 记录用户浏览历史（已登录用户）
        if (userId != null) {
            recordBrowsingHistory(userId, id, article.getCategoryId());
        }

        return toDetail(article);
    }

    @Override
    public List<NewsSummaryVO> getRelated(Long newsId, int limit) {
        NewsArticleDO article = newsArticleMapper.selectById(newsId);
        if (article == null) {
            return Collections.emptyList();
        }

        // 同分类相关新闻
        LambdaQueryWrapper<NewsArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsArticleDO::getStatus, 1)
                .eq(NewsArticleDO::getCategoryId, article.getCategoryId())
                .ne(NewsArticleDO::getId, newsId)
                .orderByDesc(NewsArticleDO::getHotScore)
                .last("LIMIT " + limit);

        return newsArticleMapper.selectList(wrapper).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<NewsCategoryDO>().eq(NewsCategoryDO::getStatus, 1)
                        .orderByAsc(NewsCategoryDO::getSort))
                .stream()
                .map(c -> CategoryVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .code(c.getCode())
                        .parentId(c.getParentId())
                        .sort(c.getSort())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsSummaryVO> getRecommendations(Long userId, int limit) {
        // 1. 冷启动：无用户或收藏不足 → 回退热门
        if (userId == null) {
            return getHotList(limit);
        }

        List<UserFavoriteDO> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavoriteDO>()
                        .eq(UserFavoriteDO::getUserId, userId)
                        .orderByDesc(UserFavoriteDO::getCreatedTime)
                        .last("LIMIT 50"));

        if (favorites.size() < 3) {
            log.debug("Cold start: userId={}, favorites={} < 3, falling back to hot list", userId, favorites.size());
            return getHotList(limit);
        }

        // 2. 计算兴趣向量：收藏权重 1.0x + 浏览权重 0.5x
        Map<Long, Double> interestVector = buildInterestVector(favorites, userId);

        // 3. 候选池：排除已收藏，取最近发布的 N 篇
        Set<Long> favoritedIds = favorites.stream()
                .map(UserFavoriteDO::getNewsId)
                .collect(Collectors.toSet());

        List<NewsArticleDO> candidates = newsArticleMapper.selectList(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getStatus, 1)
                        .notIn(!favoritedIds.isEmpty(), NewsArticleDO::getId, favoritedIds)
                        .orderByDesc(NewsArticleDO::getPublishTime)
                        .last("LIMIT 200"));

        if (candidates.isEmpty()) {
            return getHotList(limit);
        }

        // 4. 加权打分
        LocalDateTime now = LocalDateTime.now();
        List<ScoredArticle> scored = new ArrayList<>();
        for (NewsArticleDO a : candidates) {
            double score = computeScore(a, interestVector, now);
            scored.add(new ScoredArticle(a, score));
        }

        // 5. 按得分降序，取 top N
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.stream()
                .limit(limit)
                .map(s -> toSummary(s.article))
                .collect(Collectors.toList());
    }

    // ---- 推荐算法辅助方法 ----

    /** 计算用户兴趣向量：收藏权重 1.0x + 浏览权重 0.5x，归一化 */
    private Map<Long, Double> buildInterestVector(List<UserFavoriteDO> favorites, Long userId) {
        Map<Long, Double> raw = new HashMap<>();

        // 收藏权重 1.0x（显式正向信号）
        for (UserFavoriteDO fav : favorites) {
            NewsArticleDO article = newsArticleMapper.selectById(fav.getNewsId());
            if (article != null && article.getCategoryId() != null) {
                raw.merge(article.getCategoryId(), 1.0, Double::sum);
            }
        }

        // 浏览权重 0.5x（隐式兴趣信号，取最近 100 条）
        List<UserBrowsingHistoryDO> history = browsingHistoryMapper.selectList(
                new LambdaQueryWrapper<UserBrowsingHistoryDO>()
                        .eq(UserBrowsingHistoryDO::getUserId, userId)
                        .orderByDesc(UserBrowsingHistoryDO::getUpdatedTime)
                        .last("LIMIT 100"));
        for (UserBrowsingHistoryDO h : history) {
            if (h.getCategoryId() != null) {
                raw.merge(h.getCategoryId(), 0.5, Double::sum);
            }
        }

        // 归一化
        double total = raw.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<Long, Double> vector = new HashMap<>();
        if (total > 0) {
            for (var entry : raw.entrySet()) {
                vector.put(entry.getKey(), entry.getValue() / total);
            }
        }
        return vector;
    }

    /** 记录用户浏览历史（upsert：已存在则更新时间） */
    private void recordBrowsingHistory(Long userId, Long articleId, Long categoryId) {
        try {
            UserBrowsingHistoryDO existing = browsingHistoryMapper.selectOne(
                    new LambdaQueryWrapper<UserBrowsingHistoryDO>()
                            .eq(UserBrowsingHistoryDO::getUserId, userId)
                            .eq(UserBrowsingHistoryDO::getArticleId, articleId));
            if (existing != null) {
                existing.setCategoryId(categoryId);
                existing.setUpdatedTime(LocalDateTime.now());
                browsingHistoryMapper.updateById(existing);
            } else {
                UserBrowsingHistoryDO history = new UserBrowsingHistoryDO();
                history.setUserId(userId);
                history.setArticleId(articleId);
                history.setCategoryId(categoryId);
                browsingHistoryMapper.insert(history);
            }
        } catch (Exception e) {
            // 浏览历史记录失败不影响主流程
            log.warn("Failed to record browsing history: userId={}, articleId={}", userId, articleId, e);
        }
    }

    /** 计算单篇文章推荐得分 */
    private double computeScore(NewsArticleDO article, Map<Long, Double> interestVector, LocalDateTime now) {
        // 归一化热度分（假设 hotScore 0~100+，用 tanh 压缩到 0~1）
        double hotScore = article.getHotScore() != null ? article.getHotScore() : 0;
        double normalizedHot = Math.tanh(hotScore / 50.0);

        // 时间衰减：指数衰减，半衰期 7 天
        long daysOld = article.getPublishTime() != null
                ? Math.max(0, ChronoUnit.DAYS.between(article.getPublishTime(), now))
                : 30;
        double timeDecay = Math.exp(-daysOld / 7.0);

        // 兴趣权重：匹配用户兴趣分类，加 0~50% 加成
        double interestWeight = 1.0;
        if (article.getCategoryId() != null && interestVector.containsKey(article.getCategoryId())) {
            interestWeight = 1.0 + 0.5 * interestVector.get(article.getCategoryId());
        }

        return normalizedHot * timeDecay * interestWeight;
    }

    /** 热门兜底：按 hotScore 降序 */
    private List<NewsSummaryVO> getHotList(int limit) {
        List<NewsArticleDO> articles = newsArticleMapper.selectList(
                new LambdaQueryWrapper<NewsArticleDO>()
                        .eq(NewsArticleDO::getStatus, 1)
                        .orderByDesc(NewsArticleDO::getHotScore)
                        .last("LIMIT " + limit));
        return articles.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /** 内部打分容器 */
    private record ScoredArticle(NewsArticleDO article, double score) {}

    // ---- 私有转换方法 ----

    private NewsSummaryVO toSummary(NewsArticleDO a) {
        String sourceName = null;
        if (a.getSourceId() != null) {
            NewsSourceDO source = sourceMapper.selectById(a.getSourceId());
            if (source != null) sourceName = source.getName();
        }
        String categoryName = null;
        String categoryCode = null;
        if (a.getCategoryId() != null) {
            NewsCategoryDO cat = categoryMapper.selectById(a.getCategoryId());
            if (cat != null) {
                categoryName = cat.getName();
                categoryCode = cat.getCode();
            }
        }

        Map<String, Object> aiResult = buildAiResultFromI18n(a.getId());

        return NewsSummaryVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .sourceName(sourceName)
                .language(a.getLanguage())
                .categoryName(categoryName)
                .categoryCode(categoryCode)
                .hotScore(a.getHotScore())
                .viewCount(a.getViewCount())
                .tags(extractTags(aiResult))
                .aiResult(aiResult)
                .publishTime(a.getPublishTime())
                .build();
    }

    private NewsDetailVO toDetail(NewsArticleDO a) {
        String sourceName = null;
        if (a.getSourceId() != null) {
            NewsSourceDO source = sourceMapper.selectById(a.getSourceId());
            if (source != null) sourceName = source.getName();
        }
        String categoryName = null;
        String categoryCode = null;
        if (a.getCategoryId() != null) {
            NewsCategoryDO cat = categoryMapper.selectById(a.getCategoryId());
            if (cat != null) {
                categoryName = cat.getName();
                categoryCode = cat.getCode();
            }
        }

        Map<String, Object> aiResult = buildAiResultFromI18n(a.getId());

        return NewsDetailVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .summary(a.getSummary())
                .sourceName(sourceName)
                .sourceUrl(a.getSourceUrl())
                .language(a.getLanguage())
                .categoryName(categoryName)
                .categoryCode(categoryCode)
                .hotScore(a.getHotScore())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .status(a.getStatus())
                .tags(extractTags(aiResult))
                .aiResult(aiResult)
                .publishTime(a.getPublishTime())
                .createdTime(a.getCreatedTime())
                .build();
    }

    /**
     * 从 news_article_i18n 表查询所有语言 → 构建兼容前端的 aiResult map
     * 结构: {"zh": {title,summary,facts,background,impact}, "en": {...}, ...}
     */
    private Map<String, Object> buildAiResultFromI18n(Long articleId) {
        Map<String, Object> aiResult = new LinkedHashMap<>();

        List<NewsArticleI18nDO> i18nRows = i18nMapper.selectList(
                new LambdaQueryWrapper<NewsArticleI18nDO>()
                        .eq(NewsArticleI18nDO::getArticleId, articleId));

        for (NewsArticleI18nDO row : i18nRows) {
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("title", row.getTitle());
            section.put("summary", row.getSummary());
            section.put("facts", parseFactsJson(row.getFacts()));
            section.put("background", row.getBackground());
            section.put("impact", row.getImpact());
            aiResult.put(row.getLangCode(), section);
        }
        return aiResult;
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
}
