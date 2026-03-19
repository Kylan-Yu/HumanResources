package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工异动记录VO
 *
 * @author HRMS
 */
@Data
public class EmployeeChangeRecordVO {

    /**
     * 异动记录ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 异动类型：entry-入职，transfer-调动，promotion-晋升，demotion-降职，resign-离职，retire-退休
     */
    private String changeType;

    /**
     * 异动类型描述
     */
    private String changeTypeDesc;

    /**
     * 异动日期
     */
    private LocalDate changeDate;

    /**
     * 变更前值
     */
    private String beforeValue;

    /**
     * 变更后值
     */
    private String afterValue;

    /**
     * 异动原因
     */
    private String changeReason;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
