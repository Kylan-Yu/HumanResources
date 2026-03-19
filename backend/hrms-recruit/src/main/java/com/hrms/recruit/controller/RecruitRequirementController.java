package com.hrms.recruit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.recruit.dto.RecruitRequirementCreateDTO;
import com.hrms.recruit.dto.RecruitRequirementQueryDTO;
import com.hrms.recruit.dto.RecruitRequirementUpdateDTO;
import com.hrms.recruit.service.RecruitRequirementService;
import com.hrms.recruit.vo.RecruitRequirementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 招聘需求控制器
 *
 * @author HRMS
 */
@Tag(name = "招聘需求管理", description = "招聘需求相关接口")
@RestController
@RequestMapping("/recruit-requirements")
@RequiredArgsConstructor
@Validated
public class RecruitRequirementController {

    private final RecruitRequirementService recruitRequirementService;

    @Operation(summary = "分页查询招聘需求")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('recruit:requirement:list')")
    public Result<PageResult<RecruitRequirementVO>> pageRecruitRequirements(@Valid RecruitRequirementQueryDTO query) {
        IPage<RecruitRequirementVO> page = recruitRequirementService.pageRecruitRequirements(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "根据ID查询招聘需求详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('recruit:requirement:detail')")
    public Result<RecruitRequirementVO> getRecruitRequirementById(@Parameter(description = "需求ID") @PathVariable Long id) {
        RecruitRequirementVO requirement = recruitRequirementService.getRecruitRequirementById(id);
        return Result.success(requirement);
    }

    @Operation(summary = "创建招聘需求")
    @PostMapping
    @PreAuthorize("hasAuthority('recruit:requirement:add')")
    public Result<Long> createRecruitRequirement(@Valid @RequestBody RecruitRequirementCreateDTO dto) {
        Long requirementId = recruitRequirementService.createRecruitRequirement(dto);
        return Result.success(requirementId);
    }

    @Operation(summary = "更新招聘需求")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('recruit:requirement:edit')")
    public Result<Boolean> updateRecruitRequirement(
            @Parameter(description = "需求ID") @PathVariable Long id,
            @Valid @RequestBody RecruitRequirementUpdateDTO dto) {
        Boolean result = recruitRequirementService.updateRecruitRequirement(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除招聘需求")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('recruit:requirement:remove')")
    public Result<Boolean> deleteRecruitRequirement(@Parameter(description = "需求ID") @PathVariable Long id) {
        Boolean result = recruitRequirementService.deleteRecruitRequirement(id);
        return Result.success(result);
    }

    @Operation(summary = "更新招聘需求状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('recruit:requirement:status')")
    public Result<Boolean> updateRecruitRequirementStatus(
            @Parameter(description = "需求ID") @PathVariable Long id,
            @Parameter(description = "需求状态") @RequestParam String status) {
        Boolean result = recruitRequirementService.updateRecruitRequirementStatus(id, status);
        return Result.success(result);
    }
}
