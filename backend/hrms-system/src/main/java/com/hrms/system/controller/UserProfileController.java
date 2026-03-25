package com.hrms.system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.Result;
import com.hrms.system.entity.User;
import com.hrms.system.security.SecurityUtils;
import com.hrms.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Current user profile APIs.
 */
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public Result<Map<String, Object>> profile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }

        Map<String, Object> extJson = parseExtJson(user.getExtJson());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("mobile", user.getMobile());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("address", stringValue(extJson.get("address")));
        result.put("emergencyContact", stringValue(extJson.get("emergencyContact")));
        result.put("updatedTime", user.getUpdatedTime());
        return Result.success(result);
    }

    @PutMapping
    public Result<Boolean> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }

        String mobile = stringValue(body.get("mobile"));
        String email = stringValue(body.get("email"));
        if (StringUtils.hasText(mobile)) {
            user.setMobile(mobile);
        } else {
            user.setMobile(null);
        }
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        Map<String, Object> extJson = parseExtJson(user.getExtJson());
        extJson.put("address", stringValue(body.get("address")));
        extJson.put("emergencyContact", stringValue(body.get("emergencyContact")));
        user.setExtJson(writeExtJson(extJson));
        user.setUpdatedTime(LocalDateTime.now());
        return Result.success(userService.updateById(user));
    }

    private Map<String, Object> parseExtJson(String extJson) {
        if (!StringUtils.hasText(extJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(extJson, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private String writeExtJson(Map<String, Object> extJson) {
        try {
            return objectMapper.writeValueAsString(extJson);
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
