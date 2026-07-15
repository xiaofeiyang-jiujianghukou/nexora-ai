package com.nexora.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexora.common.enums.GlobalErrorCode;
import com.nexora.common.exception.BusinessException;
import com.nexora.common.response.PageResult;
import com.nexora.news.entity.NewsArticleDO;
import com.nexora.news.entity.UserFavoriteDO;
import com.nexora.news.mapper.NewsArticleMapper;
import com.nexora.news.mapper.NewsCategoryMapper;
import com.nexora.news.mapper.NewsSourceMapper;
import com.nexora.news.mapper.UserFavoriteMapper;
import com.nexora.news.service.FavoriteService;
import com.nexora.news.vo.NewsSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final NewsArticleMapper newsArticleMapper;
    private final NewsSourceMapper sourceMapper;
    private final NewsCategoryMapper categoryMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long newsId) {
        // 检查新闻存在
        NewsArticleDO article = newsArticleMapper.selectById(newsId);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException(GlobalErrorCode.NEWS_NOT_FOUND);
        }
        // 检查重复
        if (isFavorited(userId, newsId)) {
            throw new BusinessException(GlobalErrorCode.DUPLICATE_FAVORITE);
        }
        UserFavoriteDO fav = new UserFavoriteDO();
        fav.setUserId(userId);
        fav.setNewsId(newsId);
        favoriteMapper.insert(fav);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long newsId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<UserFavoriteDO>()
                        .eq(UserFavoriteDO::getUserId, userId)
                        .eq(UserFavoriteDO::getNewsId, newsId));
    }

    @Override
    public PageResult<NewsSummaryVO> listFavorites(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .orderByDesc(UserFavoriteDO::getCreatedTime);

        Page<UserFavoriteDO> pageResult = favoriteMapper.selectPage(new Page<>(page, size), wrapper);

        List<NewsSummaryVO> list = pageResult.getRecords().stream()
                .map(fav -> {
                    NewsArticleDO article = newsArticleMapper.selectById(fav.getNewsId());
                    if (article == null) return null;
                    String sourceName = null;
                    if (article.getSourceId() != null && sourceMapper.selectById(article.getSourceId()) != null) {
                        sourceName = sourceMapper.selectById(article.getSourceId()).getName();
                    }
                    String catName = null;
                    if (article.getCategoryId() != null && categoryMapper.selectById(article.getCategoryId()) != null) {
                        catName = categoryMapper.selectById(article.getCategoryId()).getName();
                    }
                    return NewsSummaryVO.builder()
                            .id(article.getId())
                            .title(article.getTitle())
                            .summary(article.getSummary())
                            .sourceName(sourceName)
                            .categoryName(catName)
                            .hotScore(article.getHotScore())
                            .viewCount(article.getViewCount())
                            .publishTime(article.getPublishTime())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), page, size);
    }

    @Override
    public boolean isFavorited(Long userId, Long newsId) {
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavoriteDO>()
                        .eq(UserFavoriteDO::getUserId, userId)
                        .eq(UserFavoriteDO::getNewsId, newsId)) > 0;
    }
}
