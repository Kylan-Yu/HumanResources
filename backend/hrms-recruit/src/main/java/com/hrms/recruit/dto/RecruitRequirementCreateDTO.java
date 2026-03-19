package com.hrms.recruit.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

/**
 * 招聘需求创建DTO
 *
 * @author HRMS
 */
@Data
public class RecruitRequirementCreateDTO {

    /**
     * 需求标题
     */
    @NotBlank(message = "需求标题不能为空")
    private String title;

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long positionId;

    /**
     * 招聘人数
     */
    @NotNull(message = "招聘人数不能为空")
    @Min(value = 1, message = "招聘人数必须大于0")
    private Integer headcount;

    /**
     * 紧急程度
     */
    @NotBlank(message = "紧急程度不能为空")
    private String urgencyLevel;

    /**
     * 期望入职日期
     */
    @NotNull(message = "期望入职日期不能为空")
    @FutureOrPresent(message = "期望入职日期不能是过去日期")
    private LocalDate expectedEntryDate;

    /**
     * 招聘原因
     */
    @NotBlank(message = "招聘原因不能为空")
    private String reason;

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
