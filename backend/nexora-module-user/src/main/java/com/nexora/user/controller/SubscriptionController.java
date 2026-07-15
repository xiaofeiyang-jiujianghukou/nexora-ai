package com.nexora.user.controller;

import com.nexora.common.response.Result;
import com.nexora.user.dto.SubscribeRequest;
import com.nexora.user.entity.UserSubscriptionDO;
import com.nexora.user.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscribe")
@RequiredArgsConstructor
@Tag(name = "订阅模块", description = "用户订阅管理")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "创建订阅")
    public Result<UserSubscriptionDO> subscribe(@Valid @RequestBody SubscribeRequest request) {
        Long userId = getUserId();
        return Result.success(subscriptionService.subscribe(userId, request.getType(), request.getTarget()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消订阅")
    public Result<Void> unsubscribe(@PathVariable Long id) {
        Long userId = getUserId();
        subscriptionService.unsubscribe(userId, id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "订阅列表")
    public Result<List<UserSubscriptionDO>> list() {
        Long userId = getUserId();
        return Result.success(subscriptionService.listSubscriptions(userId));
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
