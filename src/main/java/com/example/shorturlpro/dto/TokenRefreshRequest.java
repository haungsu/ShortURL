package com.example.shorturlpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token刷新请求DTO
 */
@Data
@Schema(description = "Token刷新请求")
public class TokenRefreshRequest {
    
    @NotBlank(message = "刷新Token不能为空")
    @Schema(description = "刷新Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}