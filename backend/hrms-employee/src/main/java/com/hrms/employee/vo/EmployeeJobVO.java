package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工任职信息VO
 *
 * @author HRMS
 */
@Data
public class EmployeeJobVO {

    /**
     * 任职ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 所属组织名称
     */
    private String orgName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 职级ID
     */
    private Long rankId;

    /**
     * 职级名称
     */
    private String rankName;

    /**
     * 直属领导ID
     */
    private Long leaderId;

    /**
     * 直属领导姓名
     */
    private String leaderName;

    /**
     * 员工类型：formal-正式工，contract-合同工，intern-实习生
     */
    private String employeeType;

    /**
     * 员工类型描述
     */
    private String employeeTypeDesc;

    /**
     * 用工类型：fulltime-全职，parttime-兼职
     */
    private String employmentType;

    /**
     * 用工类型描述
     */
    private String employmentTypeDesc;

    /**
     * 入职日期
     */
    private LocalDate entryDate;

    /**
     * 转正日期
     */
    private LocalDate regularDate;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 是否主任职：1-是，0-否
     */
    private Integer isMainJob;

    /**
     * 是否主任职描述
     */
    private String isMainJobDesc;

    /**
     * 状态：1-在职，2-离职
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
