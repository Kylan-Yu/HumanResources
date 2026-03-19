package com.hrms.recruit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人面试VO
 *
 * @author HRMS
 */
@Data
public class CandidateInterviewVO {

    /**
     * 面试ID
     */
    private Long id;

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 面试轮次
     */
    private Integer interviewRound;

    /**
     * 面试轮次描述
     */
    private String interviewRoundDesc;

    /**
     * 面试官ID
     */
    private Long interviewerId;

    /**
     * 面试官姓名
     */
    private String interviewerName;

    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;

    /**
     * 面试类型
     */
    private String interviewType;

    /**
     * 面试类型描述
     */
    private String interviewTypeDesc;

    /**
     * 面试评分
     */
    private Integer score;

    /**
     * 面试结果
     */
    private String result;

    /**
     * 面试结果描述
     */
    private String resultDesc;

    /**
     * 面试反馈
     */
    private String feedback;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
