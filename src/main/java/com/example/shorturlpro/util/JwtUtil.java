package com.example.shorturlpro.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类（适配JJWT 0.11.5版本，修复verifyWith/claims方法解析问题）
 */
@Component
public class JwtUtil {
    // 注意：密钥长度必须≥256位（32个字符以上），否则会报密钥长度异常
    private final String SECRET_KEY = "short-url-pro-jwt-secret-key-1234567890-abcdefghijklmn";
    // Token过期时间：2小时（7200000毫秒）
    private final long EXPIRATION_TIME = 7200000;

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从Token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取Token中的所有声明（核心修复：0.11.x正确的解析方式）
     */
    private Claims extractAllClaims(String token) {
        // 修复点1：Jwts.parser() → Jwts.parserBuilder() + build()
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // 设置签名密钥（替代旧版setSigningKey）
                .build() // 构建JwtParser
                .parseClaimsJws(token) // 解析Token为ClaimsJws
                .getBody(); // 获取载荷（Claims）
    }

    /**
     * 生成Token（包含用户角色）
     */
    public String generateToken(UserDetails userDetails, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // 存入角色信息
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 创建Token（核心修复：0.11.x正确的构建方式）
     */
    private String createToken(Map<String, Object> claims, String subject) {
        // 修复点2：Jwts.builder() 正确调用claims方法
        return Jwts.builder()
                .setClaims(claims) // 设置自定义声明（替代旧版claims方法）
                .setSubject(subject) // 设置用户名（subject）
                .setIssuedAt(new Date(System.currentTimeMillis())) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .signWith(getSigningKey()) // 设置签名密钥（0.11.x无需指定算法，自动适配）
                .compact(); // 生成最终Token
    }

    /**
     * 验证Token有效性（用户名匹配 + 未过期）
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 提取Token过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 获取签名密钥（0.11.x要求密钥为SecretKey类型，而非String）
     */
    private SecretKey getSigningKey() {
        // 修复点3：使用Keys.hmacShaKeyFor生成符合要求的SecretKey
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}