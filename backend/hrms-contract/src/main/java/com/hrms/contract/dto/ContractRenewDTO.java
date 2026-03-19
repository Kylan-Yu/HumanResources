package com.hrms.contract.dto;

import lombok.Data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 合同续签DTO
 *
 * @author HRMS
 */
@Data
public class ContractRenewDTO {

    /**
     * 新结束日期
     */
    @NotNull(message = "新结束日期不能为空")
    @Future(message = "新结束日期必须是未来日期")
    private LocalDate newEndDate;

    /**
     * 新签署日期
     */
    @NotNull(message = "新签署日期不能为空")
    private LocalDate newSignDate;

    /**
     * 续签原因
     */
    @NotBlank(message = "续签原因不能为空")
    private String renewReason;

    /**
     * 备注
     */
    private String remark;
}
