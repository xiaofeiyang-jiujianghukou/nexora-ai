package com.nexora.user.controller;

import com.nexora.common.response.Result;
import com.nexora.user.dto.UpdateProfileRequest;
import com.nexora.user.service.UserService;
import com.nexora.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "个人信息、偏好管理")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public Result<UserVO> getProfile() {
        Long userId = getCurrentUserId();
        UserVO profile = userService.getProfile(userId);
        return Result.success(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        UserVO profile = userService.updateProfile(userId, request);
        return Result.success(profile);
    }

    /**
     * 从 SecurityContext 获取当前用户 ID
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("无法获取当前用户");
    }
}
