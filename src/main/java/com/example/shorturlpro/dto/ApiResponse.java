package com.example.shorturlpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 * 
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一API响应结果")
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "响应码", example = "200")
    private Integer code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    @Schema(description = "时间戳", example = "1710835200000")
    private Long timestamp;
    
    // 私有构造函数
    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    private ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    private ApiResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功");
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message);
    }
    
    /**
     * 失败响应（指定错误码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }
    
    /**
     * 参数错误
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message);
    }
    
    /**
     * 未认证
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message);
    }
    
    /**
     * 权限不足
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message);
    }
    
    /**
     * 资源不存在
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message);
    }
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}