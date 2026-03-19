package com.hrms.contract.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同VO
 *
 * @author HRMS
 */
@Data
public class ContractVO {

    /**
     * 合同ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同类型
     */
    private String contractType;

    /**
     * 合同类型描述
     */
    private String contractTypeDesc;

    /**
     * 合同主体
     */
    private String contractSubject;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 签署日期
     */
    private LocalDate signDate;

    /**
     * 合同状态
     */
    private String contractStatus;

    /**
     * 合同状态描述
     */
    private String contractStatusDesc;

    /**
     * 续签次数
     */
    private Integer renewCount;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 行业类型描述
     */
    private String industryTypeDesc;

    /**
     * 扩展字段
     */
    private String extJson;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 合同记录列表
     */
    private List<ContractRecordVO> records;
}
