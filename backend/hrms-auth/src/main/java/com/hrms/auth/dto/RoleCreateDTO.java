package com.hrms.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色创建DTO
 *
 * @author HRMS
 */
@Data
public class RoleCreateDTO {

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在2-50个字符之间")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    private String roleName;

    /**
     * 数据权限：all-全部，org-本组织，dept-本部门，self-本人
     */
    private String dataScope;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
