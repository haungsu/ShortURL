package com.example.shorturlpro.controller;

import com.example.shorturlpro.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @PathVariable String shortCode) {

        try {
            log.info("收到根路径短链接跳转请求: {}", shortCode);
            
            String originalUrl = shortUrlService.getOriginalUrlAndIncrement(shortCode);
            
            log.info("短链接跳转成功: {} -> {}", shortCode, originalUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
                    
        } catch (RuntimeException e) {
            log.warn("短链接跳转失败: {}, 原因: {}", shortCode, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}