package com.example.shorturlpro.config;

import com.example.shorturlpro.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 注入UserDetailsService
public class SecurityConfig {

    // 注入自定义的UserDetailsService（从数据库加载用户）
    private final UserDetailsServiceImpl userDetailsService;

    // 密码加密器（保持不变）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置接口权限规则（核心修改）
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 开发环境关闭CSRF（前端POST请求无需处理token，方便联调）
                .authorizeHttpRequests(auth -> auth
                        // 1. 公开接口：放行演示页、短链接生成、短链接跳转、静态资源
                        .requestMatchers(
                                "/", // 根路径（static/index.html演示页）
                                "/{shortCode}", // 短链接跳转接口（核心公开功能）
                                "/api/short-url/generate", // 后端生成短链接接口（公开）
                                "/static/**" // 静态资源（演示页CSS/JS）
                        ).permitAll()
                        // 2. 管理员接口：仅ROLE_ADMIN角色可访问
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // 与数据库role_code一致
                        // 3. 其他接口：需登录认证（如后续扩展的用户中心接口）
                        .anyRequest().authenticated()
                )
                // 4. 登录配置（与课程文档一致）
                .formLogin(form -> form
                        .loginPage("/login") // 自定义登录页路径（后续可实现登录页面）
                        .defaultSuccessUrl("/admin", true) // 登录成功后跳转到管控后台
                        .permitAll() // 放行登录页，允许匿名访问
                )
                // 5. 退出登录配置
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // 退出后跳回演示页
                        .permitAll()
                )
                // 6. 配置用户信息加载服务（关键补充）
                .userDetailsService(userDetailsService);

        return http.build();
    }
}