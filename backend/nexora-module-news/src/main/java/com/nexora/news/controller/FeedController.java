package com.nexora.news.controller;

import com.nexora.common.response.Result;
import com.nexora.news.manager.FeedManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "信息流模块", description = "首页 Feed 信息流")
public class FeedController {

    private final FeedManager feedManager;

    @GetMapping("/home")
    @Operation(summary = "首页信息流")
    public Result<Map<String, Object>> home() {
        Long userId = getOptionalUserId();
        return Result.success(feedManager.buildHomeFeed(userId));
    }

    private Long getOptionalUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long id) return id;
        } catch (Exception ignored) {}
        return null;
    }
}
