package com.example.shorturlpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务线程池配置
 * 
 * 优化内存占用和并发性能：
 * - 限制最大线程数，避免过多线程消耗内存
 * - 使用有界队列，防止 OOM
 * - 合理的线程空闲超时时间
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通用异步任务线程池
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：CPU 核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 最大线程数：核心数的 2 倍
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // 队列容量：有界队列，防止 OOM
        executor.setQueueCapacity(100);
        // 线程名称前缀
        executor.setThreadNamePrefix("async-executor-");
        // 线程空闲超时时间（秒）
        executor.setKeepAliveSeconds(60);
        // 等待任务全部完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 关闭时强制等待的时间（秒）
        executor.setAwaitTerminationSeconds(60);
        // 队列满时的拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * 短链接点击统计专用线程池
     * 用于异步更新点击量，避免阻塞主流程
     */
    @Bean(name = "clickStatsExecutor")
    public Executor clickStatsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 点击统计是轻量级操作，使用较小的线程池
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("click-stats-");
        executor.setKeepAliveSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        
        executor.initialize();
        return executor;
    }
}
