package com.hrms.contract.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同记录VO
 *
 * @author HRMS
 */
@Data
public class ContractRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 合同ID
     */
    private Long contractId;

    /**
     * 记录类型
     */
    private String recordType;

    /**
     * 记录类型描述
     */
    private String recordTypeDesc;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
