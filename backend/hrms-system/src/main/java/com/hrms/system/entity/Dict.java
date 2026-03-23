package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据字典实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_dict")
public class Dict {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dict_name")
    private String dictName;

    @TableField("dict_code")
    private String dictCode;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
