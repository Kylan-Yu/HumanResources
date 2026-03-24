package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_org")
public class Org {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("org_code")
    private String orgCode;

    @TableField("org_name")
    private String orgName;

    @TableField("org_type")
    private String orgType;

    @TableField("parent_id")
    private Long parentId;

    @TableField("legal_person")
    private String legalPerson;

    @TableField("unified_social_credit_code")
    private String unifiedSocialCreditCode;

    @TableField("address")
    private String address;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("industry_type")
    private String industryType;

    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("ext_json")
    private String extJson;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField(exist = false)
    private List<Org> children;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
