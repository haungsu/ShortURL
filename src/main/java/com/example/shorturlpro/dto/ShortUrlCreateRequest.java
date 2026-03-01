package com.example.shorturlpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增短链接请求DTO
 * 接收前端新增接口的请求参数
 */
@Data
public class ShortUrlCreateRequest {

    /**
     * 短链接名称
     * 非空，长度2-50位
     */
    @NotBlank(message = "短链接名称不能为空")
    @Size(min = 2, max = 50, message = "短链接名称长度需在2-50位之间")
    private String name;

    /**
     * 原始长链接
     * 非空，需符合URL格式（后续可加URL格式校验注解）
     */
    @NotBlank(message = "原始长链接不能为空")
    private String originalUrl;

    /**
     * 应用标识（可选）
     */
    private String appId;

    /**
     * 过期时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     */
    private String expiresAt;
}