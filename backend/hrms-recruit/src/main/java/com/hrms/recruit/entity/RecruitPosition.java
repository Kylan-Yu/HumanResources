package com.hrms.recruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 招聘职位实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_recruit_position")
public class RecruitPosition {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 需求ID
     */
    @TableField("requirement_id")
    private Long requirementId;

    /**
     * 职位名称
     */
    @TableField("position_name")
    private String positionName;

    /**
     * 职位描述
     */
    @TableField("job_description")
    private String jobDescription;

    /**
     * 职位要求
     */
    @TableField("job_requirements")
    private String jobRequirements;

    /**
     * 最低薪资
     */
    @TableField("salary_min")
    private Long salaryMin;

    /**
     * 最高薪资
     */
    @TableField("salary_max")
    private Long salaryMax;

    /**
     * 工作城市
     */
    @TableField("city")
    private String city;

    /**
     * 雇佣类型
     */
    @TableField("employment_type")
    private String employmentType;

    /**
     * 发布状态
     */
    @TableField("publish_status")
    private String publishStatus;

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
