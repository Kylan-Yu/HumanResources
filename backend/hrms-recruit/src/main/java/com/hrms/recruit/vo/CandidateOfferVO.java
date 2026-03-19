package com.hrms.recruit.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 候选人OfferVO
 *
 * @author HRMS
 */
@Data
public class CandidateOfferVO {

    /**
     * OfferID
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
     * Offer编号
     */
    private String offerNo;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 薪资金额
     */
    private Long salaryAmount;

    /**
     * 入职日期
     */
    private LocalDate entryDate;

    /**
     * Offer状态
     */
    private String offerStatus;

    /**
     * Offer状态描述
     */
    private String offerStatusDesc;

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
