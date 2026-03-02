package com.example.shorturlpro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 短链接实体类（补充userId字段）
 * 映射数据库t_short_url表
 */
@Data
@Entity
@Table(name = "t_short_url")
public class ShortUrl {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 短链接名称（用于展示）
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 原始长链接
     */
    @Column(name = "original_url", nullable = false, length = 2000)
    private String originalUrl;

    /**
     * 短码（6位Base62）
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    /**
     * 状态（ENABLED/DISABLED）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShortUrlStatus status;

    /**
     * 点击次数
     */
    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    /**
     * 应用ID（扩展字段）
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 新增：创建者用户ID（关联t_user表）
     * 未登录用户生成的短链接，该字段为NULL
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 过期时间（NULL表示永久有效）
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}