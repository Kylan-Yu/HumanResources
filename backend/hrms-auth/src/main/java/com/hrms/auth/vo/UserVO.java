package com.hrms.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户VO
 *
 * @author HRMS
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
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
     * 所属组织名称
     */
    private String orgName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;

    /**
     * 权限标识列表
     */
    private List<String> permissions;
}
