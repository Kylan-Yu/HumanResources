package com.hrms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行业类型枚举
 *
 * @author HRMS
 */
@Getter
@AllArgsConstructor
public enum IndustryType {

    /**
     * 企业
     */
    COMPANY("company", "企业"),

    /**
     * 医院
     */
    HOSPITAL("hospital", "医院");

    /**
     * 编码
     */
    private final String code;

    /**
     * 名称
     */
    private final String name;

    /**
     * 根据编码获取枚举
     */
    public static IndustryType getByCode(String code) {
        for (IndustryType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return COMPANY; // 默认返回企业
    }

    /**
     * 根据编码获取名称
     */
    public static String getNameByCode(String code) {
        IndustryType type = getByCode(code);
        return type != null ? type.getName() : null;
    }
}
