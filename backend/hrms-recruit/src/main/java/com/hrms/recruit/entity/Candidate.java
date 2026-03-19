package com.hrms.recruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 候选人实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_candidate")
public class Candidate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 候选人编号
     */
    @TableField("candidate_no")
    private String candidateNo;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 简历地址
     */
    @TableField("resume_url")
    private String resumeUrl;

    /**
     * 来源渠道
     */
    @TableField("source_channel")
    private String sourceChannel;

    /**
     * 申请职位ID
     */
    @TableField("apply_position_id")
    private Long applyPositionId;

    /**
     * 候选人状态
     */
    @TableField("candidate_status")
    private String candidateStatus;

    /**
     * 当前公司
     */
    @TableField("current_company")
    private String currentCompany;

    /**
     * 当前职位
     */
    @TableField("current_position")
    private String currentPosition;

    /**
     * 期望薪资
     */
    @TableField("expected_salary")
    private Long expectedSalary;

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
