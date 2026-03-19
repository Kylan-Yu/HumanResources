package com.hrms.contract.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.contract.dto.ContractCreateDTO;
import com.hrms.contract.dto.ContractQueryDTO;
import com.hrms.contract.dto.ContractRenewDTO;
import com.hrms.contract.dto.ContractUpdateDTO;
import com.hrms.contract.service.ContractService;
import com.hrms.contract.vo.ContractVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 合同控制器
 *
 * @author HRMS
 */
@Tag(name = "合同管理", description = "合同相关接口")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
@Validated
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "分页查询合同")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('contract:list')")
    public Result<PageResult<ContractVO>> pageContracts(@Valid ContractQueryDTO query) {
        IPage<ContractVO> page = contractService.pageContracts(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "根据ID查询合同详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:detail')")
    public Result<ContractVO> getContractById(@Parameter(description = "合同ID") @PathVariable Long id) {
        ContractVO contract = contractService.getContractById(id);
        return Result.success(contract);
    }

    @Operation(summary = "创建合同")
    @PostMapping
    @PreAuthorize("hasAuthority('contract:add')")
    public Result<Long> createContract(@Valid @RequestBody ContractCreateDTO dto) {
        Long contractId = contractService.createContract(dto);
        return Result.success(contractId);
    }

    @Operation(summary = "更新合同")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:edit')")
    public Result<Boolean> updateContract(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Valid @RequestBody ContractUpdateDTO dto) {
        Boolean result = contractService.updateContract(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除合同")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:remove')")
    public Result<Boolean> deleteContract(@Parameter(description = "合同ID") @PathVariable Long id) {
        Boolean result = contractService.deleteContract(id);
        return Result.success(result);
    }

    @Operation(summary = "更新合同状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('contract:status')")
    public Result<Boolean> updateContractStatus(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Parameter(description = "合同状态") @RequestParam String status) {
        Boolean result = contractService.updateContractStatus(id, status);
        return Result.success(result);
    }

    @Operation(summary = "续签合同")
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAuthority('contract:renew')")
    public Result<Boolean> renewContract(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Valid @RequestBody ContractRenewDTO dto) {
        Boolean result = contractService.renewContract(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "查询即将到期的合同")
    @GetMapping("/expire-warning/page")
    @PreAuthorize("hasAuthority('contract:warning')")
    public Result<PageResult<ContractVO>> pageExpireWarningContracts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "预警天数") @RequestParam(defaultValue = "30") Integer warningDays) {
        IPage<ContractVO> page = contractService.pageExpireWarningContracts(pageNum, pageSize, warningDays);
        return Result.success(PageResult.of(page));
    }
}
