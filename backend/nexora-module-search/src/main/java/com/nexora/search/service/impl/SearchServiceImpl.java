package com.nexora.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexora.common.response.PageResult;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.news.vo.NewsSummaryVO;
import com.nexora.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现 — MVP 阶段基于 MySQL LIKE 搜索
 * 后续集成 Elasticsearch 替换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final NewsArticleMapper newsArticleMapper;
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

        List<NewsSummaryVO> list = pageResult.getRecords().stream()
                .map(a -> {
                    String src = sourceMapper.selectById(a.getSourceId()) != null
                            ? sourceMapper.selectById(a.getSourceId()).getName() : null;
                    String cat = categoryMapper.selectById(a.getCategoryId()) != null
                            ? categoryMapper.selectById(a.getCategoryId()).getName() : null;
                    return NewsSummaryVO.builder()
                            .id(a.getId()).title(a.getTitle()).summary(a.getSummary())
                            .sourceName(src).categoryName(cat)
                            .hotScore(a.getHotScore()).viewCount(a.getViewCount())
                            .publishTime(a.getPublishTime()).build();
                })
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), page, size);
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
