package com.nexora.search.controller;

import com.nexora.common.response.PageResult;
import com.nexora.common.response.Result;
import com.nexora.news.vo.NewsSummaryVO;
import com.nexora.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "搜索模块", description = "新闻全文搜索")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/news")
    @Operation(summary = "关键词搜索新闻")
    public Result<PageResult<NewsSummaryVO>> search(
            @RequestParam(name = "q") String keyword,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {
        return Result.success(searchService.search(keyword, page, size, categoryId));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "搜索建议")
    public Result<List<String>> suggest(@RequestParam(name = "q") String prefix) {
        return Result.success(searchService.suggest(prefix));
    }
}
