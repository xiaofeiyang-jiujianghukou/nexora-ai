package com.nexora.user.controller;

import com.nexora.common.response.Result;
import com.nexora.user.dto.LoginRequest;
import com.nexora.user.dto.RegisterRequest;
import com.nexora.user.service.UserService;
import com.nexora.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证模块", description = "用户注册、登录、Token 刷新")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        return Result.success(Map.of("userId", userId));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO result = userService.login(request);
        return Result.success(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public Result<LoginVO> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        LoginVO result = userService.refreshToken(refreshToken);
        return Result.success(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // Token 加入黑名单由 Gateway/Filter 层处理
        return Result.success();
    }
}
