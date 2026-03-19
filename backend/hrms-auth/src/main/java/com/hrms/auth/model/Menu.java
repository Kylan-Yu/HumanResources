package com.hrms.auth.model;

import lombok.Data;

import java.util.List;

/**
 * 菜单实体
 *
 * @author HRMS
 */
@Data
public class Menu {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    private Integer menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 子菜单列表
     */
    private List<Menu> children;
}
