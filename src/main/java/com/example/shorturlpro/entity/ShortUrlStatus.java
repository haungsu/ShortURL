package com.example.shorturlpro.entity;

/**
 * 短链接状态枚举
 * 对应数据库t_short_url表的status字段
 */
public enum ShortUrlStatus {
    /**
     * 启用状态
     */
    ENABLED,
    /**
     * 禁用状态
     */
    DISABLED
}