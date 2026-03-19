package com.hrms.payroll.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪资记录VO
 *
 * @author HRMS
 */
@Data
public class PayrollRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 薪资单号
     */
    private String payrollNo;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 薪资期间
     */
    private String payrollPeriod;

    /**
     * 薪资期间开始日期
     */
    private LocalDate periodStartDate;

    /**
     * 薪资期间结束日期
     */
    private LocalDate periodEndDate;

    /**
     * 发放日期
     */
    private LocalDate payDate;

    /**
     * 基本薪资
     */
    private BigDecimal baseSalary;

    /**
     * 绩效薪资
     */
    private BigDecimal performanceSalary;

    /**
     * 岗位津贴
     */
    private BigDecimal positionAllowance;

    /**
     * 餐补
     */
    private BigDecimal mealAllowance;

    /**
     * 交通补贴
     */
    private BigDecimal transportAllowance;

    /**
     * 通讯补贴
     */
    private BigDecimal communicationAllowance;

    /**
     * 住房补贴
     */
    private BigDecimal housingAllowance;

    /**
     * 其他补贴
     */
    private BigDecimal otherAllowance;

    /**
     * 应发薪资
     */
    private BigDecimal grossSalary;

    /**
     * 社保个人
     */
    private BigDecimal socialPersonal;

    /**
     * 公积金个人
     */
    private BigDecimal fundPersonal;

    /**
     * 个税
     */
    private BigDecimal incomeTax;

    /**
     * 其他扣款
     */
    private BigDecimal otherDeduction;

    /**
     * 总扣款
     */
    private BigDecimal totalDeduction;

    /**
     * 实发薪资
     */
    private BigDecimal netSalary;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 行业类型描述
     */
    private String industryTypeDesc;

    /**
     * 扩展字段
     */
    private String extJson;

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
