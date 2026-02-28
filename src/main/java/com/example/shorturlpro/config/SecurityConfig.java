package com.example.shorturlpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置接口权限规则
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 开发环境关闭CSRF（避免前端POST请求报错）
                .authorizeHttpRequests(auth -> auth
                        // 放行所有公开接口
                        .requestMatchers(
                                "/hello", // 测试接口，测完删
                                "/api/shortlinks/generate", // 生成短链接（公开）
                                "/api/short-url/**", // 短链接跳转（公开）
                                "/static/**" // 静态资源
                        ).permitAll()
                        // admin接口
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 其他未配置接口
                        .anyRequest().authenticated()
                )
                // 配置默认登录页面
                .formLogin(form -> form
                        .permitAll()
                )
                // 配置退出登录
                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }
}