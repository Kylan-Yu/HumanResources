package com.hrms.recruit.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 招聘需求查询DTO
 *
 * @author HRMS
 */
@Data
public class RecruitRequirementQueryDTO {

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 需求状态
     */
    private String requirementStatus;

    /**
     * 紧急程度
     */
    private String urgencyLevel;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 期望入职日期-开始
     */
    private LocalDate expectedEntryDateBegin;

    /**
     * 期望入职日期-结束
     */
    private LocalDate expectedEntryDateEnd;
}
