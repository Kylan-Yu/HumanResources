package com.hrms.recruit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招聘职位VO
 *
 * @author HRMS
 */
@Data
public class RecruitPositionVO {

    /**
     * 职位ID
     */
    private Long id;

    /**
     * 需求ID
     */
    private Long requirementId;

    /**
     * 需求编号
     */
    private String requirementNo;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 职位描述
     */
    private String jobDescription;

    /**
     * 职位要求
     */
    private String jobRequirements;

    /**
     * 最低薪资
     */
    private Long salaryMin;

    /**
     * 最高薪资
     */
    private Long salaryMax;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 雇佣类型
     */
    private String employmentType;

    /**
     * 雇佣类型描述
     */
    private String employmentTypeDesc;

    /**
     * 发布状态
     */
    private String publishStatus;

    /**
     * 发布状态描述
     */
    private String publishStatusDesc;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 行业类型描述
     */
    private String industryTypeDesc;

    /**
     * 扩展字段
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
}
