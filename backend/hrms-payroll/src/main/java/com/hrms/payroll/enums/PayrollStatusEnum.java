package com.hrms.payroll.enums;

/**
 * 薪资状态枚举
 *
 * @author HRMS
 */
public enum PayrollStatusEnum {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 计算中
     */
    CALCULATING("CALCULATING", "计算中"),
    
    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),
    
    /**
     * 已审核
     */
    APPROVED("APPROVED", "已审核"),
    
    /**
     * 已发放
     */
    PAID("PAID", "已发放"),
    
    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String description;
    
    PayrollStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PayrollStatusEnum fromCode(String code) {
        for (PayrollStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payroll status code: " + code);
    }
}
