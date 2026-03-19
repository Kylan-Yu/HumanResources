package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工工作经历实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_work_experience")
public class EmployeeWorkExperience {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 公司名称
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 工作描述
     */
    @TableField("job_description")
    private String jobDescription;

    /**
     * 离职原因
     */
    @TableField("resign_reason")
    private String resignReason;

    /**
     * 证明人
     */
    @TableField("witness")
    private String witness;

    /**
     * 证明人电话
     */
    @TableField("witness_mobile")
    private String witnessMobile;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
