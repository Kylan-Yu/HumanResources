package com.hrms.auth.controller;

import com.hrms.common.Result;
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
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "健康检查", description = "系统健康检查接口")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "hrms-auth");
        data.put("version", "1.0.0");
        return Result.success(data);
    }

    @Operation(summary = "Hello World", description = "Hello World测试接口")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello HRMS Auth Service!");
    }
}
