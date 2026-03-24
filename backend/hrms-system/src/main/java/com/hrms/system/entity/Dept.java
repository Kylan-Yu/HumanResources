package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_dept")
public class Dept {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_code")
    private String deptCode;

    @TableField("dept_name")
    private String deptName;

    @TableField("org_id")
    private Long orgId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("dept_type")
    private String deptType;

    @TableField("manager")
    private String manager;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("address")
    private String address;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("ext_json")
    private String extJson;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField(exist = false)
    private List<Dept> children;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
