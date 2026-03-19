package com.hrms.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 成本中心实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_cost_center")
public class CostCenter {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 成本中心编码
     */
    @TableField("cost_center_code")
    private String costCenterCode;

    /**
     * 成本中心名称
     */
    @TableField("cost_center_name")
    private String costCenterName;

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
     * 负责人ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

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
