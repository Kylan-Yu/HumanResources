package com.hrms.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.entity.User;
import com.hrms.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User APIs.
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页查询", description = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<User>> pageUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status
    ) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> userPage = userService.pageUsers(page, username, realName, status);
        return Result.success(PageResult.of(userPage));
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详情信息")
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return Result.success(user.getId());
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return Result.success(true);
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success(true);
    }

    @Operation(summary = "更新用户状态", description = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateUserStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Integer finalStatus = status;
        if (finalStatus == null && body != null && body.get("status") != null) {
            finalStatus = Integer.valueOf(String.valueOf(body.get("status")));
        }
        if (finalStatus == null) {
            return Result.error("状态不能为空");
        }

        User user = new User();
        user.setId(id);
        user.setStatus(finalStatus);
        userService.updateById(user);
        return Result.success(true);
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PutMapping("/{id}/password")
    public Result<Boolean> resetPassword(
            @PathVariable Long id,
            @RequestParam(required = false) String newPassword,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String finalPassword = newPassword;
        if (!StringUtils.hasText(finalPassword) && body != null && body.get("newPassword") != null) {
            finalPassword = String.valueOf(body.get("newPassword"));
        }
        if (!StringUtils.hasText(finalPassword)) {
            return Result.error("新密码不能为空");
        }

        User user = new User();
        user.setId(id);
        user.setPassword(finalPassword); // 实际建议加密
        userService.updateById(user);
        return Result.success(true);
    }
}
