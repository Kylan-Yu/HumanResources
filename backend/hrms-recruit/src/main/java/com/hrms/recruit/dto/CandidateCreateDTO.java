package com.hrms.recruit.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 候选人创建DTO
 *
 * @author HRMS
 */
@Data
public class CandidateCreateDTO {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 性别
     */
    @NotBlank(message = "性别不能为空")
    private String gender;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 简历地址
     */
    private String resumeUrl;

    /**
     * 来源渠道
     */
    @NotBlank(message = "来源渠道不能为空")
    private String sourceChannel;

    /**
     * 申请职位ID
     */
    @NotNull(message = "申请职位ID不能为空")
    private Long applyPositionId;

    /**
     * 当前公司
     */
    private String currentCompany;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 期望薪资
     */
    private Long expectedSalary;

    /**
     * 行业类型
     */
    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    /**
     * 扩展字段
     */
    private String extJson;

    /**
     * 备注
     */
    private String remark;
}
