package com.hrms.system.security;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT auth filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        System.out.println("[workflow-auth-fix] request uri: " + requestUri);
        
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("[workflow-auth-fix] authorization header exists: " + StringUtils.hasText(authHeader));
            
            String token = jwtUtil.extractToken(authHeader);
            if (StringUtils.hasText(token)) {
                String username = jwtUtil.getUsername(token);
                System.out.println("[workflow-auth-fix] parsed username: " + username);
                
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validate(token, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        System.out.println("[workflow-auth-fix] authentication success: token valid and user loaded");
                    } else {
                        System.out.println("[workflow-auth-fix] authentication fail: token validation failed");
                    }
                } else {
                    System.out.println("[workflow-auth-fix] authentication fail: username empty or already authenticated");
                }
            } else {
                System.out.println("[workflow-auth-fix] authentication fail: no token found");
            }
        } catch (Exception ex) {
            System.out.println("[workflow-auth-fix] authentication fail reason: " + ex.getMessage());
            log.warn("JWT authentication failed: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}

