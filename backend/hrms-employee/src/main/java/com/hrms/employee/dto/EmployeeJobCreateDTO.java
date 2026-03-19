package com.hrms.employee.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 员工任职信息创建DTO
 *
 * @author HRMS
 */
@Data
public class EmployeeJobCreateDTO {

    /**
     * 所属组织ID
     */
    @NotNull(message = "所属组织不能为空")
    private Long orgId;

    /**
     * 所属部门ID
     */
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位不能为空")
    private Long positionId;

    /**
     * 职级ID
     */
    private Long rankId;

    /**
     * 直属领导ID
     */
    private Long leaderId;

    /**
     * 员工类型：formal-正式工，contract-合同工，intern-实习生
     */
    @NotBlank(message = "员工类型不能为空")
    private String employeeType;

    /**
     * 用工类型：fulltime-全职，parttime-兼职
     */
    @NotBlank(message = "用工类型不能为空")
    private String employmentType;

    /**
     * 入职日期
     */
    @NotNull(message = "入职日期不能为空")
    @PastOrPresent(message = "入职日期不能是未来日期")
    private LocalDate entryDate;

    /**
     * 转正日期
     */
    @PastOrPresent(message = "转正日期不能是未来日期")
    private LocalDate regularDate;

    /**
     * 工作地点
     */
    @Size(max = 100, message = "工作地点长度不能超过100个字符")
    private String workLocation;

    /**
     * 是否主任职：1-是，0-否
     */
    @NotNull(message = "是否主任职不能为空")
    @Min(value = 0, message = "是否主任职值不正确")
    @Max(value = 1, message = "是否主任职值不正确")
    private Integer isMainJob;
}
