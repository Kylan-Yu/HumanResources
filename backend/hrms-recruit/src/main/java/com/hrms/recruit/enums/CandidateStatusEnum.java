package com.hrms.recruit.enums;

/**
 * 候选人状态枚举
 *
 * @author HRMS
 */
public enum CandidateStatusEnum {
    
    /**
     * 新建
     */
    NEW("NEW", "新建"),
    
    /**
     * 筛选
     */
    SCREENING("SCREENING", "筛选"),
    
    /**
     * 面试中
     */
    INTERVIEWING("INTERVIEWING", "面试中"),
    
    /**
     * 待发Offer
     */
    OFFER_PENDING("OFFER_PENDING", "待发Offer"),
    
    /**
     * 已发Offer
     */
    OFFER_SENT("OFFER_SENT", "已发Offer"),
    
    /**
     * 已接受
     */
    ACCEPTED("ACCEPTED", "已接受"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),
    
    /**
     * 已入职
     */
    HIRED("HIRED", "已入职");
    
    private final String code;
    private final String description;
    
    CandidateStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CandidateStatusEnum fromCode(String code) {
        for (CandidateStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown candidate status code: " + code);
    }
}
