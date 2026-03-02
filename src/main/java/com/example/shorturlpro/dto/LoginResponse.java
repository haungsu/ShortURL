package com.example.shorturlpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * Access Token
     */
    private String accessToken;
    
    /**
     * Refresh Token
     */
    private String refreshToken;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色（ROLE_ADMIN/ROLE_USER）
     */
    private String role;
    
    /**
     * 昵称
     */
    private String nickname;
    
    // 兼容旧版本的构造函数
    public LoginResponse(String accessToken, String username, String role, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = null;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
    }
}