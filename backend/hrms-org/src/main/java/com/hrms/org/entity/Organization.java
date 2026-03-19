package com.hrms.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 组织实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_org")
public class Organization {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组织编码
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 组织名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 组织类型：company-公司，hospital-医院
     */
    @TableField("org_type")
    private String orgType;

    /**
     * 父组织ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 负责人ID
     */
    @TableField("leader_id")
    private Long leaderId;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField("ext_json")
    private String extJson;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 子组织列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<Organization> children;
}
