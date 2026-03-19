package com.hrms.employee.dto;

import lombok.Data;

/**
 * 员工查询DTO
 *
 * @author HRMS
 */
@Data
public class EmployeeQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 员工状态
     */
    private Integer employeeStatus;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 行业类型
     */
    private String industryType;
}
