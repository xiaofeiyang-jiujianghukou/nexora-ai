package com.nexora.common.constants;

/**
 * Redis Key 常量
 */
public interface RedisKeys {

    // ---- 新闻缓存 ----
    String NEWS_DETAIL = "news:detail:%d";
    String NEWS_HOT = "news:hot";
    String NEWS_VIEW_COUNT = "news:view:%d";

    // ---- 用户 ----
    String USER_SESSION = "user:session:%d";
    String USER_TOKEN_BLACKLIST = "user:token:blacklist:%s";

    // ---- AI ----
    String AI_TASK_LOCK = "ai:task:%d";

    // ---- 默认 TTL (秒) ----
    long TTL_NEWS_DETAIL = 1800;       // 30 分钟
    long TTL_USER_SESSION = 86400;     // 24 小时
    long TTL_TOKEN_BLACKLIST = 604800; // 7 天
    long TTL_AI_TASK_LOCK = 3600;      // 1 小时
}
