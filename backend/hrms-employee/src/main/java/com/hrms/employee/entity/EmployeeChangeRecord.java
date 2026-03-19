package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工异动记录实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_change_record")
public class EmployeeChangeRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 异动类型：entry-入职，transfer-调动，promotion-晋升，demotion-降职，resign-离职，retire-退休
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 异动日期
     */
    @TableField("change_date")
    private LocalDate changeDate;

    /**
     * 变更前值
     */
    @TableField("before_value")
    private String beforeValue;

    /**
     * 变更后值
     */
    @TableField("after_value")
    private String afterValue;

    /**
     * 异动原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

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
