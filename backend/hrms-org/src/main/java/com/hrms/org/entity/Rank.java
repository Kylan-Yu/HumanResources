package com.hrms.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 职级实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_rank")
public class Rank {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 职级编码
     */
    @TableField("rank_code")
    private String rankCode;

    /**
     * 职级名称
     */
    @TableField("rank_name")
    private String rankName;

    /**
     * 职级类型：company-职级，hospital-职称职级
     */
    @TableField("rank_type")
    private String rankType;

    /**
     * 职级级别
     */
    @TableField("rank_level")
    private String rankLevel;

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
