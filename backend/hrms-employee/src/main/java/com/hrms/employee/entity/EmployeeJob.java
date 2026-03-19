package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工任职信息实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_job")
public class EmployeeJob {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 所属组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 所属部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 职级ID
     */
    @TableField("rank_id")
    private Long rankId;

    /**
     * 直属领导ID
     */
    @TableField("leader_id")
    private Long leaderId;

    /**
     * 员工类型：formal-正式工，contract-合同工，intern-实习生
     */
    @TableField("employee_type")
    private String employeeType;

    /**
     * 用工类型：fulltime-全职，parttime-兼职
     */
    @TableField("employment_type")
    private String employmentType;

    /**
     * 入职日期
     */
    @TableField("entry_date")
    private LocalDate entryDate;

    /**
     * 转正日期
     */
    @TableField("regular_date")
    private LocalDate regularDate;

    /**
     * 工作地点
     */
    @TableField("work_location")
    private String workLocation;

    /**
     * 是否主任职：1-是，0-否
     */
    @TableField("is_main_job")
    private Integer isMainJob;

    /**
     * 状态：1-在职，2-离职
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
}
