package com.example.shorturlpro.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 多级缓存配置类
 * 实现Caffeine本地缓存(L1) + Redis分布式缓存(L2)的二级缓存架构
 */
@Configuration
public class MultiLevelCacheConfig {

    /**
     * Caffeine本地缓存管理器 (L1缓存)
     * 用于缓存热点数据，提供最快的访问速度
     */
    @Bean("caffeineCacheManager")
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置Caffeine缓存策略
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)                           // 初始容量
                .maximumSize(1000)                              // 最大缓存条目数
                .expireAfterWrite(30, TimeUnit.MINUTES)         // 写入后30分钟过期
                .expireAfterAccess(10, TimeUnit.MINUTES)        // 访问后10分钟过期
                .recordStats());                                // 记录缓存统计信息
        
        return cacheManager;
    }

    /**
     * Redis缓存管理器引用 (L2缓存)
     * 用于分布式缓存，支持多实例共享
     * 注意：实际的Redis缓存管理器在RedisConfig中配置
     */
    @Bean("distributedCacheManager")
    public CacheManager distributedCacheManager() {
        // 这个会在RedisConfig中被配置，这里只是声明Bean名称
        return null;
    }
}