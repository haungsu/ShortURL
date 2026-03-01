package com.example.shorturlpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成短链接请求DTO
 * 接收演示页生成短码接口的请求参数
 */
@Data
public class ShortUrlGenerateRequest {

    /**
     * 原始长链接
     */
    @NotBlank(message = "原始长链接不能为空")
    private String originalUrl;

    /**
     * 应用标识（可选）
     */
    private String appId;
}