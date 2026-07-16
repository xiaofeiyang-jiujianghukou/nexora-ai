package com.nexora.news.service;

import com.nexora.common.response.PageResult;
import com.nexora.news.vo.CategoryVO;
import com.nexora.news.vo.NewsDetailVO;
import com.nexora.news.vo.NewsSummaryVO;

import java.util.List;

/**
 * 新闻服务接口
 */
public interface NewsService {

    /**
     * 新闻分页列表
     */
    PageResult<NewsSummaryVO> listNews(Integer page, Integer size, Long categoryId, String language);

    /**
     * 新闻详情
     *
     * @param id     新闻ID
     * @param userId 当前用户ID（null 表示未登录，不记录浏览历史）
     */
    NewsDetailVO getDetail(Long id, Long userId);

    /**
     * 相关新闻
     */
    List<NewsSummaryVO> getRelated(Long newsId, int limit);

    /**
     * 获取所有分类
     */
    List<CategoryVO> getCategories();

    /**
     * 个性化推荐 — 基于用户收藏历史的规则推荐
     * 冷启动（收藏数 < 3）时回退热门排序
     *
     * @param userId 用户 ID（null 时返回热门）
     * @param limit  返回条数
     */
    List<NewsSummaryVO> getRecommendations(Long userId, int limit);
}
