package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工家庭成员实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_family")
public class EmployeeFamily {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 关系：father-父亲，mother-母亲，spouse-配偶，child-子女
     */
    @TableField("relationship")
    private String relationship;

    /**
     * 性别：1-男，2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 身份证号
     */
    @TableField("id_card_no")
    private String idCardNo;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 职业
     */
    @TableField("occupation")
    private String occupation;

    /**
     * 工作单位
     */
    @TableField("work_unit")
    private String workUnit;

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
