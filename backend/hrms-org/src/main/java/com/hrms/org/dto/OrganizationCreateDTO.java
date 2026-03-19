package com.hrms.org.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 组织创建DTO
 *
 * @author HRMS
 */
@Data
public class OrganizationCreateDTO {

    /**
     * 组织编码
     */
    @NotBlank(message = "组织编码不能为空")
    @Size(max = 50, message = "组织编码长度不能超过50个字符")
    private String orgCode;

    /**
     * 组织名称
     */
    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100, message = "组织名称长度不能超过100个字符")
    private String orgName;

    /**
     * 组织类型：company-公司，hospital-医院
     */
    @NotBlank(message = "组织类型不能为空")
    private String orgType;

    /**
     * 父组织ID
     */
    private Long parentId;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    /**
     * 负责人ID
     */
    private Long leaderId;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /**
     * 地址
     */
    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;

    /**
     * 扩展字段（JSON格式）
     */
    private String extJson;
}
