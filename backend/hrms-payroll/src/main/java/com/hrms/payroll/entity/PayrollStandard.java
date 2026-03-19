package com.hrms.payroll.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资标准实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_payroll_standard")
public class PayrollStandard {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准名称
     */
    @TableField("standard_name")
    private String standardName;

    /**
     * 组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 职级
     */
    @TableField("grade_level")
    private String gradeLevel;

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
