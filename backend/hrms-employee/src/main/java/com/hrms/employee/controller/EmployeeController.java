package com.hrms.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.employee.dto.EmployeeCreateDTO;
import com.hrms.employee.dto.EmployeeUpdateDTO;
import com.hrms.employee.dto.EmployeeQueryDTO;
import com.hrms.employee.service.EmployeeService;
import com.hrms.employee.vo.EmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 员工控制器
 *
 * @author HRMS
 */
@Tag(name = "员工管理", description = "员工相关接口")
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "分页查询员工")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('employee:list')")
    public Result<PageResult<EmployeeVO>> pageEmployees(@Valid EmployeeQueryDTO query) {
        IPage<EmployeeVO> page = employeeService.pageEmployees(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "创建员工")
    @PostMapping
    @PreAuthorize("hasAuthority('employee:add')")
    public Result<Long> createEmployee(@Valid @RequestBody EmployeeCreateDTO dto) {
        Long employeeId = employeeService.createEmployee(dto);
        return Result.success(employeeId);
    }

    @Operation(summary = "更新员工")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:edit')")
    public Result<Boolean> updateEmployee(
            @Parameter(description = "员工ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDTO dto) {
        Boolean result = employeeService.updateEmployee(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:remove')")
    public Result<Boolean> deleteEmployee(@Parameter(description = "员工ID") @PathVariable Long id) {
        Boolean result = employeeService.deleteEmployee(id);
        return Result.success(result);
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:detail')")
    public Result<EmployeeVO> getEmployeeById(@Parameter(description = "员工ID") @PathVariable Long id) {
        EmployeeVO employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    @Operation(summary = "更新员工状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('employee:status')")
    public Result<Boolean> updateEmployeeStatus(
            @Parameter(description = "员工ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        Boolean result = employeeService.updateEmployeeStatus(id, status);
        return Result.success(result);
    }
}
