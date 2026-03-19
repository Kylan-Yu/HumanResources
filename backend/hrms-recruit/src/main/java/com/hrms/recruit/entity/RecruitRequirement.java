package com.hrms.recruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招聘需求实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_recruit_requirement")
public class RecruitRequirement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 需求编号
     */
    @TableField("requirement_no")
    private String requirementNo;

    /**
     * 需求标题
     */
    @TableField("title")
    private String title;

    /**
     * 组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 招聘人数
     */
    @TableField("headcount")
    private Integer headcount;

    /**
     * 紧急程度
     */
    @TableField("urgency_level")
    private String urgencyLevel;

    /**
     * 需求状态
     */
    @TableField("requirement_status")
    private String requirementStatus;

    /**
     * 期望入职日期
     */
    @TableField("expected_entry_date")
    private LocalDate expectedEntryDate;

    /**
     * 招聘原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 扩展字段
     */
    @TableField("ext_json")
    private String extJson;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
