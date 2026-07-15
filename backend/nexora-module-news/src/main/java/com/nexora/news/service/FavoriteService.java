package com.nexora.news.service;

import com.nexora.common.response.PageResult;
import com.nexora.news.vo.NewsSummaryVO;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long newsId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long newsId);

    /**
     * 收藏列表
     */
    PageResult<NewsSummaryVO> listFavorites(Long userId, Integer page, Integer size);

    /**
     * 是否已收藏
     */
    boolean isFavorited(Long userId, Long newsId);
}
