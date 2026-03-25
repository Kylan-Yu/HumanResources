package com.hrms.auth.controller;

import com.hrms.auth.dto.LoginRequest;
import com.hrms.auth.vo.LoginResponse;
import com.hrms.auth.service.AuthService;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 *
 * @author HRMS
 */
@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(">>> AuthController.login reached");
        System.out.println("请求用户名: " + request.getUsername());
        System.out.println("请求时间: " + new java.util.Date());
        LoginResponse response = authService.login(request);
        System.out.println("登录处理完成，返回结果: " + (response != null ? "成功" : "失败"));
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token", description = "刷新访问令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader("Authorization") String token) {
        LoginResponse response = authService.refresh(token);
        return Result.success(response);
    }

    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @GetMapping("/user-info")
    public Result<LoginResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        LoginResponse response = authService.getUserInfo(token);
        return Result.success(response);
    }
}
