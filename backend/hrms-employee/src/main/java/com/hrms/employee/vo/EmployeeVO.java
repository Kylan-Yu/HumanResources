package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工VO
 *
 * @author HRMS
 */
@Data
public class EmployeeVO {

    /**
     * 员工ID
     */
    private Long id;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

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
     * 邮箱
     */
    private String email;

    /**
     * 婚姻状况：1-未婚，2-已婚，3-离异，4-丧偶
     */
    private Integer maritalStatus;

    /**
     * 婚姻状况描述
     */
    private String maritalStatusDesc;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 户籍地址
     */
    private String domicileAddress;

    /**
     * 现住址
     */
    private String currentAddress;

    /**
     * 员工状态：1-在职，2-离职，3-退休
     */
    private Integer employeeStatus;

    /**
     * 员工状态描述
     */
    private String employeeStatusDesc;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    private String industryType;

    /**
     * 行业类型描述
     */
    private String industryTypeDesc;

    /**
     * 扩展字段（JSON格式）
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

    /**
     * 任职信息
     */
    private EmployeeJobVO mainJob;

    /**
     * 所有任职信息
     */
    private List<EmployeeJobVO> jobs;

    /**
     * 家庭成员
     */
    private List<EmployeeFamilyVO> families;

    /**
     * 教育经历
     */
    private List<EmployeeEducationVO> educations;

    /**
     * 工作经历
     */
    private List<EmployeeWorkExperienceVO> workExperiences;

    /**
     * 附件
     */
    private List<EmployeeAttachmentVO> attachments;

    /**
     * 异动记录
     */
    private List<EmployeeChangeRecordVO> changeRecords;
}
