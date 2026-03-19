package com.hrms.contract.enums;

/**
 * 合同记录类型枚举
 *
 * @author HRMS
 */
public enum ContractRecordTypeEnum {
    
    /**
     * 创建
     */
    CREATE("CREATE", "创建"),
    
    /**
     * 更新
     */
    UPDATE("UPDATE", "更新"),
    
    /**
     * 续签
     */
    RENEW("RENEW", "续签"),
    
    /**
     * 终止
     */
    TERMINATE("TERMINATE", "终止"),
    
    /**
     * 状态变更
     */
    STATUS_CHANGE("STATUS_CHANGE", "状态变更");
    
    private final String code;
    private final String description;
    
    ContractRecordTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ContractRecordTypeEnum fromCode(String code) {
        for (ContractRecordTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown contract record type code: " + code);
    }
}
