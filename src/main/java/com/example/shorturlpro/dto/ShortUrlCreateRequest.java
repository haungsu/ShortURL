package com.example.shorturlpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;
import com.example.shorturlpro.entity.ShortUrlStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 管理员创建短链接请求DTO
 * 用于管理员界面的手动创建短链接功能
 */
@Data
public class ShortUrlCreateRequest {

    /**
     * 短链接名称（用于展示）
     */
    private String name = "未命名";

    /**
     * 自定义短码（可选）
     */
    @Pattern(regexp = "^[a-zA-Z0-9]{6,10}$", message = "自定义短码必须是6-10位字母/数字")
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
     * 用于接收字符串形式的状态值
     */
    private String statusString;
    
    /**
     * 设置状态的setter方法，支持字符串输入
     */
    public void setStatus(String statusStr) {
        if (statusStr != null) {
            this.status = ShortUrlStatus.valueOf(statusStr.toUpperCase());
        }
    }
    
    /**
     * 获取状态字符串表示
     */
    @JsonValue
    public String getStatusString() {
        return status != null ? status.name() : null;
    }
    
    /**
     * 从字符串创建枚举值
     */
    @JsonCreator
    public static ShortUrlStatus fromString(String value) {
        if (value == null) return null;
        return ShortUrlStatus.valueOf(value.toUpperCase());
    }

    /**
     * 应用标识（可选）
     * 默认值：admin（管理员创建）
     */
    private String appId = "admin";

    /**
     * 过期时间（可选）
     */
    private LocalDateTime expiresAt;
}