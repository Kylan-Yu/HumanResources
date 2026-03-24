package com.hrms.system.controller;

import com.hrms.common.Result;
import com.hrms.system.entity.Org;
import com.hrms.system.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 组织控制器
 *
 * @author HRMS
 */
@Tag(name = "组织管理", description = "组织管理相关接口")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "获取组织树", description = "获取组织树形结构")
    @GetMapping("/tree")
    public Result<List<Org>> getOrgTree() {
        List<Org> orgTree = orgService.getOrgTree();
        return Result.success(orgTree);
    }

    @Operation(summary = "获取组织详情", description = "根据ID获取组织详细信息")
    @GetMapping("/{id}")
    public Result<Org> getOrgDetail(@PathVariable Long id) {
        Org org = orgService.getById(id);
        return Result.success(org);
    }

    @Operation(summary = "创建组织", description = "创建新组织")
    @PostMapping
    public Result<Void> createOrg(@Valid @RequestBody Org org) {
        orgService.createOrg(org);
        return Result.success();
    }

    @Operation(summary = "更新组织", description = "更新组织信息")
    @PutMapping("/{id}")
    public Result<Void> updateOrg(@PathVariable Long id, @Valid @RequestBody Org org) {
        org.setId(id);
        orgService.updateOrg(org);
        return Result.success();
    }

    @Operation(summary = "删除组织", description = "删除组织")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrg(@PathVariable Long id) {
        orgService.deleteOrg(id);
        return Result.success();
    }

    @Operation(summary = "批量删除组织", description = "批量删除组织")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteOrgs(@RequestBody List<Long> ids) {
        orgService.batchDeleteOrgs(ids);
        return Result.success();
    }

    @Operation(summary = "更新组织状态", description = "启用或禁用组织")
    @PutMapping("/{id}/status")
    public Result<Void> updateOrgStatus(@PathVariable Long id, @RequestParam Integer status) {
        orgService.updateOrgStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取所有组织", description = "获取所有启用的组织列表")
    @GetMapping("/list")
    public Result<List<Org>> listOrgs() {
        List<Org> orgs = orgService.listEnabledOrgs();
        return Result.success(orgs);
    }
}
