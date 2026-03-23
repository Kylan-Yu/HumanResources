package com.hrms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据字典项实体
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_dict_item")
public class DictItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dict_id")
    private Long dictId;

    @TableField("item_name")
    private String itemName;

    @TableField("item_code")
    private String itemCode;

    @TableField("item_value")
    private String itemValue;

    @TableField("description")
    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

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
