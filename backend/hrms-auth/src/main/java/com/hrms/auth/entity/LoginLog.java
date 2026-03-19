package com.hrms.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_login_log")
public class LoginLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录IP地址
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登录状态：0-成功，1-失败
     */
    @TableField("login_status")
    private Integer loginStatus;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;
}
