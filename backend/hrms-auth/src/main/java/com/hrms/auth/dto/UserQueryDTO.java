package com.hrms.auth.dto;

import lombok.Data;

/**
 * 用户查询DTO
 *
 * @author HRMS
 */
@Data
public class UserQueryDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
