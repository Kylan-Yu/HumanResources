package com.hrms.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 *
 * @author HRMS
 */
@Tag(name = "测试接口", description = "系统测试相关接口")
@RestController
@RequestMapping("/api")
public class TestController {

    @Operation(summary = "健康检查", description = "系统健康检查接口")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "hrms-test");
        result.put("version", "1.0.0");
        result.put("message", "HRMS Test Service is running!");
        return result;
    }

    @Operation(summary = "Hello World", description = "Hello World测试接口")
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello HRMS System!");
        result.put("status", "success");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    @Operation(summary = "系统信息", description = "获取系统信息")
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("system", "HRMS - Human Resource Management System");
        result.put("version", "1.0.0");
        result.put("description", "人力资源管理系统");
        result.put("modules", new String[]{"auth", "org", "employee", "contract", "recruit", "payroll"});
        result.put("status", "development");
        return result;
    }
}
