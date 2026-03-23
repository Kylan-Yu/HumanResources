package com.hrms.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 *
 * @author HRMS
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "1234")
    private String captcha;

    /**
     * 验证码Key
     */
    @Schema(description = "验证码Key", example = "captcha_key")
    private String captchaKey;
}
