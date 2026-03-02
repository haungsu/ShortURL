package com.example.shorturlpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import com.example.shorturlpro.entity.ShortUrlStatus;

/**
 * 管理员创建短链接请求DTO
 * 用于管理员界面的手动创建短链接功能
 */
@Data
public class ShortUrlCreateRequest {

    /**
     * 短链接名称（用于展示）
     */
    private String name;

    /**
     * 自定义短码（可选）
     */
    private String shortCode;

    /**
     * 原始长链接
     */
    @NotBlank(message = "原始长链接不能为空")
    private String originalUrl;

    /**
     * 状态（ENABLED/DISABLED）
     */
    private ShortUrlStatus status;

    /**
     * 应用标识（可选）
     */
    private String appId;

    /**
     * 过期时间（可选）
     */
    private LocalDateTime expiresAt;
}