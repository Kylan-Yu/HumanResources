package com.hrms.org.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织VO
 *
 * @author HRMS
 */
@Data
public class OrganizationVO {

    /**
     * 组织ID
     */
    private Long id;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织类型：company-公司，hospital-医院
     */
    private String orgType;

    /**
     * 父组织ID
     */
    private Long parentId;

    /**
     * 父组织名称
     */
    private String parentName;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    private String industryType;

    /**
     * 负责人ID
     */
    private Long leaderId;

    /**
     * 负责人姓名
     */
    private String leaderName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 子组织列表
     */
    private List<OrganizationVO> children;

    /**
     * 部门数量
     */
    private Integer deptCount;

    /**
     * 员工数量
     */
    private Integer employeeCount;
}
