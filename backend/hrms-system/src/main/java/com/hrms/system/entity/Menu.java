package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 菜单实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_menu")
public class Menu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_code")
    private String menuCode;

    @TableField("menu_type")
    private Integer menuType;

    @TableField("icon")
    private String icon;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("permission")
    private String permission;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField("is_external")
    private Integer isExternal;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
