package com.hrms.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.auth.dto.AssignRoleDTO;
import com.hrms.auth.dto.UserCreateDTO;
import com.hrms.auth.dto.UserQueryDTO;
import com.hrms.auth.dto.UserUpdateDTO;
import com.hrms.auth.service.UserService;
import com.hrms.auth.vo.UserVO;
import com.hrms.common.PageResult;
import com.hrms.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management endpoints.
 */
@Tag(name = "User Management", description = "User related APIs")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "Page users")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVO>> pageUsers(@Valid UserQueryDTO query) {
        IPage<UserVO> page = userService.pageUsers(query);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "Create user")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Boolean> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return Result.success(userService.updateUser(id, dto));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<Boolean> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }

    @Operation(summary = "Get user detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:detail')")
    public Result<UserVO> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "Assign roles")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assign')")
    public Result<Boolean> assignRoles(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AssignRoleDTO dto
    ) {
        dto.setUserId(id);
        return Result.success(userService.assignRoles(dto));
    }

    @Operation(summary = "Update user status")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:status')")
    public Result<Boolean> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Status") @RequestParam Integer status
    ) {
        return Result.success(userService.updateUserStatus(id, status));
    }

    @Operation(summary = "Reset password")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:reset')")
    public Result<Boolean> resetPassword(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New password") @RequestParam String newPassword
    ) {
        return Result.success(userService.resetPassword(id, newPassword));
    }
}
