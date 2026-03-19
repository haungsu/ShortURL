package com.example.shorturlpro.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 监控指标配置类
 * 配置自定义业务指标和监控维度
 */
@Configuration
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    // 自定义指标名称
    public static final String SHORT_URL_GENERATE_COUNT = "shorturl.generate.count";
    public static final String SHORT_URL_REDIRECT_COUNT = "shorturl.redirect.count";
    public static final String SHORT_URL_REDIRECT_TIME = "shorturl.redirect.time";
    public static final String CACHE_HIT_RATE = "cache.hit.rate";
    public static final String DATABASE_QUERY_TIME = "database.query.time";

    /**
     * 初始化自定义监控指标
     */
    @PostConstruct
    public void initCustomMetrics() {
        // 注册短链接生成计数器
        meterRegistry.counter(SHORT_URL_GENERATE_COUNT);
        
        // 注册短链接跳转计数器
        meterRegistry.counter(SHORT_URL_REDIRECT_COUNT);
        
        // 注册短链接跳转耗时计时器
        Timer.builder(SHORT_URL_REDIRECT_TIME)
                .description("短链接跳转耗时")
                .register(meterRegistry);
                
        // 注册缓存命中率
        meterRegistry.gauge(CACHE_HIT_RATE, 0.0);
        
        // 注册数据库查询耗时
        Timer.builder(DATABASE_QUERY_TIME)
                .description("数据库查询耗时")
                .register(meterRegistry);
    }

    /**
     * 记录短链接生成次数
     */
    public void recordShortUrlGenerate() {
        meterRegistry.counter(SHORT_URL_GENERATE_COUNT).increment();
    }

    /**
     * 记录短链接跳转次数
     */
    public void recordShortUrlRedirect() {
        meterRegistry.counter(SHORT_URL_REDIRECT_COUNT).increment();
    }

    /**
     * 记录短链接跳转耗时
     * @param duration 耗时（毫秒）
     */
    public void recordShortUrlRedirectTime(long duration) {
        meterRegistry.timer(SHORT_URL_REDIRECT_TIME).record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 更新缓存命中率
     * @param hitRate 命中率（0-1之间的值）
     */
    public void updateCacheHitRate(double hitRate) {
        // 注意：gauge需要通过回调函数来更新值
        meterRegistry.gauge(CACHE_HIT_RATE, hitRate);
    }

    /**
     * 记录数据库查询耗时
     * @param duration 耗时（毫秒）
     */
    public void recordDatabaseQueryTime(long duration) {
        meterRegistry.timer(DATABASE_QUERY_TIME).record(duration, TimeUnit.MILLISECONDS);
    }
}