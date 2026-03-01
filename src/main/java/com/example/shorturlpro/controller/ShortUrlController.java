package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.ShortUrlGenerateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateResponse;
import com.example.shorturlpro.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 短链接控制器
 * 提供短链接生成和跳转接口
 */
@Slf4j
@RestController
@RequestMapping("/api/short-url")
@RequiredArgsConstructor
@Tag(name = "短链接服务", description = "短链接生成与跳转相关接口")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    /**
     * 生成短链接接口
     * POST /api/short-url/generate
     *
     * @param request 生成请求参数
     * @return 生成结果
     */
    @PostMapping("/generate")
    @Operation(
            summary = "生成短链接",
            description = "将长链接转换为短链接\n\n**权限要求**：公开接口，无需认证"
    )
    @ApiResponse(responseCode = "200", description = "生成成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ShortUrlGenerateResponse.class)))
    @ApiResponse(responseCode = "400", description = "请求参数错误")
    @ApiResponse(responseCode = "500", description = "服务器内部错误")
    public ResponseEntity<?> generateShortUrl(
            @Valid @RequestBody ShortUrlGenerateRequest request) {

        try {
            log.info("收到短链接生成请求: {}", request.getOriginalUrl());
            
            ShortUrlGenerateResponse response = shortUrlService.generateShortUrl(request);
            
            log.info("短链接生成成功: {} -> {}", response.getShortCode(), response.getShortUrl());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("参数校验失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("生成短链接失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 短链接跳转接口
     * GET /{shortCode}
     *
     * @param shortCode 短码
     * @return 302重定向响应
     */
    @GetMapping("/{shortCode}")
    @Operation(
            summary = "短链接跳转",
            description = "根据短码进行302重定向到原始链接，并更新访问统计\n\n**权限要求**：公开接口，无需认证"
    )
    @ApiResponse(responseCode = "302", description = "重定向到原始链接")
    @ApiResponse(responseCode = "404", description = "短链接不存在或已被禁用")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @Parameter(description = "短链接码", example = "abc123xyz")
            @PathVariable String shortCode) {

        try {
            log.info("收到短链接跳转请求: {}", shortCode);
            
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

    /**
     * 错误处理：捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("控制器异常", e);
        Map<String, Object> error = new HashMap<>();
        error.put("code", 500);
        error.put("message", "服务器内部错误：" + e.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}