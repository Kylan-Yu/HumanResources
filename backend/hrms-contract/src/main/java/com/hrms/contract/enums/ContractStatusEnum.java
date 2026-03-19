package com.hrms.contract.enums;

/**
 * 合同状态枚举
 *
 * @author HRMS
 */
public enum ContractStatusEnum {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 生效
     */
    ACTIVE("ACTIVE", "生效"),
    
    /**
     * 即将到期
     */
    EXPIRING("EXPIRING", "即将到期"),
    
    /**
     * 已到期
     */
    EXPIRED("EXPIRED", "已到期"),
    
    /**
     * 终止
     */
    TERMINATED("TERMINATED", "终止");
    
    private final String code;
    private final String description;
    
    ContractStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ContractStatusEnum fromCode(String code) {
        for (ContractStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown contract status code: " + code);
    }
}
