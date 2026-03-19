package com.hrms.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合同记录实体类
 *
 * @author HRMS
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_contract_record")
public class ContractRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合同ID
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 记录类型
     */
    @TableField("record_type")
    private String recordType;

    /**
     * 旧值
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 新值
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 变更原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
