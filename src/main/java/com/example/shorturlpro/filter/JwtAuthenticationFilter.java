package com.example.shorturlpro.filter;

import com.example.shorturlpro.service.UserDetailsServiceImpl;
import com.example.shorturlpro.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器：解析请求头中的Token，设置认证信息
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. 提取请求头中的Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;

        // 2. 校验Token格式（Bearer + Token）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取Token
        jwtToken = authHeader.substring(7);
        try {
            // 4. 解析用户名
            username = jwtUtil.extractUsername(jwtToken);

            // 5. 用户名存在且未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. 加载用户信息
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. 验证Token有效性
                if (jwtUtil.isTokenValid(jwtToken, userDetails)) {
                    // 8. 设置认证信息
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("JWT认证失败", e);
        }

        // 9. 继续过滤链
        filterChain.doFilter(request, response);
    }
}