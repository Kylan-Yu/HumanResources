package com.hrms.payroll.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.payroll.dto.PayrollStandardCreateDTO;
import com.hrms.payroll.dto.PayrollStandardQueryDTO;
import com.hrms.payroll.dto.PayrollStandardUpdateDTO;
import com.hrms.payroll.service.PayrollStandardService;
import com.hrms.payroll.vo.PayrollStandardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 薪资标准控制器
 *
 * @author HRMS
 */
@Tag(name = "薪资标准管理", description = "薪资标准相关接口")
@RestController
@RequestMapping("/payroll-standards")
@RequiredArgsConstructor
@Validated
public class PayrollStandardController {

    private final PayrollStandardService payrollStandardService;

    @Operation(summary = "分页查询薪资标准")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('payroll:standard:list')")
    public Result<PageResult<PayrollStandardVO>> pagePayrollStandards(@Valid PayrollStandardQueryDTO query) {
        IPage<PayrollStandardVO> page = payrollStandardService.pagePayrollStandards(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "根据ID查询薪资标准详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('payroll:standard:detail')")
    public Result<PayrollStandardVO> getPayrollStandardById(@Parameter(description = "标准ID") @PathVariable Long id) {
        PayrollStandardVO standard = payrollStandardService.getPayrollStandardById(id);
        return Result.success(standard);
    }

    @Operation(summary = "创建薪资标准")
    @PostMapping
    @PreAuthorize("hasAuthority('payroll:standard:add')")
    public Result<Long> createPayrollStandard(@Valid @RequestBody PayrollStandardCreateDTO dto) {
        Long standardId = payrollStandardService.createPayrollStandard(dto);
        return Result.success(standardId);
    }

    @Operation(summary = "更新薪资标准")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('payroll:standard:edit')")
    public Result<Boolean> updatePayrollStandard(
            @Parameter(description = "标准ID") @PathVariable Long id,
            @Valid @RequestBody PayrollStandardUpdateDTO dto) {
        Boolean result = payrollStandardService.updatePayrollStandard(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除薪资标准")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('payroll:standard:remove')")
    public Result<Boolean> deletePayrollStandard(@Parameter(description = "标准ID") @PathVariable Long id) {
        Boolean result = payrollStandardService.deletePayrollStandard(id);
        return Result.success(result);
    }

    @Operation(summary = "更新薪资标准状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('payroll:standard:status')")
    public Result<Boolean> updatePayrollStandardStatus(
            @Parameter(description = "标准ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        Boolean result = payrollStandardService.updatePayrollStandardStatus(id, status);
        return Result.success(result);
    }

    @Operation(summary = "根据员工ID查询适用的薪资标准")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('payroll:standard:detail')")
    public Result<PayrollStandardVO> getPayrollStandardByEmployeeId(
            @Parameter(description = "员工ID") @PathVariable Long employeeId) {
        PayrollStandardVO standard = payrollStandardService.getPayrollStandardByEmployeeId(employeeId);
        return Result.success(standard);
    }
}
