package com.hrms.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hrms.auth.dto.UserCreateDTO;
import com.hrms.auth.dto.UserUpdateDTO;
import com.hrms.auth.dto.UserQueryDTO;
import com.hrms.auth.dto.AssignRoleDTO;
import com.hrms.auth.service.UserService;
import com.hrms.auth.vo.UserVO;
import com.hrms.common.Result;
import com.hrms.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户控制器
 *
 * @author HRMS
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVO>> pageUsers(@Valid UserQueryDTO query) {
        IPage<UserVO> page = userService.pageUsers(query);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), 
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Boolean> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {
        Boolean result = userService.updateUser(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<Boolean> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        Boolean result = userService.deleteUser(id);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:detail')")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assign')")
    public Result<Boolean> assignRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody AssignRoleDTO dto) {
        dto.setUserId(id);
        Boolean result = userService.assignRoles(dto);
        return Result.success(result);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:status')")
    public Result<Boolean> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        Boolean result = userService.updateUserStatus(id, status);
        return Result.success(result);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:reset')")
    public Result<Boolean> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        Boolean result = userService.resetPassword(id, newPassword);
        return Result.success(result);
    }
}
