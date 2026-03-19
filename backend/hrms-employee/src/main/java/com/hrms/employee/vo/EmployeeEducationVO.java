package com.hrms.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工教育经历VO
 *
 * @author HRMS
 */
@Data
public class EmployeeEducationVO {

    /**
     * 教育经历ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学历层次：primary-小学，middle-初中，high-高中，college-大专，bachelor-本科，master-硕士，doctor-博士
     */
    private String educationLevel;

    /**
     * 学历层次描述
     */
    private String educationLevelDesc;

    /**
     * 专业
     */
    private String major;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 是否最高学历：1-是，0-否
     */
    private Integer isHighest;

    /**
     * 是否最高学历描述
     */
    private String isHighestDesc;

    /**
     * 学位类型：bachelor-学士，master-硕士，doctor-博士
     */
    private String degreeType;

    /**
     * 学位类型描述
     */
    private String degreeTypeDesc;

    /**
     * 毕业证书编号
     */
    private String graduationCertificate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
