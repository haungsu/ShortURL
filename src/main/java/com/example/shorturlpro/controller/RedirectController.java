package com.example.shorturlpro.controller;

import com.example.shorturlpro.service.ShortUrlService;
import com.example.shorturlpro.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 根路径跳转控制器
 * 处理根路径下的短链接跳转请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "短链接跳转", description = "根路径短链接跳转服务")
public class RedirectController {

    private final ShortUrlService shortUrlService;

    /**
     * 根路径短链接跳转 - 只处理6位Base62编码的短码
     * GET /{shortCode}
     *
     * @param shortCode 短码（必须是6位Base62字符）
     * @return 302重定向响应
     */
    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    @Operation(
            summary = "短链接跳转",
            description = "根据短码进行302重定向到原始链接，并更新访问统计\n\n**权限要求**：公开接口，无需认证"
    )
    @ApiResponse(responseCode = "302", description = "重定向到原始链接")
    @ApiResponse(responseCode = "404", description = "短链接不存在或已被禁用")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @Parameter(description = "短链接码", example = "abc123")
            @PathVariable String shortCode,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        String operation = "ROOT_REDIRECT";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, "visitor");
            
            log.info("收到根路径短链接跳转请求: {}", shortCode);
            LogUtil.logBusiness(operation, String.format("收到根路径跳转请求，短码=%s", shortCode));
            
            // 添加调试信息
            log.debug("请求URI: {}", request.getRequestURI());
            log.debug("请求URL: {}", request.getRequestURL());
            log.debug("Servlet路径: {}", request.getServletPath());
            log.debug("路径信息: {}", request.getPathInfo());
            
            String originalUrl = shortUrlService.getOriginalUrlAndIncrement(shortCode);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("短链接跳转成功: {} -> {}，耗时：{}ms", shortCode, originalUrl, duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "根路径跳转成功，短码=%s, 原始链接=%s, 耗时=%dms", shortCode, originalUrl, duration));
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
                    
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("短链接跳转失败: {}, 原因: {}，耗时：{}ms", shortCode, e.getMessage(), duration);
            LogUtil.logBusinessFailure(operation, String.format(
                "根路径跳转失败，短码=%s, 错误=%s, 耗时=%dms", shortCode, e.getMessage(), duration), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 测试端点 - 用于验证路由是否正常工作
     */
    @GetMapping("/test-route/{code}")
    public ResponseEntity<String> testRoute(
            @PathVariable String code) {
        return ResponseEntity.ok("路由测试成功，收到代码: " + code);
    }
}