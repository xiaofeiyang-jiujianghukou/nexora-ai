package com.nexora.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexora.common.enums.GlobalErrorCode;
import com.nexora.common.exception.BusinessException;
import com.nexora.common.response.PageResult;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.NewsCategoryDO;
import com.nexora.news.entity.NewsSourceDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.news.service.NewsService;
import com.nexora.news.vo.CategoryVO;
import com.nexora.news.vo.NewsDetailVO;
import com.nexora.news.vo.NewsSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsSourceMapper sourceMapper;

    @Override
    public PageResult<NewsSummaryVO> listNews(Integer page, Integer size, Long categoryId, String language) {
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

        return PageResult.of(list, pageResult.getTotal(), page, size);
    }

    @Override
    public NewsDetailVO getDetail(Long id) {
        NewsArticleDO article = newsArticleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException(GlobalErrorCode.NEWS_NOT_FOUND);
        }

        // 增加阅读数
        article.setViewCount(article.getViewCount() + 1);
        newsArticleMapper.updateById(article);

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

    // ---- 私有转换方法 ----

    private NewsSummaryVO toSummary(NewsArticleDO a) {
        String sourceName = null;
        if (a.getSourceId() != null) {
            NewsSourceDO source = sourceMapper.selectById(a.getSourceId());
            if (source != null) sourceName = source.getName();
        }
        String categoryName = null;
        if (a.getCategoryId() != null) {
            NewsCategoryDO cat = categoryMapper.selectById(a.getCategoryId());
            if (cat != null) categoryName = cat.getName();
        }

        return NewsSummaryVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .sourceName(sourceName)
                .language(a.getLanguage())
                .categoryName(categoryName)
                .hotScore(a.getHotScore())
                .viewCount(a.getViewCount())
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
        if (a.getCategoryId() != null) {
            NewsCategoryDO cat = categoryMapper.selectById(a.getCategoryId());
            if (cat != null) categoryName = cat.getName();
        }

        return NewsDetailVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .summary(a.getSummary())
                .sourceName(sourceName)
                .sourceUrl(a.getSourceUrl())
                .language(a.getLanguage())
                .categoryName(categoryName)
                .hotScore(a.getHotScore())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .status(a.getStatus())
                .publishTime(a.getPublishTime())
                .createdTime(a.getCreatedTime())
                .build();
    }
}
