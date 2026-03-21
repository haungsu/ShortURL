package com.example.shorturlpro.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * 日志工具类
 * 提供统一的日志记录方法和MDC上下文管理
 */
@Slf4j
@Component
public class LogUtil {

    // 访问日志记录器
    private static final Logger accessLogger = LoggerFactory.getLogger("access");
    
    // 业务日志记录器
    private static final Logger businessLogger = LoggerFactory.getLogger("business");

    /**
     * 初始化请求上下文
     * 在每个请求开始时调用
     */
    public static void initRequestContext(HttpServletRequest request) {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("requestId", requestId);
        
        // 记录客户端IP
        String clientIp = getClientIp(request);
        MDC.put("clientIp", clientIp);
        
        // 记录请求方法和URI
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        
        // 记录User-Agent
        String userAgent = request.getHeader("User-Agent");
        MDC.put("userAgent", userAgent != null ? userAgent : "Unknown");
        
        // 记录用户信息（如果有）
        MDC.put("userId", "anonymous");
        MDC.put("username", "anonymous");
    }

    /**
     * 设置用户上下文信息
     */
    public static void setUserContext(Long userId, String username) {
        if (userId != null) {
            MDC.put("userId", userId.toString());
        }
        if (username != null) {
            MDC.put("username", username);
        }
    }

    /**
     * 设置操作类型
     */
    public static void setOperation(String operation) {
        MDC.put("operation", operation);
    }

    /**
     * 清理请求上下文
     * 在每个请求结束时调用
     */
    public static void clearRequestContext() {
        MDC.clear();
    }

    /**
     * 记录访问日志
     */
    public static void logAccess(String message) {
        accessLogger.info(message);
    }

    /**
     * 记录访问日志（带耗时）
     */
    public static void logAccess(String message, long durationMs) {
        accessLogger.info("{} | duration={}ms", message, durationMs);
    }

    /**
     * 记录业务操作日志
     */
    public static void logBusiness(String operation, String message) {
        setOperation(operation);
        businessLogger.info(message);
    }

    /**
     * 记录业务操作日志（成功）
     */
    public static void logBusinessSuccess(String operation, String details) {
        setOperation(operation);
        businessLogger.info("SUCCESS | {}", details);
    }

    /**
     * 记录业务操作日志（失败）
     */
    public static void logBusinessFailure(String operation, String details, Exception e) {
        setOperation(operation);
        businessLogger.error("FAILURE | {} | error={}", details, e.getMessage());
    }

    /**
     * 获取客户端真实IP地址
     */
    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多个IP时取第一个
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        } else {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        return ip;
    }

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前请求ID
     */
    public static String getCurrentRequestId() {
        return MDC.get("requestId");
    }
}