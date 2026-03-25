package com.hrms.auth.filter;

import com.hrms.auth.util.JwtUtil;
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
 * JWT认证过滤器
 *
 * @author HRMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 打印请求信息
        System.out.println("=== JWT Filter Debug ===");
        System.out.println("requestURI = " + request.getRequestURI());
        System.out.println("servletPath = " + request.getServletPath());
        System.out.println("contextPath = " + request.getContextPath());
        System.out.println("method = " + request.getMethod());

        // 获取请求路径
        String requestURI = request.getRequestURI();
        String servletPath = request.getServletPath();
        
        System.out.println("即将判断 - requestURI: " + requestURI);
        System.out.println("即将判断 - servletPath: " + servletPath);
        
        // 登录接口直接放行，不需要JWT验证
        boolean isLoginPath = (requestURI != null && (requestURI.equals("/auth/login") || requestURI.contains("/auth/login"))) ||
                             (servletPath != null && (servletPath.equals("/auth/login") || servletPath.contains("/auth/login")));
        
        System.out.println("isLoginPath = " + isLoginPath);
        
        if (isLoginPath) {
            System.out.println(">>> JWT filter bypass login");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(">>> JWT filter continues validation");

        try {
            // 从请求头中获取JWT令牌
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 验证令牌
                String username = jwtUtil.getUsernameFromToken(jwt);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 验证令牌
                    if (jwtUtil.validateToken(jwt, username)) {
                        // 创建认证令牌
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置认证信息
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("用户 {} 认证成功", username);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
