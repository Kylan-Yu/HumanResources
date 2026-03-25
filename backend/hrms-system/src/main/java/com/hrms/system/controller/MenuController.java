package com.hrms.system.controller;

import com.hrms.common.Result;
import com.hrms.system.entity.Menu;
import com.hrms.system.security.SecurityUtils;
import com.hrms.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Menu APIs.
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单树", description = "获取系统菜单树")
    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }

    @Operation(summary = "获取当前用户菜单树", description = "根据当前登录用户返回可见菜单")
    @GetMapping("/current/tree")
    public Result<List<Menu>> getCurrentMenuTree() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }
        return Result.success(menuService.getUserMenuTree(userId));
    }

    @Operation(summary = "获取角色菜单", description = "获取指定角色的菜单列表")
    @GetMapping("/role/{roleId}")
    public Result<List<Menu>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(menuService.getRoleMenus(roleId));
    }

    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详情")
    @GetMapping("/{id}")
    public Result<Menu> getMenuDetail(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @Operation(summary = "创建菜单", description = "创建新菜单")
    @PostMapping
    public Result<Void> createMenu(@Valid @RequestBody Menu menu) {
        menuService.createMenu(menu);
        return Result.success();
    }

    @Operation(summary = "更新菜单", description = "更新菜单信息")
    @PutMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @Valid @RequestBody Menu menu) {
        menu.setId(id);
        menuService.updateMenu(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单", description = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    @Operation(summary = "更新菜单状态", description = "启用或禁用菜单")
    @PutMapping("/{id}/status")
    public Result<Void> updateMenuStatus(@PathVariable Long id, @RequestParam Integer status) {
        menuService.updateMenuStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "分配角色菜单", description = "为角色分配菜单权限")
    @PostMapping("/role/{roleId}")
    public Result<Void> assignRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        menuService.assignRoleMenus(roleId, menuIds);
        return Result.success();
    }
}
