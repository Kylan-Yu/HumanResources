package com.hrms.auth.service.impl;

import com.hrms.auth.dto.LoginRequest;
import com.hrms.auth.vo.LoginResponse;
import com.hrms.auth.mapper.UserMapper;
import com.hrms.auth.entity.User;
import com.hrms.auth.service.AuthService;
import com.hrms.auth.util.JwtUtil;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现类
 *
 * @author HRMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录：{}", request.getUsername());
        
        try {
            // 1. 验证用户名密码
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // 2. 获取用户信息
            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            
            // 3. 生成JWT Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRealName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 4. 查询用户权限和角色
            List<String> roles = userMapper.findRolesByUserId(user.getId());
            List<String> permissions = userMapper.findPermissionsByUserId(user.getId());
            
            // 5. 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(7200L);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setEmail(user.getEmail());
            response.setMobile(user.getMobile());
            response.setAvatar(user.getAvatar());
            response.setRoles(roles);
            response.setPermissions(permissions);
            
            // 6. 更新最后登录信息
            updateLastLoginInfo(user.getId());
            
            log.info("用户 {} 登录成功", request.getUsername());
            return response;
            
        } catch (Exception e) {
            log.error("用户 {} 登录失败", request.getUsername(), e);
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }
    }

    @Override
    public void logout(String token) {
        log.info("用户登出");
        
        try {
            // 从token中获取用户名
            String username = jwtUtil.getUsernameFromToken(jwtUtil.extractToken(token));
            
            // 清除Security上下文
            SecurityContextHolder.clearContext();
            
            // TODO: 将Token加入黑名单（Redis）
            // TODO: 清理用户缓存
            
            log.info("用户 {} 登出成功", username);
            
        } catch (Exception e) {
            log.error("登出失败", e);
        }
    }

    @Override
    public LoginResponse refresh(String token) {
        log.info("刷新Token");
        
        try {
            String cleanToken = jwtUtil.extractToken(token);
            
            // 验证刷新令牌
            if (!jwtUtil.isTokenExpired(cleanToken)) {
                String tokenType = jwtUtil.getTokenTypeFromToken(cleanToken);
                if (!"refresh".equals(tokenType)) {
                    throw new BusinessException(ResultCode.TOKEN_INVALID);
                }
                
                // 获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(cleanToken);
                String username = jwtUtil.getUsernameFromToken(cleanToken);
                User user = userMapper.findByUsername(username);
                
                if (user == null) {
                    throw new BusinessException(ResultCode.USER_NOT_FOUND);
                }
                
                // 生成新的访问令牌
                String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRealName());
                String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
                
                // 查询用户权限和角色
                List<String> roles = userMapper.findRolesByUserId(user.getId());
                List<String> permissions = userMapper.findPermissionsByUserId(user.getId());
                
                // 构建响应
                LoginResponse response = new LoginResponse();
                response.setAccessToken(newAccessToken);
                response.setRefreshToken(newRefreshToken);
                response.setExpiresIn(7200L);
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setRealName(user.getRealName());
                response.setEmail(user.getEmail());
                response.setMobile(user.getMobile());
                response.setAvatar(user.getAvatar());
                response.setRoles(roles);
                response.setPermissions(permissions);
                
                log.info("用户 {} Token刷新成功", username);
                return response;
            }
            
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
            
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new BusinessException(ResultCode.TOKEN_REFRESH_FAILED);
        }
    }

    @Override
    public LoginResponse getUserInfo(String token) {
        log.info("获取用户信息");
        
        try {
            String cleanToken = jwtUtil.extractToken(token);
            
            // 验证令牌
            if (!jwtUtil.isTokenExpired(cleanToken)) {
                String username = jwtUtil.getUsernameFromToken(cleanToken);
                User user = userMapper.findByUsername(username);
                
                if (user == null) {
                    throw new BusinessException(ResultCode.USER_NOT_FOUND);
                }
                
                // 查询用户权限和角色
                List<String> roles = userMapper.findRolesByUserId(user.getId());
                List<String> permissions = userMapper.findPermissionsByUserId(user.getId());
                
                // 构建响应
                LoginResponse response = new LoginResponse();
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setRealName(user.getRealName());
                response.setEmail(user.getEmail());
                response.setMobile(user.getMobile());
                response.setAvatar(user.getAvatar());
                response.setRoles(roles);
                response.setPermissions(permissions);
                
                log.info("获取用户 {} 信息成功", username);
                return response;
            }
            
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }
    
    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(Long userId) {
        // TODO: 实现更新最后登录时间和IP的逻辑
        log.debug("更新用户 {} 最后登录信息", userId);
    }
}
