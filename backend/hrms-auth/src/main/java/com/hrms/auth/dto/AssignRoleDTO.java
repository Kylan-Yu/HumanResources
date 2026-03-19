package com.hrms.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 分配角色DTO
 *
 * @author HRMS
 */
@Data
public class AssignRoleDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
