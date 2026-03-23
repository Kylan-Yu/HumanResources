package com.hrms.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户管理控制器
 *
 * @author HRMS
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")
public class UserController {

    // 模拟用户数据
    private static final List<Map<String, Object>> MOCK_USERS = new ArrayList<>();
    
    static {
        // 初始化模拟数据
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", i);
            user.put("username", "user" + i);
            user.put("realName", "用户" + i);
            user.put("mobile", "138" + String.format("%08d", i));
            user.put("email", "user" + i + "@example.com");
            user.put("orgId", 1);
            user.put("orgName", "技术部");
            user.put("deptId", 1);
            user.put("deptName", "开发组");
            user.put("positionId", 1);
            user.put("positionName", "开发工程师");
            user.put("status", i % 3 == 0 ? 0 : 1);
            user.put("industryType", "company");
            user.put("remark", "测试用户" + i);
            user.put("createdTime", LocalDateTime.now().minusDays(i % 30));
            MOCK_USERS.add(user);
        }
    }

    @Operation(summary = "用户分页查询", description = "分页查询用户列表")
    @GetMapping("/page")
    public Map<String, Object> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status
    ) {
        // 过滤数据
        List<Map<String, Object>> filteredUsers = MOCK_USERS.stream()
                .filter(user -> username == null || user.get("username").toString().contains(username))
                .filter(user -> realName == null || user.get("realName").toString().contains(realName))
                .filter(user -> status == null || user.get("status").equals(status))
                .toList();

        // 分页
        int total = filteredUsers.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageData = start < total ? filteredUsers.subList(start, end) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", Map.of(
                "list", pageData,
                "total", total,
                "pageNum", pageNum,
                "pageSize", pageSize
        ));
        return result;
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> newUser = new HashMap<>(userData);
        newUser.put("id", MOCK_USERS.size() + 1);
        newUser.put("createdTime", LocalDateTime.now());
        if (newUser.get("status") == null) {
            newUser.put("status", 1);
        }
        MOCK_USERS.add(newUser);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", newUser.get("id"));
        return result;
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> userData) {
        for (int i = 0; i < MOCK_USERS.size(); i++) {
            Map<String, Object> user = MOCK_USERS.get(i);
            if (user.get("id").equals(id)) {
                userData.put("id", id);
                MOCK_USERS.set(i, userData);
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id) {
        MOCK_USERS.removeIf(user -> user.get("id").equals(id));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "获取用户详情", description = "获取用户详细信息")
    @GetMapping("/{id}")
    public Map<String, Object> getUserDetail(@PathVariable Integer id) {
        Map<String, Object> user = MOCK_USERS.stream()
                .filter(u -> u.get("id").equals(id))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PutMapping("/{id}/password")
    public Map<String, Object> resetPassword(@PathVariable Integer id, @RequestBody Map<String, String> requestData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "密码重置成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "更新用户状态", description = "更新用户状态")
    @PutMapping("/{id}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> requestData) {
        for (Map<String, Object> user : MOCK_USERS) {
            if (user.get("id").equals(id)) {
                user.put("status", requestData.get("status"));
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "状态更新成功");
        result.put("data", true);
        return result;
    }
}
