package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.LoginRequest;
import com.example.shorturlpro.dto.LoginResponse;
import com.example.shorturlpro.entity.User;
import com.example.shorturlpro.repository.UserRepository;
import com.example.shorturlpro.service.UserDetailsServiceImpl;
import com.example.shorturlpro.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            description = "验证用户名密码，生成JWT Token\n\n**权限要求**：公开接口，无需认证"
    )
    @ApiResponse(responseCode = "200", description = "登录成功，返回Token和用户信息")
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
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
        // 5. 生成Token
        final String jwtToken = jwtUtil.generateToken(userDetails, role);

        log.info("用户{}登录成功，角色{}", request.getUsername(), role);
        // 6. 返回响应
        return new LoginResponse(jwtToken, request.getUsername(), role, user.getNickname());
    }
}