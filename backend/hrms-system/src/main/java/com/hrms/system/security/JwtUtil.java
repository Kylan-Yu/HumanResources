package com.hrms.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT parser for system service.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:hrms-jwt-secret-key-2024-hrms-system-secure-key-for-hs512-algorithm-requirement-abcdefg1234567890}")
    private String secret;

    public String extractToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(userId));
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    public boolean validate(String token, String username) {
        return StringUtils.hasText(token)
                && StringUtils.hasText(username)
                && username.equals(getUsername(token))
                && !isExpired(token);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

