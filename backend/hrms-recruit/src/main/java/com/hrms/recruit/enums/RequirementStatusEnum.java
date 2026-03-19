package com.hrms.recruit.enums;

/**
 * 招聘需求状态枚举
 *
 * @author HRMS
 */
public enum RequirementStatusEnum {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 开放
     */
    OPEN("OPEN", "开放"),
    
    /**
     * 关闭
     */
    CLOSED("CLOSED", "关闭"),
    
    /**
     * 取消
     */
    CANCELLED("CANCELLED", "取消");
    
    private final String code;
    private final String description;
    
    RequirementStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static RequirementStatusEnum fromCode(String code) {
        for (RequirementStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown requirement status code: " + code);
    }
}
