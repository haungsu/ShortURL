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
        try {
            // 1. 认证用户名密码（Spring Security自动BCrypt校验）
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("登录失败：用户名{}，原因{}", request.getUsername(), e.getMessage());
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 加载用户信息
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        // 3. 获取用户角色（取第一个角色，适配测试场景）
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        // 4. 获取用户昵称
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        // 5. 生成Access Token
        final String accessToken = jwtUtil.generateToken(userDetails, role);
        // 6. 生成Refresh Token
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        log.info("用户{}登录成功，角色{}", request.getUsername(), role);
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
        String refreshToken = request.getRefreshToken();
        
        // 1. 验证刷新Token格式
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新Token类型");
        }
        
        // 2. 验证Token有效性
        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
            if (username == null) {
                throw new RuntimeException("刷新Token无效");
            }
        } catch (Exception e) {
            throw new RuntimeException("刷新Token解析失败");
        }
        
        // 3. 检查Token是否在黑名单中
        if (jwtUtil.isTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("刷新Token已被撤销");
        }
        
        // 4. 验证Token是否过期
        try {
            Claims claims = jwtUtil.getAllClaimsFromToken(refreshToken);
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("刷新Token已过期");
            }
        } catch (Exception e) {
            throw new RuntimeException("刷新Token已过期");
        }
        
        // 5. 加载用户信息
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        User user = userRepository.findByUsername(username).orElseThrow();
        
        // 6. 生成新的Access Token
        final String newAccessToken = jwtUtil.generateToken(userDetails, role);
        // 7. 生成新的Refresh Token（可选，根据安全策略决定）
        final String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        log.info("用户{}刷新Token成功", username);
        LoginResponse loginResponse = new LoginResponse(newAccessToken, newRefreshToken, username, role, user.getNickname());
        return ApiResponse.success("Token刷新成功", loginResponse);
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
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                log.info("用户{}注销成功", username);
                
                // 如果提供了Token，则将其加入黑名单
                if (requestBody != null && requestBody.containsKey("tokens")) {
                    // 实际项目中应该将Token加入Redis黑名单
                    // 这里简化处理
                }
            }
            
            return ApiResponse.success("注销成功", null);
            
        } catch (Exception e) {
            log.error("注销失败", e);
            return ApiResponse.error("注销失败: " + e.getMessage());
        }
    }
}