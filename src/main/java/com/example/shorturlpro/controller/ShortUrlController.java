package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.ApiResponse;
import com.example.shorturlpro.dto.ShortUrlCreateRequest;
import com.example.shorturlpro.dto.ShortUrlResponse;
import com.example.shorturlpro.dto.ShortUrlGenerateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateResponse;
import com.example.shorturlpro.dto.ShortUrlResponse;
import com.example.shorturlpro.entity.ShortUrl;
import com.example.shorturlpro.entity.ShortUrlStatus;
import com.example.shorturlpro.entity.User;
import com.example.shorturlpro.repository.ShortUrlRepository;
import com.example.shorturlpro.repository.UserRepository;
import com.example.shorturlpro.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;

    /**
     * 生成短链接接口
     * POST /api/short-url/generate
     */
    @PostMapping("/generate")
    @Operation(
            summary = "生成短链接",
            description = "将长链接转换为短链接\n\n**权限要求**：公开接口，无需认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "生成成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ShortUrlGenerateResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    public ResponseEntity<ApiResponse<ShortUrlGenerateResponse>> generateShortUrl(
            @Valid @RequestBody ShortUrlGenerateRequest request) {

        try {
            // 获取当前登录用户信息
            String username = "none";
            String role = "none";
            Long userId = null;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication.getPrincipal() instanceof String)) {
                // 已登录：获取用户名、角色
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                final String loginUsername = userDetails.getUsername(); // 声明为final
                username = loginUsername; // 赋值给原变量（不影响）
                role = userDetails.getAuthorities().iterator().next().getAuthority();

                // 通过username查询User实体，获取真实userId
                User loginUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("登录用户不存在：" + loginUsername));
                userId = loginUser.getId(); // 拿到用户的真实ID

                log.info("当前登录用户：{}（ID：{}），角色：{}", username, userId, role);
            } else {
                log.info("未登录用户生成短链接（匿名用户）");
            }

            log.info("收到短链接生成请求: {}，用户ID：{}，用户名：{}，角色：{}",
                    request.getOriginalUrl(), userId, username, role);

            // 传递userId到服务层
            ShortUrlGenerateResponse response = shortUrlService.generateShortUrl(request, userId);

            log.info("短链接生成成功: {} -> {}，用户ID：{}，用户名：{}",
                    response.getShortCode(), response.getShortUrl(), userId, username);
            
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("参数校验失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));

            // 捕获用户不存在的异常
        } catch (RuntimeException e) {
            log.warn("用户信息异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unauthorized(e.getMessage()));

        } catch (Exception e) {
            log.error("生成短链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("生成短链接失败: " + e.getMessage()));
        }
    }

    /**
     * 短链接跳转接口
     * GET /{shortCode}
     */
    @GetMapping("/{shortCode}")
    @Operation(
            summary = "短链接跳转",
            description = "根据短码进行302重定向到原始链接，并更新访问统计\n\n**权限要求**：公开接口，无需认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "重定向到原始链接")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "短链接不存在或已被禁用")
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
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    @Operation(
            summary = "获取系统统计信息",
            description = "获取短链接系统的统计信息\n\n**权限要求**：需要认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)))
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总数量
            stats.put("totalCount", shortUrlRepository.countTotal());
            
            // 总点击量
            stats.put("totalClicks", shortUrlRepository.sumTotalClickCount());
            
            // 各状态统计
            long enabledCount = 0;
            long disabledCount = 0;
            List<Object[]> statusCounts = shortUrlRepository.countByStatus();
            for (Object[] row : statusCounts) {
                ShortUrlStatus status = (ShortUrlStatus) row[0];
                Long count = (Long) row[1];
                if (status == ShortUrlStatus.ENABLED) {
                    enabledCount = count;
                } else if (status == ShortUrlStatus.DISABLED) {
                    disabledCount = count;
                }
            }
            stats.put("enabledCount", enabledCount);
            stats.put("disabledCount", disabledCount);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新短链接接口
     * PUT /api/short-url/{id}
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "更新短链接",
            description = "根据ID更新短链接信息\n\n**权限要求**：需要认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> updateShortUrl(
            @Parameter(description = "短链接ID") @PathVariable Long id,
            @Valid @RequestBody ShortUrlCreateRequest request) {
        
        try {
            log.info("收到更新短链接请求: id={}, name={}", id, request.getName());
            
            ShortUrl updatedShortUrl = shortUrlService.updateShortUrlByAdmin(id, request);
            
            log.info("短链接更新成功: id={}", id);
            
            return ResponseEntity.ok(ApiResponse.success(convertToResponseDto(updatedShortUrl)));
            
        } catch (RuntimeException e) {
            log.warn("更新短链接失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("更新短链接失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("更新短链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新短链接失败: " + e.getMessage()));
        }
    }

    /**
     * 更新短链接状态接口
     */
    @PatchMapping("/status/{id}")
    @Operation(
            summary = "更新短链接状态",
            description = "更新短链接的状态（启用/禁用）\n\n**权限要求**：需要认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<?> updateShortUrlStatus(
            @Parameter(description = "短链接ID") @PathVariable Long id,
            @Parameter(description = "状态信息") @RequestBody Map<String, String> request) {
        
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                throw new IllegalArgumentException("状态参数不能为空");
            }
            
            ShortUrlStatus status = ShortUrlStatus.valueOf(statusStr.toUpperCase());
            
            log.info("更新短链接状态: id={}, status={}", id, status);
            
            // 查找短链接
            ShortUrl shortUrl = shortUrlRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("短链接不存在，ID: " + id));
            
            // 更新状态
            shortUrl.setStatus(status);
            shortUrl.setUpdatedAt(LocalDateTime.now());
            shortUrlRepository.save(shortUrl);
            
            // 清除缓存
            shortUrlService.clearCache(shortUrl.getShortCode());
            
            log.info("短链接状态更新成功: id={}, status={}", id, status);
            
            // 转换为响应DTO
            ShortUrlResponse responseDto = convertToResponseDto(shortUrl);
            return ResponseEntity.ok(ApiResponse.success(responseDto));
            
        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.warn("更新状态失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("更新状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 将 ShortUrl 实体转换为 ShortUrlResponse DTO
     */
    private ShortUrlResponse convertToResponseDto(ShortUrl shortUrl) {
        ShortUrlResponse dto = new ShortUrlResponse();
        dto.setId(shortUrl.getId());
        dto.setName(shortUrl.getName());
        dto.setShortCode(shortUrl.getShortCode());
        dto.setShortUrl(getFullShortUrl(shortUrl.getShortCode()));
        dto.setOriginalUrl(shortUrl.getOriginalUrl());
        dto.setStatus(shortUrl.getStatus().name());
        dto.setClickCount(shortUrl.getClickCount());
        dto.setCreatedAt(shortUrl.getCreatedAt());
        dto.setUpdatedAt(shortUrl.getUpdatedAt());
        dto.setUserId(shortUrl.getUserId());
        dto.setAppId(shortUrl.getAppId());
        dto.setExpiresAt(shortUrl.getExpiresAt());
        return dto;
    }

    /**
     * 构造完整的短链接URL
     */
    private String getFullShortUrl(String shortCode) {
        return "http://localhost:8080/" + shortCode;
    }

    /**
     * 管理员创建短链接接口
     * POST /api/short-url
     */
    @PostMapping("")
    @Operation(
            summary = "管理员创建短链接",
            description = "管理员手动创建短链接\n\n**权限要求**：需要认证且具有管理员权限"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ShortUrlGenerateResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<?> createShortUrl(
            @Valid @RequestBody ShortUrlCreateRequest request) {
        
        try {
            log.info("收到管理员创建短链接请求: {}", request);
            
            // 获取当前登录用户信息
            String username = "unknown";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                username = userDetails.getUsername();
                log.info("当前操作用户: {}", username);
            }
            
            // 构造 ShortUrlGenerateRequest 对象
            ShortUrlGenerateRequest generateRequest = new ShortUrlGenerateRequest();
            generateRequest.setOriginalUrl(request.getOriginalUrl());
            // 注意：ShortUrlGenerateRequest 中没有 shortCode 字段，所以这里不设置
            
            // 调用服务层生成短链接
            ShortUrlGenerateResponse response = shortUrlService.generateShortUrl(generateRequest, null);
            
            // 更新数据库中的额外字段
            Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(response.getShortCode());
            if (shortUrlOpt.isPresent()) {
                ShortUrl shortUrl = shortUrlOpt.get();
                // 设置名称
                if (request.getName() != null && !request.getName().isEmpty()) {
                    shortUrl.setName(request.getName());
                }
                
                // 设置自定义短码（如果提供）
                if (request.getShortCode() != null && !request.getShortCode().isEmpty()) {
                    shortUrl.setShortCode(request.getShortCode());
                }
                
                // 设置状态
                if (request.getStatus() != null) {
                    shortUrl.setStatus(request.getStatus());
                }
                
                // 设置应用ID
                if (request.getAppId() != null && !request.getAppId().isEmpty()) {
                    shortUrl.setAppId(request.getAppId());
                }
                
                // 设置过期时间
                if (request.getExpiresAt() != null) {
                    shortUrl.setExpiresAt(request.getExpiresAt());
                }
                
                shortUrlRepository.save(shortUrl);
            }
            
            log.info("管理员创建短链接成功: {} -> {}", response.getShortCode(), response.getShortUrl());
            // 包装在统一的响应格式中
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", response);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("参数校验失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
            
        } catch (Exception e) {
            log.error("创建短链接失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除短链接接口
     * DELETE /api/short-url/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除短链接",
            description = "根据ID删除短链接记录\n\n**权限要求**：需要认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "短链接不存在")
    public ResponseEntity<ApiResponse<Object>> deleteShortUrl(
            @Parameter(description = "短链接ID") @PathVariable Long id) {
        
        try {
            log.info("收到删除短链接请求: id={}", id);
            
            // 调用服务层删除方法
            shortUrlService.deleteShortUrlByAdmin(id);
            
            log.info("短链接删除成功: id={}", id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (RuntimeException e) {
            log.warn("删除短链接失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("删除短链接失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("删除短链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除短链接失败: " + e.getMessage()));
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