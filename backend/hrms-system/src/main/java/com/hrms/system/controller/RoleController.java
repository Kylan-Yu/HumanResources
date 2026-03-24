package com.hrms.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import com.hrms.system.entity.Role;
import com.hrms.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 角色控制器
 *
 * @author HRMS
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色分页查询", description = "分页查询角色列表")
    @GetMapping("/page")
    public Result<PageResult<Role>> pageRoles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status
    ) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        IPage<Role> rolePage = roleService.pageRoles(page, roleName, roleCode, status);
        return Result.success(PageResult.of(rolePage));
    }

    @Operation(summary = "获取所有角色", description = "获取所有启用的角色列表")
    @GetMapping("/list")
    public Result<List<Role>> listRoles() {
        List<Role> roles = roleService.listEnabledRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @GetMapping("/{id}")
    public Result<Role> getRoleDetail(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return Result.success(role);
    }

    @Operation(summary = "创建角色", description = "创建新角色")
    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody Role role) {
        roleService.createRole(role);
        return Result.success();
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success();
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "批量删除角色", description = "批量删除角色")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteRoles(@RequestBody List<Long> ids) {
        roleService.batchDeleteRoles(ids);
        return Result.success();
    }

    @Operation(summary = "更新角色状态", description = "启用或禁用角色")
    @PutMapping("/{id}/status")
    public Result<Void> updateRoleStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return Result.success();
    }
}
