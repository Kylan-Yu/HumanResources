package com.hrms.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_employee")
public class Employee {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工编号
     */
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

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
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 婚姻状况：1-未婚，2-已婚，3-离异，4-丧偶
     */
    @TableField("marital_status")
    private Integer maritalStatus;

    /**
     * 国籍
     */
    @TableField("nationality")
    private String nationality;

    /**
     * 户籍地址
     */
    @TableField("domicile_address")
    private String domicileAddress;

    /**
     * 现住址
     */
    @TableField("current_address")
    private String currentAddress;

    /**
     * 员工状态：1-在职，2-离职，3-退休
     */
    @TableField("employee_status")
    private Integer employeeStatus;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 扩展字段（JSON格式）
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
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
