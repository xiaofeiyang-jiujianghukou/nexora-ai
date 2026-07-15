package com.nexora.news.controller;

import com.nexora.common.response.PageResult;
import com.nexora.common.response.Result;
import com.nexora.news.service.NewsService;
import com.nexora.news.vo.CategoryVO;
import com.nexora.news.vo.NewsDetailVO;
import com.nexora.news.vo.NewsSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "新闻模块", description = "新闻列表、详情、分类")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/list")
    @Operation(summary = "新闻分页列表")
    public Result<PageResult<NewsSummaryVO>> list(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "language", required = false) String language) {
        return Result.success(newsService.listNews(page, size, categoryId, language));
    }

    @GetMapping("/{id}")
    @Operation(summary = "新闻详情")
    public Result<NewsDetailVO> detail(@PathVariable Long id) {
        return Result.success(newsService.getDetail(id));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "相关新闻")
    public Result<List<NewsSummaryVO>> related(@PathVariable Long id,
                                                @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return Result.success(newsService.getRelated(id, limit));
    }

    @GetMapping("/categories")
    @Operation(summary = "新闻分类列表")
    public Result<List<CategoryVO>> categories() {
        return Result.success(newsService.getCategories());
    }
}
