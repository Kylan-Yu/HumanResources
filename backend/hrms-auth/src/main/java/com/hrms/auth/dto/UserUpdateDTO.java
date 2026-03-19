package com.hrms.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户更新DTO
 *
 * @author HRMS
 */
@Data
public class UserUpdateDTO {

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
