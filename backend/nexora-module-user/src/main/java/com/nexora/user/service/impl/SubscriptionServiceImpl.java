package com.nexora.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexora.common.enums.GlobalErrorCode;
import com.nexora.common.exception.BusinessException;
import com.nexora.user.entity.UserSubscriptionDO;
import com.nexora.user.mapper.UserSubscriptionMapper;
import com.nexora.user.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public UserSubscriptionDO subscribe(Long userId, String type, String target) {
        // 检查重复
        Long count = subscriptionMapper.selectCount(
                new LambdaQueryWrapper<UserSubscriptionDO>()
                        .eq(UserSubscriptionDO::getUserId, userId)
                        .eq(UserSubscriptionDO::getType, type)
                        .eq(UserSubscriptionDO::getTarget, target));
        if (count > 0) {
            throw new BusinessException(GlobalErrorCode.DUPLICATE_FAVORITE);
        }
        UserSubscriptionDO sub = new UserSubscriptionDO();
        sub.setUserId(userId);
        sub.setType(type);
        sub.setTarget(target);
        subscriptionMapper.insert(sub);
        return sub;
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, Long subscriptionId) {
        UserSubscriptionDO sub = subscriptionMapper.selectById(subscriptionId);
        if (sub == null || !sub.getUserId().equals(userId)) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }
        subscriptionMapper.deleteById(subscriptionId);
    }

    @Override
    public List<UserSubscriptionDO> listSubscriptions(Long userId) {
        return subscriptionMapper.selectList(
                new LambdaQueryWrapper<UserSubscriptionDO>()
                        .eq(UserSubscriptionDO::getUserId, userId)
                        .orderByDesc(UserSubscriptionDO::getCreatedTime));
    }
}
