package com.hrms.auth.controller;

import com.hrms.auth.entity.Menu;
import com.hrms.auth.service.MenuQueryService;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Menu query endpoints.
 */
@Tag(name = "Menu Management", description = "Menu related APIs")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuQueryService menuQueryService;

    @Operation(summary = "Get current user menu tree")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<Menu>> getUserMenuTree() {
        return Result.success(menuQueryService.getCurrentUserMenus());
    }

    @Operation(summary = "Get all menus")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<Menu>> getAllMenus() {
        return Result.success(menuQueryService.getAllMenus());
    }
}
