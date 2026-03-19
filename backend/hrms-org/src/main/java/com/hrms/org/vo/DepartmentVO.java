package com.hrms.org.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门VO
 *
 * @author HRMS
 */
@Data
public class DepartmentVO {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 所属组织名称
     */
    private String orgName;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 父部门名称
     */
    private String parentName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门类型：company-部门，hospital-科室
     */
    private String deptType;

    /**
     * 负责人ID
     */
    private Long leaderId;

    /**
     * 负责人姓名
     */
    private String leaderName;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    private String industryType;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 子部门列表
     */
    private List<DepartmentVO> children;

    /**
     * 岗位数量
     */
    private Integer positionCount;

    /**
     * 员工数量
     */
    private Integer employeeCount;
}
