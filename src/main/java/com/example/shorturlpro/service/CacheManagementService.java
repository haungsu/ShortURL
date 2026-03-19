package com.example.shorturlpro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存管理服务
 * 负责缓存预热、失效和多级缓存协调
 */
@Slf4j
@Service
public class CacheManagementService {

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager caffeineCacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    private static final String[] CACHE_NAMES = {
        "shortUrl",           // 短链接缓存
        "shortUrlDetail",     // 短链接详情缓存
        "statistics"          // 统计数据缓存
    };

    /**
     * 应用启动时预热缓存
     */
    @PostConstruct
    public void warmUpCaches() {
        log.info("开始预热缓存...");
        CompletableFuture.runAsync(this::performCacheWarmUp);
    }

    /**
     * 执行缓存预热
     */
    private void performCacheWarmUp() {
        try {
            // 预热常用缓存
            warmUpShortUrlCache();
            warmUpStatisticsCache();
            
            log.info("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }

    /**
     * 预热短链接缓存
     */
    private void warmUpShortUrlCache() {
        // 这里可以添加具体的预热逻辑
        // 例如：加载热门短链接到缓存中
        log.debug("预热短链接缓存");
    }

    /**
     * 预热统计数据缓存
     */
    private void warmUpStatisticsCache() {
        // 预热系统统计数据
        log.debug("预热统计数据缓存");
    }

    /**
     * 清除指定缓存
     * @param cacheName 缓存名称
     */
    public void evictCache(String cacheName) {
        try {
            // 清除Caffeine缓存
            Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                caffeineCache.clear();
                log.debug("已清除Caffeine缓存: {}", cacheName);
            }

            // 清除Redis缓存
            Cache redisCache = redisCacheManager.getCache(cacheName);
            if (redisCache != null) {
                redisCache.clear();
                log.debug("已清除Redis缓存: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("清除缓存失败: {}", cacheName, e);
        }
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCaches() {
        for (String cacheName : CACHE_NAMES) {
            evictCache(cacheName);
        }
        log.info("已清除所有缓存");
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        // 获取Caffeine缓存统计
        for (String cacheName : CACHE_NAMES) {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) {
                // 这里可以根据实际需要获取具体的统计信息
                stats.addCacheInfo(cacheName, "Caffeine", "Active");
            }
        }
        
        return stats;
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private Collection<CacheInfo> cacheInfos;

        public void addCacheInfo(String name, String type, String status) {
            // 实现缓存信息添加逻辑
        }

        // getter和setter方法
        public Collection<CacheInfo> getCacheInfos() {
            return cacheInfos;
        }

        public void setCacheInfos(Collection<CacheInfo> cacheInfos) {
            this.cacheInfos = cacheInfos;
        }
    }

    /**
     * 缓存信息类
     */
    public static class CacheInfo {
        private String name;
        private String type;
        private String status;

        // 构造函数、getter和setter方法
        public CacheInfo(String name, String type, String status) {
            this.name = name;
            this.type = type;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}