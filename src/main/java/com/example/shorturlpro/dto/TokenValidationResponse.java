package com.example.shorturlpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token验证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token验证响应")
public class TokenValidationResponse {
    
    @Schema(description = "Token是否有效", example = "true")
    private boolean valid;
    
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    @Schema(description = "用户角色", example = "ROLE_ADMIN")
    private String role;
    
    @Schema(description = "过期时间戳", example = "1703123456789")
    private Long expireAt;
    
    @Schema(description = "错误信息", example = "Token已过期")
    private String errorMessage;
    
    public static TokenValidationResponse valid(String username, String role, Long expireAt) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(true);
        response.setUsername(username);
        response.setRole(role);
        response.setExpireAt(expireAt);
        return response;
    }
    
    public static TokenValidationResponse invalid(String errorMessage) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}