package com.hrms.contract.enums;

/**
 * 合同类型枚举
 *
 * @author HRMS
 */
public enum ContractTypeEnum {
    
    /**
     * 劳动合同
     */
    LABOR_CONTRACT("LABOR_CONTRACT", "劳动合同"),
    
    /**
     * 保密协议
     */
    CONFIDENTIALITY_AGREEMENT("CONFIDENTIALITY_AGREEMENT", "保密协议"),
    
    /**
     * 竞业协议
     */
    NON_COMPETE_AGREEMENT("NON_COMPETE_AGREEMENT", "竞业协议"),
    
    /**
     * 劳务协议
     */
    SERVICE_AGREEMENT("SERVICE_AGREEMENT", "劳务协议"),
    
    /**
     * 返聘协议
     */
    REEMPLOYMENT_AGREEMENT("REEMPLOYMENT_AGREEMENT", "返聘协议"),
    
    /**
     * 岗位聘任协议
     */
    POSITION_APPOINTMENT_AGREEMENT("POSITION_APPOINTMENT_AGREEMENT", "岗位聘任协议");
    
    private final String code;
    private final String description;
    
    ContractTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ContractTypeEnum fromCode(String code) {
        for (ContractTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown contract type code: " + code);
    }
}
