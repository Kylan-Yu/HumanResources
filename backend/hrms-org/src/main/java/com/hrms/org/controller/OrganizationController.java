package com.hrms.org.controller;

import com.hrms.org.dto.OrganizationCreateDTO;
import com.hrms.org.service.OrganizationService;
import com.hrms.org.vo.OrganizationVO;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 组织控制器
 *
 * @author HRMS
 */
@Tag(name = "组织管理", description = "组织相关接口")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
@Validated
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "获取组织树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:org:list')")
    public Result<List<OrganizationVO>> getOrganizationTree() {
        List<OrganizationVO> tree = organizationService.getOrganizationTree();
        return Result.success(tree);
    }

    @Operation(summary = "分页查询组织")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:org:list')")
    public Result<List<OrganizationVO>> pageOrganizations(
            @Parameter(description = "组织名称") @RequestParam(required = false) String orgName,
            @Parameter(description = "组织类型") @RequestParam(required = false) String orgType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "行业类型") @RequestParam(required = false) String industryType) {
        List<OrganizationVO> organizations = organizationService.listOrganizations(orgName, orgType, status, industryType);
        return Result.success(organizations);
    }

    @Operation(summary = "创建组织")
    @PostMapping
    @PreAuthorize("hasAuthority('system:org:add')")
    public Result<Long> createOrganization(@Valid @RequestBody OrganizationCreateDTO dto) {
        Long orgId = organizationService.createOrganization(dto);
        return Result.success(orgId);
    }

    @Operation(summary = "更新组织")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:edit')")
    public Result<Boolean> updateOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id,
            @Valid @RequestBody OrganizationCreateDTO dto) {
        Boolean result = organizationService.updateOrganization(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:remove')")
    public Result<Boolean> deleteOrganization(@Parameter(description = "组织ID") @PathVariable Long id) {
        Boolean result = organizationService.deleteOrganization(id);
        return Result.success(result);
    }

    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:detail')")
    public Result<OrganizationVO> getOrganizationById(@Parameter(description = "组织ID") @PathVariable Long id) {
        OrganizationVO organization = organizationService.getOrganizationById(id);
        return Result.success(organization);
    }

    @Operation(summary = "更新组织状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:org:status')")
    public Result<Boolean> updateOrganizationStatus(
            @Parameter(description = "组织ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        Boolean result = organizationService.updateOrganizationStatus(id, status);
        return Result.success(result);
    }
}
