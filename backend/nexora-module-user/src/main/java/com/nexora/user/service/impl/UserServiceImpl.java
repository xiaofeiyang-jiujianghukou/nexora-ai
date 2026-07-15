package com.nexora.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.common.enums.GlobalErrorCode;
import com.nexora.common.exception.BusinessException;
import com.nexora.common.utils.JwtUtils;
import com.nexora.user.dto.LoginRequest;
import com.nexora.user.dto.RegisterRequest;
import com.nexora.user.dto.UpdateProfileRequest;
import com.nexora.user.entity.UserDO;
import com.nexora.user.mapper.UserMapper;
import com.nexora.user.service.UserService;
import com.nexora.user.vo.LoginVO;
import com.nexora.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        // 检查邮箱是否已注册
        UserDO existUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getEmail, request.getEmail()));
        if (existUser != null) {
            throw new BusinessException(GlobalErrorCode.DUPLICATE_EMAIL);
        }

        // 创建用户
        UserDO user = new UserDO();
        user.setUsername(request.getEmail()); // 用邮箱作为用户名
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setLanguage("zh-CN");
        user.setStatus(1);
        userMapper.insert(user);

        log.info("用户注册成功: userId={}, email={}", user.getId(), user.getEmail());
        return user.getId();
    }

    @Override
    public LoginVO login(LoginRequest request) {
        // 查找用户
        UserDO user = userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getEmail, request.getEmail()));
        if (user == null) {
            throw new BusinessException(GlobalErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(GlobalErrorCode.INVALID_CREDENTIALS);
        }

        // 生成 Token
        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // 缓存 Session (Redis 可用时)
        if (redisTemplate != null) {
            String sessionKey = String.format("user:session:%d", user.getId());
            redisTemplate.opsForValue().set(sessionKey, user.getId(), Duration.ofHours(24));
        }

        log.info("用户登录成功: userId={}", user.getId());
        return LoginVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(toVO(user))
                .build();
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!JwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(GlobalErrorCode.TOKEN_EXPIRED);
        }

        Long userId = JwtUtils.getUserId(refreshToken);
        String username = JwtUtils.getUsername(refreshToken);

        // 将旧 refresh token 加入黑名单 (Redis 可用时)
        if (redisTemplate != null) {
            String blacklistKey = String.format("user:token:blacklist:%s", refreshToken.substring(0, 32));
            redisTemplate.opsForValue().set(blacklistKey, "1", 7, TimeUnit.DAYS);
        }

        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }

        String newAccessToken = JwtUtils.generateAccessToken(userId, username);
        String newRefreshToken = JwtUtils.generateRefreshToken(userId, username);

        return LoginVO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(toVO(user))
                .build();
    }

    @Override
    public UserVO getProfile(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }
        userMapper.updateById(user);

        return toVO(user);
    }

    // ---- 私有方法 ----

    private UserVO toVO(UserDO user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .language(user.getLanguage())
                .status(user.getStatus())
                .createdTime(user.getCreatedTime())
                .build();
    }
}
