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
     * JWT Token
     */
    private String token;
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
}