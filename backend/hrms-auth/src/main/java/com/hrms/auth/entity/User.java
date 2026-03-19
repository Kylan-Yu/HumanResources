package com.hrms.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 所属组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 所属部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
