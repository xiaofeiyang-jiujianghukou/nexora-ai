package com.nexora.news.controller;

import com.nexora.common.response.PageResult;
import com.nexora.common.response.Result;
import com.nexora.news.service.FavoriteService;
import com.nexora.news.vo.NewsSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "收藏模块", description = "新闻收藏管理")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/news/{id}/favorite")
    @Operation(summary = "收藏新闻")
    public Result<Void> addFavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        favoriteService.addFavorite(userId, id);
        return Result.success();
    }

    @DeleteMapping("/news/{id}/favorite")
    @Operation(summary = "取消收藏")
    public Result<Void> removeFavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, id);
        return Result.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "收藏列表")
    public Result<PageResult<NewsSummaryVO>> listFavorites(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        return Result.success(favoriteService.listFavorites(userId, page, size));
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new com.nexora.common.exception.BusinessException(
                    com.nexora.common.enums.GlobalErrorCode.UNAUTHORIZED);
        }
        return (Long) auth.getPrincipal();
    }
}
