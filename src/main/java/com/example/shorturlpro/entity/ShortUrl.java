package com.example.shorturlpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接实体类
 * 对应数据库表t_short_url
 */
@Data // Lombok注解，自动生成get/set/toString等方法
@TableName("t_short_url") // MyBatis-Plus注解，指定数据库表名
@Entity // JPA注解，标记为实体类
@Table(name = "t_short_url") // JPA注解，指定数据库表名
public class ShortUrl {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO) // MyBatis-Plus主键注解
    @Id // JPA主键注解
    @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA自增策略
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
     * 短码（全局唯一）
     */
    private String shortCode;

    /**
     * 状态（ENABLED/DISABLED）
     */
    @Enumerated(EnumType.STRING) // JPA枚举映射
    private ShortUrlStatus status;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 应用标识
     */
    private String appId;

    /**
     * 过期时间（NULL表示永久）
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 关联创建用户ID（外键）
     */
    @TableField(value = "create_user_id")
    @Column(name = "create_user_id") // JPA列名映射
    private Long createUserId;
}