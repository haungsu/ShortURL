package com.example.shorturlpro.filter;

import com.example.shorturlpro.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器（修复：增加token校验、跳过无需认证的路径）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 无需JWT认证的路径（登录、静态资源、首页等）
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/login",             // 登录接口
            "/",                  // 首页
            "/index.html",        // 首页HTML
            "/login.html",        // 登录页HTML
            "/admin-manage.html", // 管理员页面
            "/favicon.ico",       // 网站图标
            "/css/**",            // CSS静态资源
            "/js/**"              // JS静态资源
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("处理请求：{}", requestURI);

        // 1. 跳过无需JWT认证的路径
        if (shouldSkipFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = null;
        String username = null;

        try {
            // 2. 从Authorization请求头获取token（处理Bearer前缀）
            String authHeader = request.getHeader("Authorization");
            log.debug("Authorization头：{}", authHeader);

            // 3. 校验Authorization头格式（非空 + 以Bearer开头）
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 截取真正的token（去掉"Bearer "前缀）
                jwtToken = authHeader.substring(7);
                // 4. 前置校验token格式（必须包含两个点）
                if (jwtToken == null || !jwtToken.contains(".") || jwtToken.split("\\.").length != 3) {
                    log.warn("JWT格式错误：token无有效分隔符，token={}", jwtToken);
                    filterChain.doFilter(request, response);
                    return;
                }
                // 5. 解析用户名（仅当token格式合法时）
                username = jwtUtil.extractUsername(jwtToken);
            } else {
                log.warn("Authorization头为空或格式错误（非Bearer开头）");
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 用户名存在且未认证时，进行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 7. 验证token有效性
                if (jwtUtil.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT认证成功，用户名：{}", username);
                }
            }
        } catch (Exception e) {
            // 8. 捕获所有JWT相关异常（格式错误/过期/签名错误等），不中断过滤器链
            log.error("JWT认证失败", e);
            // 注意：这里不要抛出异常，仅打印日志，让请求继续走后续过滤器
        }

        // 9. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否需要跳过当前请求的JWT过滤
     */
    private boolean shouldSkipFilter(String requestURI) {
        return EXCLUDE_PATHS.stream().anyMatch(path -> {
            // 处理通配符（如/css/**）
            if (path.endsWith("/**")) {
                String prefix = path.substring(0, path.length() - 3);
                return requestURI.startsWith(prefix);
            }
            // 精确匹配
            return requestURI.equals(path);
        });
    }
}