package com.hrms.recruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 候选人面试实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_candidate_interview")
public class CandidateInterview {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 候选人ID
     */
    @TableField("candidate_id")
    private Long candidateId;

    /**
     * 面试轮次
     */
    @TableField("interview_round")
    private Integer interviewRound;

    /**
     * 面试官ID
     */
    @TableField("interviewer_id")
    private Long interviewerId;

    /**
     * 面试官姓名
     */
    @TableField("interviewer_name")
    private String interviewerName;

    /**
     * 面试时间
     */
    @TableField("interview_time")
    private LocalDateTime interviewTime;

    /**
     * 面试类型
     */
    @TableField("interview_type")
    private String interviewType;

    /**
     * 面试评分
     */
    @TableField("score")
    private Integer score;

    /**
     * 面试结果
     */
    @TableField("result")
    private String result;

    /**
     * 面试反馈
     */
    @TableField("feedback")
    private String feedback;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

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
