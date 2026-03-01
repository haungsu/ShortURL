package com.example.shorturlpro.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类，对应t_user表
 * 适配Spring Security用户认证与角色权限
 */
@Data
@Entity
@Table(name = "t_user")
@DynamicInsert // 插入时忽略null字段，使用数据库默认值
@DynamicUpdate // 更新时仅更新修改的字段
public class User {

    /**
     * 用户主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * BCrypt加密密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户昵称
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 是否删除：0-未删，1-已删
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 角色集合，关联t_user_role表（多对多）
     * 适配Spring Security的角色权限控制
     */
    @ElementCollection(fetch = FetchType.EAGER) // 立即加载，登录时直接获取角色
    @CollectionTable(
            name = "t_user_role", // 关联的中间表名
            joinColumns = @JoinColumn(name = "user_id") // 中间表关联当前表的字段
    )
    @Column(name = "role_code") // 中间表中角色编码的字段名
    private Set<String> roles = new HashSet<>();

    /**
     * 给用户添加管理员角色
     * 适配课程文档中的方法设计
     */
    public void addAdminRole() {
        this.roles.add("ROLE_ADMIN");
    }

    /**
     * 给用户添加普通用户角色
     */
    public void addUserRole() {
        this.roles.add("ROLE_USER");
    }

    // JPA自动填充创建/更新时间
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}