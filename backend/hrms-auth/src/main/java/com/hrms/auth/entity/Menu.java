package com.hrms.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_menu")
public class Menu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 路由地址
     */
    @TableField("route_path")
    private String routePath;

    /**
     * 组件路径
     */
    @TableField("component_path")
    private String componentPath;

    /**
     * 权限标识
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 是否显示：0-隐藏，1-显示
     */
    @TableField("visible")
    private Integer visible;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

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

    /**
     * 子菜单列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Menu> children;
}
