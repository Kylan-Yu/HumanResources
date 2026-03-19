package com.hrms.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 菜单创建DTO
 *
 * @author HRMS
 */
@Data
public class MenuCreateDTO {

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    /**
     * 路由地址
     */
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String routePath;

    /**
     * 组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String componentPath;

    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permissionCode;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
