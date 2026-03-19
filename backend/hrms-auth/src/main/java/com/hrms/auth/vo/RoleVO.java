package com.hrms.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author HRMS
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 数据权限：all-全部，org-本组织，dept-本部门，self-本人
     */
    private String dataScope;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

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
     * 菜单列表
     */
    private List<MenuVO> menus;

    /**
     * 用户数量
     */
    private Integer userCount;
}
