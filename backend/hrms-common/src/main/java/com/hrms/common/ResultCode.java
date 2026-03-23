package com.hrms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回状态码枚举
 *
 * @author HRMS
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未登录
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "没有权限访问该资源"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 系统繁忙
     */
    SYSTEM_BUSY(429, "系统繁忙，请稍后再试"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务异常"),

    /**
     * 数据已存在
     */
    DATA_EXISTS(601, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(602, "数据不存在"),

    /**
     * 数据状态异常
     */
    DATA_STATUS_ERROR(603, "数据状态异常"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_ERROR(700, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(701, "文件类型不支持"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(702, "文件大小超过限制"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(800, "验证码错误"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(801, "验证码已过期"),

    /**
     * 账号或密码错误
     */
    USERNAME_PASSWORD_ERROR(802, "账号或密码错误"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(803, "账号已被禁用"),

    /**
     * 账号已被锁定
     */
    ACCOUNT_LOCKED(804, "账号已被锁定"),

    /**
     * Token无效
     */
    TOKEN_INVALID(805, "Token无效"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(806, "Token已过期"),

    /**
     * 用户名已存在
     */
    USERNAME_ALREADY_EXISTS(807, "用户名已存在"),

    /**
     * 手机号已存在
     */
    MOBILE_ALREADY_EXISTS(808, "手机号已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_ALREADY_EXISTS(809, "邮箱已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(810, "用户不存在"),

    /**
     * Token刷新失败
     */
    TOKEN_REFRESH_FAILED(811, "Token刷新失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
