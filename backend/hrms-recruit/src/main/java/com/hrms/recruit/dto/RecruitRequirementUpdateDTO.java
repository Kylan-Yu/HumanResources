package com.hrms.recruit.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

/**
 * 招聘需求更新DTO
 *
 * @author HRMS
 */
@Data
public class RecruitRequirementUpdateDTO {

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
     * 招聘人数
     */
    @Min(value = 1, message = "招聘人数必须大于0")
    private Integer headcount;

    /**
     * 紧急程度
     */
    private String urgencyLevel;

    /**
     * 期望入职日期
     */
    @FutureOrPresent(message = "期望入职日期不能是过去日期")
    private LocalDate expectedEntryDate;

    /**
     * 招聘原因
     */
    private String reason;

    /**
     * 行业类型
     */
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
