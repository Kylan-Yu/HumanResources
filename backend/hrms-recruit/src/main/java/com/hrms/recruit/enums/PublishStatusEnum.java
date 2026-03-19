package com.hrms.recruit.enums;

/**
 * 发布状态枚举
 *
 * @author HRMS
 */
public enum PublishStatusEnum {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 已发布
     */
    PUBLISHED("PUBLISHED", "已发布"),
    
    /**
     * 下线
     */
    OFFLINE("OFFLINE", "下线");
    
    private final String code;
    private final String description;
    
    PublishStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PublishStatusEnum fromCode(String code) {
        for (PublishStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown publish status code: " + code);
    }
}
