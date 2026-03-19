package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工工作经历VO
 *
 * @author HRMS
 */
@Data
public class EmployeeWorkExperienceVO {

    /**
     * 工作经历ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 职位
     */
    private String position;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 工作描述
     */
    private String jobDescription;

    /**
     * 离职原因
     */
    private String resignReason;

    /**
     * 证明人
     */
    private String witness;

    /**
     * 证明人电话
     */
    private String witnessMobile;

    /**
     * 工作时长（月）
     */
    private Integer workMonths;

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
