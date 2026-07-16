package com.nexora.news.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 新闻缓存管理器 — 统一管理 Redis 缓存的写入与失效
 * <p>
 * 缓存 key 格式：news:list:{page}:{size}:{categoryId}:{language}
 */
@Slf4j
@Component
public class NewsCacheManager {

    private static final String CACHE_KEY_PREFIX = "news:list:";

    /** Redis 可选 — 测试环境或 nexora.redis.enabled=false 时为 null */
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 清除所有新闻列表缓存（新文章发布时调用）
     */
    public void evictAll() {
        if (redisTemplate == null) {
            log.debug("Redis not available, skipping cache eviction");
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("Cache evicted: {} keys (pattern: {}*)", deleted, CACHE_KEY_PREFIX);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache keys: {}*", CACHE_KEY_PREFIX, e);
        }
    }

    /**
     * 清除指定分类的列表缓存（AI 分析完成时调用）
     */
    public void evictByCategory(Long categoryId) {
        if (redisTemplate == null || categoryId == null) {
            log.debug("Redis not available or categoryId is null, skipping cache eviction");
            return;
        }
        try {
            String pattern = String.format("%s*:%d:*", CACHE_KEY_PREFIX, categoryId);
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("Cache evicted by category: {} keys (categoryId={})", deleted, categoryId);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache by category: categoryId={}", categoryId, e);
        }
    }
}
