package com.example.shorturlpro.config;

import com.example.shorturlpro.interceptor.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 处理静态资源配置和视图控制器
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    /**
     * 配置静态资源处理器
     * 在前后端分离架构中，后端主要处理API请求
     * 静态资源由前端开发服务器或Nginx提供
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加对favicon.ico的处理，返回空响应而不是404
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 缓存1小时
    }

    /**
     * 配置视图控制器
     * 在纯前后端分离架构中，后端不处理前端路由
     * 所有前端路由由Vue Router在前端处理
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 不添加任何前端路由转发
        // 前端路由完全由Vue Router在浏览器中处理
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**"
                );
    }
}