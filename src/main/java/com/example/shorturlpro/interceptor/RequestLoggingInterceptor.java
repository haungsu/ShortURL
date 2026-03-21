package com.example.shorturlpro.interceptor;

import com.example.shorturlpro.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器
 * 记录所有HTTP请求的访问日志
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        // 初始化请求上下文
        LogUtil.initRequestContext(request);
        
        // 记录请求开始
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        log.debug("开始处理请求: {} {}", request.getMethod(), request.getRequestURI());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, @Nullable Exception ex) throws Exception {
        try {
            // 计算请求耗时
            Long startTime = (Long) request.getAttribute("startTime");
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            
            // 获取响应状态码
            int status = response.getStatus();
            
            // 记录访问日志
            String message = String.format("%s %s -> %d", 
                request.getMethod(), 
                request.getRequestURI(), 
                status);
            
            LogUtil.logAccess(message, duration);
            
            // 如果有异常，记录错误日志
            if (ex != null) {
                log.error("请求处理异常: {} {} -> {}", 
                    request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
            }
            
            log.debug("请求处理完成: {} {} -> {} (耗时: {}ms)", 
                request.getMethod(), request.getRequestURI(), status, duration);
                
        } finally {
            // 清理请求上下文
            LogUtil.clearRequestContext();
        }
    }
}