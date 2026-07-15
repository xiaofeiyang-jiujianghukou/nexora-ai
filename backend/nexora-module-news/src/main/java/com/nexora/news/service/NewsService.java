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
     */
    NewsDetailVO getDetail(Long id);

    /**
     * 相关新闻
     */
    List<NewsSummaryVO> getRelated(Long newsId, int limit);

    /**
     * 获取所有分类
     */
    List<CategoryVO> getCategories();
}
