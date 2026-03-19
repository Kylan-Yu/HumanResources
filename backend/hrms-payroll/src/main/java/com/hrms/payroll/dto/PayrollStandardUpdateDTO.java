package com.hrms.payroll.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 薪资标准更新DTO
 *
 * @author HRMS
 */
@Data
public class PayrollStandardUpdateDTO {

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
     * 基本薪资
     */
    @DecimalMin(value = "0.0", message = "基本薪资不能为负数")
    private BigDecimal baseSalary;

    /**
     * 绩效薪资
     */
    @DecimalMin(value = "0.0", message = "绩效薪资不能为负数")
    private BigDecimal performanceSalary;

    /**
     * 岗位津贴
     */
    @DecimalMin(value = "0.0", message = "岗位津贴不能为负数")
    private BigDecimal positionAllowance;

    /**
     * 餐补
     */
    @DecimalMin(value = "0.0", message = "餐补不能为负数")
    private BigDecimal mealAllowance;

    /**
     * 交通补贴
     */
    @DecimalMin(value = "0.0", message = "交通补贴不能为负数")
    private BigDecimal transportAllowance;

    /**
     * 通讯补贴
     */
    @DecimalMin(value = "0.0", message = "通讯补贴不能为负数")
    private BigDecimal communicationAllowance;

    /**
     * 住房补贴
     */
    @DecimalMin(value = "0.0", message = "住房补贴不能为负数")
    private BigDecimal housingAllowance;

    /**
     * 其他补贴
     */
    @DecimalMin(value = "0.0", message = "其他补贴不能为负数")
    private BigDecimal otherAllowance;

    /**
     * 状态
     */
    private String status;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 扩展字段
     */
    private String extJson;

    /**
     * 备注
     */
    private String remark;
}
