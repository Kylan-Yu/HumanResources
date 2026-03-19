package com.hrms.recruit.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 招聘需求VO
 *
 * @author HRMS
 */
@Data
public class RecruitRequirementVO {

    /**
     * 需求ID
     */
    private Long id;

    /**
     * 需求编号
     */
    private String requirementNo;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 招聘人数
     */
    private Integer headcount;

    /**
     * 紧急程度
     */
    private String urgencyLevel;

    /**
     * 紧急程度描述
     */
    private String urgencyLevelDesc;

    /**
     * 需求状态
     */
    private String requirementStatus;

    /**
     * 需求状态描述
     */
    private String requirementStatusDesc;

    /**
     * 期望入职日期
     */
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

    /**
     * 招聘职位列表
     */
    private List<RecruitPositionVO> positions;
}
