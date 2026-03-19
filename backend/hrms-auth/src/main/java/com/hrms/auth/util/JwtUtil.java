package com.hrms.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author HRMS
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:hrms-jwt-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:7200}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username, String realName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("realName", realName);
        claims.put("tokenType", "access");
        
        return createToken(claims, username, expiration * 1000);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        
        return createToken(claims, username, refreshExpiration * 1000);
    }

    /**
     * 创建JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从令牌中获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("从令牌中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取真实姓名
     */
    public String getRealNameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("realName", String.class);
        } catch (Exception e) {
            log.error("从令牌中获取真实姓名失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取令牌类型
     */
    public String getTokenTypeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("tokenType", String.class);
        } catch (Exception e) {
            log.error("从令牌中获取令牌类型失败", e);
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证令牌失败", e);
            return false;
        }
    }

    /**
     * 验证令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("检查令牌是否过期失败", e);
            return true;
        }
    }

    /**
     * 解析令牌
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中提取Bearer Token
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
