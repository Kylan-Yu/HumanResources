package com.hrms.payroll.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资标准VO
 *
 * @author HRMS
 */
@Data
public class PayrollStandardVO {

    /**
     * 标准ID
     */
    private Long id;

    /**
     * 标准名称
     */
    private String standardName;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 职级
     */
    private String gradeLevel;

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
     * 总薪资
     */
    private BigDecimal totalSalary;

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
