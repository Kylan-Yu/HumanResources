package com.hrms.recruit.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人VO
 *
 * @author HRMS
 */
@Data
public class CandidateVO {

    /**
     * 候选人ID
     */
    private Long id;

    /**
     * 候选人编号
     */
    private String candidateNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 简历地址
     */
    private String resumeUrl;

    /**
     * 来源渠道
     */
    private String sourceChannel;

    /**
     * 来源渠道描述
     */
    private String sourceChannelDesc;

    /**
     * 申请职位ID
     */
    private Long applyPositionId;

    /**
     * 申请职位名称
     */
    private String applyPositionName;

    /**
     * 候选人状态
     */
    private String candidateStatus;

    /**
     * 候选人状态描述
     */
    private String candidateStatusDesc;

    /**
     * 当前公司
     */
    private String currentCompany;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 期望薪资
     */
    private Long expectedSalary;

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
     * 面试记录列表
     */
    private List<CandidateInterviewVO> interviews;

    /**
     * Offer记录
     */
    private CandidateOfferVO offer;
}
