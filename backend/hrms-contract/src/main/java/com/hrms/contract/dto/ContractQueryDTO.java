package com.hrms.contract.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 合同查询DTO
 *
 * @author HRMS
 */
@Data
public class ContractQueryDTO {

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
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同类型
     */
    private String contractType;

    /**
     * 合同状态
     */
    private String contractStatus;

    /**
     * 开始日期-查询范围
     */
    private LocalDate startDateBegin;

    /**
     * 开始日期-查询范围
     */
    private LocalDate startDateEnd;

    /**
     * 结束日期-查询范围
     */
    private LocalDate endDateBegin;

    /**
     * 结束日期-查询范围
     */
    private LocalDate endDateEnd;

    /**
     * 行业类型
     */
    private String industryType;
}
