package com.hrms.contract.dto;

import com.hrms.contract.enums.ContractTypeEnum;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * 合同创建DTO
 *
 * @author HRMS
 */
@Data
public class ContractCreateDTO {

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    /**
     * 合同类型
     */
    @NotBlank(message = "合同类型不能为空")
    private String contractType;

    /**
     * 合同主体
     */
    @NotBlank(message = "合同主体不能为空")
    private String contractSubject;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @PastOrPresent(message = "开始日期不能是未来日期")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @Future(message = "结束日期必须是未来日期")
    private LocalDate endDate;

    /**
     * 签署日期
     */
    @NotNull(message = "签署日期不能为空")
    @PastOrPresent(message = "签署日期不能是未来日期")
    private LocalDate signDate;

    /**
     * 行业类型
     */
    @NotBlank(message = "行业类型不能为空")
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
