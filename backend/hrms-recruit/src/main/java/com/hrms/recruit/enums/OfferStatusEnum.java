package com.hrms.recruit.enums;

/**
 * Offer状态枚举
 *
 * @author HRMS
 */
public enum OfferStatusEnum {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 已发送
     */
    SENT("SENT", "已发送"),
    
    /**
     * 已接受
     */
    ACCEPTED("ACCEPTED", "已接受"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),
    
    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String description;
    
    OfferStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static OfferStatusEnum fromCode(String code) {
        for (OfferStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown offer status code: " + code);
    }
}
