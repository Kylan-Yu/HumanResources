package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工家庭成员VO
 *
 * @author HRMS
 */
@Data
public class EmployeeFamilyVO {

    /**
     * 家庭成员ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 关系：father-父亲，mother-母亲，spouse-配偶，child-子女
     */
    private String relationship;

    /**
     * 关系描述
     */
    private String relationshipDesc;

    /**
     * 性别：1-男，2-女
     */
    private Integer gender;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 工作单位
     */
    private String workUnit;

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
