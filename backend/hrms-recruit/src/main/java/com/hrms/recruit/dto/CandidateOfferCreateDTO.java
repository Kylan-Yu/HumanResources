package com.hrms.recruit.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

/**
 * 候选人Offer创建DTO
 *
 * @author HRMS
 */
@Data
public class CandidateOfferCreateDTO {

    /**
     * 候选人ID
     */
    @NotNull(message = "候选人ID不能为空")
    private Long candidateId;

    /**
     * 职位ID
     */
    @NotNull(message = "职位ID不能为空")
    private Long positionId;

    /**
     * 薪资金额
     */
    @NotNull(message = "薪资金额不能为空")
    @Min(value = 0, message = "薪资金额不能为负数")
    private Long salaryAmount;

    /**
     * 入职日期
     */
    @NotNull(message = "入职日期不能为空")
    @FutureOrPresent(message = "入职日期不能是过去日期")
    private LocalDate entryDate;

    /**
     * 备注
     */
    private String remark;
}
