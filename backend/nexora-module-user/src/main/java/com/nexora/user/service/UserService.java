package com.nexora.user.service;

import com.nexora.user.dto.LoginRequest;
import com.nexora.user.dto.RegisterRequest;
import com.nexora.user.dto.UpdateProfileRequest;
import com.nexora.user.vo.LoginVO;
import com.nexora.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    Long register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 刷新 Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 获取用户信息
     */
    UserVO getProfile(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateProfile(Long userId, UpdateProfileRequest request);
}
