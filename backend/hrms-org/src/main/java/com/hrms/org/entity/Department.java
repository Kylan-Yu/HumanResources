package com.hrms.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 部门实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 父部门ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 部门编码
     */
    @TableField("dept_code")
    private String deptCode;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 部门类型：company-部门，hospital-科室
     */
    @TableField("dept_type")
    private String deptType;

    /**
     * 负责人ID
     */
    @TableField("leader_id")
    private Long leaderId;

    /**
     * 排序
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField("ext_json")
    private String extJson;

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

    /**
     * 子部门列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<Department> children;
}
