package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 岗位实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_position")
public class Position {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("position_code")
    private String positionCode;

    @TableField("position_name")
    private String positionName;

    @TableField("org_id")
    private Long orgId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("position_category")
    private String positionCategory;

    @TableField("rank_grade")
    private String rankGrade;

    @TableField("rank_series")
    private String rankSeries;

    @TableField("job_description")
    private String jobDescription;

    @TableField("requirements")
    private String requirements;

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

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
