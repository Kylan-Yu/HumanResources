package com.hrms.payroll.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 薪资标准查询DTO
 *
 * @author HRMS
 */
@Data
public class PayrollStandardQueryDTO {

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    /**
     * 标准名称
     */
    private String standardName;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 职级
     */
    private String gradeLevel;

    /**
     * 状态
     */
    private String status;

    /**
     * 行业类型
     */
    private String industryType;
}
