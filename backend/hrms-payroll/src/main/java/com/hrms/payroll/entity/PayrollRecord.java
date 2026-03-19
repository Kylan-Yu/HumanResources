package com.hrms.payroll.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪资记录实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_payroll_record")
public class PayrollRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 薪资单号
     */
    @TableField("payroll_no")
    private String payrollNo;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 薪资期间
     */
    @TableField("payroll_period")
    private String payrollPeriod;

    /**
     * 薪资期间开始日期
     */
    @TableField("period_start_date")
    private LocalDate periodStartDate;

    /**
     * 薪资期间结束日期
     */
    @TableField("period_end_date")
    private LocalDate periodEndDate;

    /**
     * 发放日期
     */
    @TableField("pay_date")
    private LocalDate payDate;

    /**
     * 基本薪资
     */
    @TableField("base_salary")
    private BigDecimal baseSalary;

    /**
     * 绩效薪资
     */
    @TableField("performance_salary")
    private BigDecimal performanceSalary;

    /**
     * 岗位津贴
     */
    @TableField("position_allowance")
    private BigDecimal positionAllowance;

    /**
     * 餐补
     */
    @TableField("meal_allowance")
    private BigDecimal mealAllowance;

    /**
     * 交通补贴
     */
    @TableField("transport_allowance")
    private BigDecimal transportAllowance;

    /**
     * 通讯补贴
     */
    @TableField("communication_allowance")
    private BigDecimal communicationAllowance;

    /**
     * 住房补贴
     */
    @TableField("housing_allowance")
    private BigDecimal housingAllowance;

    /**
     * 其他补贴
     */
    @TableField("other_allowance")
    private BigDecimal otherAllowance;

    /**
     * 应发薪资
     */
    @TableField("gross_salary")
    private BigDecimal grossSalary;

    /**
     * 社保个人
     */
    @TableField("social_personal")
    private BigDecimal socialPersonal;

    /**
     * 公积金个人
     */
    @TableField("fund_personal")
    private BigDecimal fundPersonal;

    /**
     * 个税
     */
    @TableField("income_tax")
    private BigDecimal incomeTax;

    /**
     * 其他扣款
     */
    @TableField("other_deduction")
    private BigDecimal otherDeduction;

    /**
     * 实发薪资
     */
    @TableField("net_salary")
    private BigDecimal netSalary;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 扩展字段
     */
    @TableField("ext_json")
    private String extJson;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
