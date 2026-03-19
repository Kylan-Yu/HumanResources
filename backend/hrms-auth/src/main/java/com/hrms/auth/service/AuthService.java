package com.hrms.auth.service;

import com.hrms.auth.dto.LoginRequest;
import com.hrms.auth.vo.LoginResponse;

/**
 * 认证服务接口
 *
 * @author HRMS
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 刷新Token
     *
     * @param token 刷新令牌
     * @return 登录响应
     */
    LoginResponse refresh(String token);

    /**
     * 获取用户信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    LoginResponse getUserInfo(String token);
}
