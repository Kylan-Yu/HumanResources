package com.hrms.employee.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 员工创建DTO
 *
 * @author HRMS
 */
@Data
public class EmployeeCreateDTO {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    /**
     * 性别：1-男，2-女
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    /**
     * 出生日期
     */
    @Past(message = "出生日期必须是过去的日期")
    private LocalDate birthday;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCardNo;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 婚姻状况：1-未婚，2-已婚，3-离异，4-丧偶
     */
    @Min(value = 1, message = "婚姻状况值不正确")
    @Max(value = 4, message = "婚姻状况值不正确")
    private Integer maritalStatus;

    /**
     * 国籍
     */
    @Size(max = 50, message = "国籍长度不能超过50个字符")
    private String nationality;

    /**
     * 户籍地址
     */
    @Size(max = 255, message = "户籍地址长度不能超过255个字符")
    private String domicileAddress;

    /**
     * 现住址
     */
    @Size(max = 255, message = "现住址长度不能超过255个字符")
    private String currentAddress;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    /**
     * 扩展字段（JSON格式）
     */
    private String extJson;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 任职信息
     */
    private EmployeeJobCreateDTO jobInfo;
}
