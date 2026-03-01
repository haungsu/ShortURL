package com.example.shorturlpro.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接响应DTO
 * 向前端返回短链接数据（隐藏不必要的字段）
 */
@Data
public class ShortUrlResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 短链接名称
     */
    private String name;

    /**
     * 原始长链接
     */
    private String originalUrl;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 完整短链接（拼接域名+短码）
     */
    private String shortUrl;

    /**
     * 状态（ENABLED/DISABLED）
     */
    private String status;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}