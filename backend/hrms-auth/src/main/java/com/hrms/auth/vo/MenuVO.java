package com.hrms.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单VO
 *
 * @author HRMS
 */
@Data
public class MenuVO {

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
    private String routePath;

    /**
     * 组件路径
     */
    private String componentPath;

    /**
     * 权限标识
     */
    private String permissionCode;

    /**
     * 菜单图标
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;
}
