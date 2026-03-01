package com.example.shorturlpro.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理控制器中抛出的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@Valid注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 400);
        error.put("message", "参数校验失败");

        StringBuilder details = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            details.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        error.put("details", details.toString());
        error.put("timestamp", System.currentTimeMillis());

        log.warn("参数校验失败: {}", details);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 400);
        error.put("message", "参数绑定失败");

        StringBuilder details = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            details.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        error.put("details", details.toString());
        error.put("timestamp", System.currentTimeMillis());

        log.warn("参数绑定失败: {}", details);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 500);
        error.put("message", e.getMessage());
        error.put("timestamp", System.currentTimeMillis());

        log.error("运行时异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 500);
        error.put("message", "服务器内部错误：" + e.getMessage());
        error.put("timestamp", System.currentTimeMillis());

        log.error("服务器内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}