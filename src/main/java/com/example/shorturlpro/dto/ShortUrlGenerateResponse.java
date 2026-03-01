package com.example.shorturlpro.dto;

import lombok.Data;

/**
 * 生成短链接响应DTO
 * 返回短码和完整短链接
 */
@Data
public class ShortUrlGenerateResponse {

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 完整短链接（域名+短码）
     */
    private String shortUrl;
}