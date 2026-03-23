package com.hrms.auth.controller;

import com.hrms.auth.mapper.MenuMapper;
import com.hrms.auth.entity.Menu;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author HRMS
 */
@Tag(name = "菜单管理", description = "菜单相关接口")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuMapper menuMapper;

    /**
     * 获取用户菜单树
     */
    @Operation(summary = "获取用户菜单树", description = "获取当前登录用户的菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<Menu>> getUserMenuTree() {
        // TODO: 从SecurityContext获取当前用户
        // Long userId = getCurrentUserId();
        
        // 临时返回所有菜单
        List<Menu> menus = menuMapper.findAllMenus();
        
        // TODO: 构建菜单树结构
        // List<Menu> menuTree = buildMenuTree(menus);
        
        return Result.success(menus);
    }

    /**
     * 获取所有菜单列表
     */
    @Operation(summary = "获取所有菜单", description = "获取系统所有菜单列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> menus = menuMapper.findAllMenus();
        return Result.success(menus);
    }
}
