package com.nexora.news.manager;

import com.nexora.news.service.NewsService;
import com.nexora.news.vo.CategoryVO;
import com.nexora.news.vo.NewsSummaryVO;
import com.nexora.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 首页 Feed 编排器
 * 组合：热点聚合 + 分类分组 + 用户个性化
 */
@Component
@RequiredArgsConstructor
public class FeedManager {

    private final NewsService newsService;

    /**
     * 首页信息流
     * @return 按分类分组的新闻
     */
    public Map<String, Object> buildHomeFeed(Long userId) {
        Map<String, Object> feed = new LinkedHashMap<>();

        // 1. 全局热点 (Top 10)
        PageResult<NewsSummaryVO> hot = newsService.listNews(1, 10, null, null);
        feed.put("hot", hot.getList());

        // 2. 按分类分组
        List<CategoryVO> categories = newsService.getCategories();
        for (CategoryVO cat : categories) {
            if (cat.getParentId() != null && cat.getParentId() > 0) continue; // 只取顶级分类
            PageResult<NewsSummaryVO> catNews = newsService.listNews(1, 6, cat.getId(), null);
            if (!catNews.getList().isEmpty()) {
                Map<String, Object> section = new LinkedHashMap<>();
                section.put("category", cat.getName());
                section.put("items", catNews.getList());
                feed.put(cat.getCode(), section);
            }
        }

        // 3. 用户个性化 (如果有 userId)
        if (userId != null) {
            feed.put("personalized", Collections.emptyList()); // 后续从订阅生成
        }

        return feed;
    }
}
