package com.hrms.org.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 部门创建DTO
 *
 * @author HRMS
 */
@Data
public class DepartmentCreateDTO {

    /**
     * 所属组织ID
     */
    @NotNull(message = "所属组织不能为空")
    private Long orgId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 部门编码
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String deptCode;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String deptName;

    /**
     * 部门类型：company-部门，hospital-科室
     */
    @NotBlank(message = "部门类型不能为空")
    private String deptType;

    /**
     * 负责人ID
     */
    private Long leaderId;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    /**
     * 扩展字段（JSON格式）
     */
    private String extJson;
}
