package com.nexora.search.service;

import com.nexora.common.response.PageResult;
import com.nexora.news.vo.NewsSummaryVO;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 关键词搜索新闻
     */
    PageResult<NewsSummaryVO> search(String keyword, Integer page, Integer size, Long categoryId);

    /**
     * 搜索建议
     */
    java.util.List<String> suggest(String prefix);
}
