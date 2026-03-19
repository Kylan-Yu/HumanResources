package com.hrms.contract.dto;

import lombok.Data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * 合同更新DTO
 *
 * @author HRMS
 */
@Data
public class ContractUpdateDTO {

    /**
     * 合同类型
     */
    private String contractType;

    /**
     * 合同主体
     */
    private String contractSubject;

    /**
     * 开始日期
     */
    @PastOrPresent(message = "开始日期不能是未来日期")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Future(message = "结束日期必须是未来日期")
    private LocalDate endDate;

    /**
     * 签署日期
     */
    @PastOrPresent(message = "签署日期不能是未来日期")
    private LocalDate signDate;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 扩展字段
     */
    private String extJson;

    /**
     * 备注
     */
    private String remark;
}
