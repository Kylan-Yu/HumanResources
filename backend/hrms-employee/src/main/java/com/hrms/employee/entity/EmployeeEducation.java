package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工教育经历实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee_education")
public class EmployeeEducation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 学校名称
     */
    @TableField("school_name")
    private String schoolName;

    /**
     * 学历层次：primary-小学，middle-初中，high-高中，college-大专，bachelor-本科，master-硕士，doctor-博士
     */
    @TableField("education_level")
    private String educationLevel;

    /**
     * 专业
     */
    @TableField("major")
    private String major;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 是否最高学历：1-是，0-否
     */
    @TableField("is_highest")
    private Integer isHighest;

    /**
     * 学位类型：bachelor-学士，master-硕士，doctor-博士
     */
    @TableField("degree_type")
    private String degreeType;

    /**
     * 毕业证书编号
     */
    @TableField("graduation_certificate")
    private String graduationCertificate;

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
