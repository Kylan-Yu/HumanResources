package com.hrms.employee.enums;

/**
 * 员工状态枚举
 *
 * @author HRMS
 */
public enum EmployeeStatusEnum {
    IN_SERVICE(1, "在职"),
    RESIGNED(2, "离职"),
    RETIRED(3, "退休");

    private final Integer code;
    private final String desc;

    EmployeeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EmployeeStatusEnum getByCode(Integer code) {
        for (EmployeeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
