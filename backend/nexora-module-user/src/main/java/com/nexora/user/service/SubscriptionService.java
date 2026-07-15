package com.nexora.user.service;

import com.nexora.user.entity.UserSubscriptionDO;

import java.util.List;

public interface SubscriptionService {

    UserSubscriptionDO subscribe(Long userId, String type, String target);

    void unsubscribe(Long userId, Long subscriptionId);

    List<UserSubscriptionDO> listSubscriptions(Long userId);
}
