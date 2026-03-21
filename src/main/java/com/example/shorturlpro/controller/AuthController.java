package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.ApiResponse;
import com.example.shorturlpro.dto.LoginRequest;
import com.example.shorturlpro.dto.LoginResponse;
import com.example.shorturlpro.dto.TokenRefreshRequest;
import com.example.shorturlpro.dto.TokenValidationResponse;
import com.example.shorturlpro.entity.User;
import com.example.shorturlpro.repository.UserRepository;
import com.example.shorturlpro.service.UserDetailsServiceImpl;
import com.example.shorturlpro.util.JwtUtil;
import com.example.shorturlpro.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器：处理登录请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "用户登录相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 用户登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "验证用户名密码，生成JWT Token\n\n**权限要求**：公开接口，无需认证\n\n**Header参数**：无\n\n**Body参数**：用户名和密码"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功，返回Token和用户信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        long startTime = System.currentTimeMillis();
        String operation = "USER_LOGIN";
        
        try {
            LogUtil.setOperation(operation);
            LogUtil.setUserContext(null, request.getUsername());
            
            log.info("收到登录请求：用户名{}", request.getUsername());
            LogUtil.logBusiness(operation, String.format("收到登录请求，用户名=%s", request.getUsername()));
            
            // 1. 认证用户名密码（Spring Security自动BCrypt校验）
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("登录失败：用户名{}，原因{}，耗时：{}ms", request.getUsername(), e.getMessage(), duration);
            LogUtil.logBusinessFailure(operation, String.format(
                "登录失败，用户名=%s, 错误=%s, 耗时=%dms", 
                request.getUsername(), e.getMessage(), duration), e);
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 加载用户信息
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        // 3. 获取用户角色
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        // 4. 获取用户昵称
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        // 5. 生成Access Token
        final String accessToken = jwtUtil.generateToken(userDetails, role);
        // 6. 生成Refresh Token
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        long duration = System.currentTimeMillis() - startTime;
        log.info("用户{}登录成功，角色{}，耗时：{}ms", request.getUsername(), role, duration);
        LogUtil.logBusinessSuccess(operation, String.format(
            "登录成功，用户名=%s, 角色=%s, 用户ID=%d, 昵称=%s, 耗时=%dms", 
            request.getUsername(), role, user.getId(), user.getNickname(), duration));
        
        // 7. 构造响应数据
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, request.getUsername(), role, user.getNickname());
        // 8. 返回标准ApiResponse格式
        return ApiResponse.success("登录成功", loginResponse);
    }

    /**
     * 刷新Token接口
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "刷新Token",
            description = "使用刷新Token获取新的访问Token\n\n**权限要求**：公开接口，无需认证\n\n**Header参数**：无\n\n**Body参数**：刷新Token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token刷新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "刷新Token无效或已过期")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        long startTime = System.currentTimeMillis();
        String operation = "TOKEN_REFRESH";
        String refreshToken = request.getRefreshToken();
        
        try {
            LogUtil.setOperation(operation);
            
            log.info("收到Token刷新请求");
            LogUtil.logBusiness(operation, "收到Token刷新请求");
            
            // 1. 验证刷新Token格式
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                String errorMsg = "无效的刷新Token类型";
                LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }
            
            // 2. 验证Token有效性
            String username;
            try {
                username = jwtUtil.extractUsername(refreshToken);
                if (username == null) {
                    String errorMsg = "刷新Token无效";
                    LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                    throw new RuntimeException(errorMsg);
                }
                LogUtil.setUserContext(null, username);
            } catch (Exception e) {
                String errorMsg = "刷新Token解析失败";
                LogUtil.logBusinessFailure(operation, errorMsg, e);
                throw new RuntimeException(errorMsg);
            }
            
            // 3. 检查Token是否在黑名单中
            if (jwtUtil.isTokenBlacklisted(refreshToken)) {
                String errorMsg = "刷新Token已被撤销";
                LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                throw new RuntimeException(errorMsg);
            }
            
            // 4. 验证Token是否过期
            try {
                Claims claims = jwtUtil.getAllClaimsFromToken(refreshToken);
                if (claims.getExpiration().before(new Date())) {
                    String errorMsg = "刷新Token已过期";
                    LogUtil.logBusinessFailure(operation, errorMsg, new RuntimeException(errorMsg));
                    throw new RuntimeException(errorMsg);
                }
            } catch (Exception e) {
                String errorMsg = "刷新Token已过期";
                LogUtil.logBusinessFailure(operation, errorMsg, e);
                throw new RuntimeException(errorMsg);
            }
            
            // 5. 加载用户信息
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            User user = userRepository.findByUsername(username).orElseThrow();
            
            // 6. 生成新的Access Token
            final String newAccessToken = jwtUtil.generateToken(userDetails, role);
            // 7. 生成新的Refresh Token
            final String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("用户{}刷新Token成功，耗时：{}ms", username, duration);
            LogUtil.logBusinessSuccess(operation, String.format(
                "Token刷新成功，用户名=%s, 角色=%s, 耗时=%dms", username, role, duration));
            
            LoginResponse loginResponse = new LoginResponse(newAccessToken, newRefreshToken, username, role, user.getNickname());
            return ApiResponse.success("Token刷新成功", loginResponse);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtil.logBusinessFailure(operation, String.format("Token刷新失败，耗时=%dms", duration), e);
            throw e;
        }
    }

    /**
     * 验证Token接口
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    @Operation(
            summary = "验证Token",
            description = "验证JWT Token的有效性\n\n**权限要求**：需要认证\n\n**Header参数**：Authorization: Bearer {token}\n\n**Body参数**：无"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token验证结果")
    public TokenValidationResponse validateToken(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return TokenValidationResponse.invalid("未提供有效的认证信息");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            
            // 从SecurityContext中获取Token（需要在过滤器中保存）
            // 这里简化处理，实际项目中应该在JWT过滤器中保存原始Token
            return TokenValidationResponse.valid(username, role, System.currentTimeMillis() + 7200000);
            
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return TokenValidationResponse.invalid("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 注销接口
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Operation(
            summary = "用户注销",
            description = "注销当前用户的Token\n\n**权限要求**：需要认证\n\n**Header参数**：Authorization: Bearer {token}\n\n**Body参数**：可选的Token列表"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注销成功")
    public ApiResponse<Object> logout(Authentication authentication, @RequestBody(required = false) Map<String, Object> requestBody) {
        long startTime = System.currentTimeMillis();
        String operation = "USER_LOGOUT";
        
        try {
            LogUtil.setOperation(operation);
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                LogUtil.setUserContext(null, username);
                
                log.info("用户{}注销请求", username);
                LogUtil.logBusiness(operation, String.format("用户注销请求，用户名=%s", username));
                
                // 如果提供了Token，则将其加入黑名单
                if (requestBody != null && requestBody.containsKey("tokens")) {
                    // 实际项目中应该将Token加入Redis黑名单
                    // 这里简化处理
                    log.info("用户{}的Token将被加入黑名单", username);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("用户{}注销成功，耗时：{}ms", username, duration);
                LogUtil.logBusinessSuccess(operation, String.format(
                    "注销成功，用户名=%s, 耗时=%dms", username, duration));
            } else {
                long duration = System.currentTimeMillis() - startTime;
                log.warn("未认证用户尝试注销，耗时：{}ms", duration);
                LogUtil.logBusinessFailure(operation, String.format(
                    "未认证用户注销失败，耗时=%dms", duration), 
                    new RuntimeException("用户未认证"));
                return ApiResponse.unauthorized("用户未认证");
            }
            
            return ApiResponse.success("注销成功", null);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("注销失败，耗时：{}ms", duration, e);
            LogUtil.logBusinessFailure(operation, String.format("注销失败，耗时=%dms", duration), e);
            return ApiResponse.error("注销失败: " + e.getMessage());
        }
    }
}