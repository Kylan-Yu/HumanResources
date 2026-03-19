package com.hrms.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 岗位实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_position")
public class Position {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属组织ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 所属部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位编码
     */
    @TableField("position_code")
    private String positionCode;

    /**
     * 岗位名称
     */
    @TableField("position_name")
    private String positionName;

    /**
     * 岗位类型：company-岗位，hospital-医护岗位
     */
    @TableField("position_type")
    private String positionType;

    /**
     * 岗位级别
     */
    @TableField("position_level")
    private String positionLevel;

    /**
     * 行业类型：company-企业，hospital-医院
     */
    @TableField("industry_type")
    private String industryType;

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
}
