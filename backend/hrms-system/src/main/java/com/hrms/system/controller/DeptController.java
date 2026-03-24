package com.hrms.system.controller;

import com.hrms.common.Result;
import com.hrms.system.entity.Dept;
import com.hrms.system.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 部门控制器
 *
 * @author HRMS
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @Operation(summary = "获取部门树", description = "获取部门树形结构")
    @GetMapping("/tree")
    public Result<List<Dept>> getDeptTree() {
        List<Dept> deptTree = deptService.getDeptTree();
        return Result.success(deptTree);
    }

    @Operation(summary = "根据组织ID获取部门列表", description = "根据组织ID获取部门列表")
    @GetMapping("/org/{orgId}")
    public Result<List<Dept>> getDeptsByOrgId(@PathVariable Long orgId) {
        List<Dept> depts = deptService.getDeptsByOrgId(orgId);
        return Result.success(depts);
    }

    @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
    @GetMapping("/{id}")
    public Result<Dept> getDeptDetail(@PathVariable Long id) {
        Dept dept = deptService.getById(id);
        return Result.success(dept);
    }

    @Operation(summary = "创建部门", description = "创建新部门")
    @PostMapping
    public Result<Void> createDept(@Valid @RequestBody Dept dept) {
        deptService.createDept(dept);
        return Result.success();
    }

    @Operation(summary = "更新部门", description = "更新部门信息")
    @PutMapping("/{id}")
    public Result<Void> updateDept(@PathVariable Long id, @Valid @RequestBody Dept dept) {
        dept.setId(id);
        deptService.updateDept(dept);
        return Result.success();
    }

    @Operation(summary = "删除部门", description = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }

    @Operation(summary = "批量删除部门", description = "批量删除部门")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteDepts(@RequestBody List<Long> ids) {
        deptService.batchDeleteDepts(ids);
        return Result.success();
    }

    @Operation(summary = "更新部门状态", description = "启用或禁用部门")
    @PutMapping("/{id}/status")
    public Result<Void> updateDeptStatus(@PathVariable Long id, @RequestParam Integer status) {
        deptService.updateDeptStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取所有部门", description = "获取所有启用的部门列表")
    @GetMapping("/list")
    public Result<List<Dept>> listDepts() {
        List<Dept> depts = deptService.listEnabledDepts();
        return Result.success(depts);
    }
}
