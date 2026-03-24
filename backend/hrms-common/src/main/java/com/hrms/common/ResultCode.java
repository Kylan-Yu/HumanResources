package com.hrms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),
    SYSTEM_BUSY(429, "系统繁忙，请稍后再试"),
    SYSTEM_ERROR(500, "系统错误"),

    BUSINESS_ERROR(600, "业务异常"),
    DATA_EXISTS(601, "数据已存在"),
    DATA_NOT_EXISTS(602, "数据不存在"),
    DATA_STATUS_ERROR(603, "数据状态异常"),

    FILE_UPLOAD_ERROR(700, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(701, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(702, "文件大小超过限制"),

    CAPTCHA_ERROR(800, "验证码错误"),
    CAPTCHA_EXPIRED(801, "验证码已过期"),
    USERNAME_PASSWORD_ERROR(802, "账号或密码错误"),
    ACCOUNT_DISABLED(803, "账号已被禁用"),
    ACCOUNT_LOCKED(804, "账号已被锁定"),
    TOKEN_INVALID(805, "Token无效"),
    TOKEN_EXPIRED(806, "Token已过期"),
    USERNAME_ALREADY_EXISTS(807, "用户名已存在"),
    MOBILE_ALREADY_EXISTS(808, "手机号已存在"),
    EMAIL_ALREADY_EXISTS(809, "邮箱已存在"),
    USER_NOT_FOUND(810, "用户不存在"),
    TOKEN_REFRESH_FAILED(811, "Token刷新失败"),

    ID_CARD_NO_ALREADY_EXISTS(820, "身份证号已存在"),
    EMPLOYEE_NOT_FOUND(821, "员工不存在"),

    ORG_CODE_ALREADY_EXISTS(830, "组织编码已存在"),
    ORG_NOT_FOUND(831, "组织不存在"),
    ORG_HAS_CHILDREN(832, "组织存在下级节点"),
    ORG_HAS_DEPARTMENTS(833, "组织下存在部门"),
    ORG_HAS_EMPLOYEES(834, "组织下存在员工");

    private final Integer code;
    private final String message;
}
