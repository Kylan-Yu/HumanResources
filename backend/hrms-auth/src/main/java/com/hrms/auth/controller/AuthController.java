package com.hrms.auth.controller;

import com.hrms.auth.dto.LoginRequest;
import com.hrms.auth.service.AuthService;
import com.hrms.auth.vo.LoginResponse;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 */
@Tag(name = "Auth", description = "Authentication related APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login", description = "Login with username and password")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "User logout", description = "Logout current user")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    @Operation(summary = "Refresh token", description = "Refresh access token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader("Authorization") String token) {
        return Result.success(authService.refresh(token));
    }

    @Operation(summary = "Get user info", description = "Get current login user info")
    @GetMapping("/user-info")
    public Result<LoginResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        return Result.success(authService.getUserInfo(token));
    }
}
