package com.hrms.system.controller;

import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据库测试控制器
 *
 * @author HRMS
 */
@RestController
@RequestMapping("/db-test")
@RequiredArgsConstructor
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/connection")
    public Result<String> testConnection() {
        try {
            // 简单的连接测试
            String result = jdbcTemplate.queryForObject("SELECT 'Database connection OK' as message", String.class);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("数据库连接失败: " + e.getMessage());
        }
    }

    @GetMapping("/tables")
    public Result<String> testTables() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'hr_%'"
            );
            
            StringBuilder sb = new StringBuilder("找到的表: ");
            for (Map<String, Object> table : tables) {
                sb.append(table.get("TABLE_NAME")).append(", ");
            }
            
            return Result.success(sb.toString());
        } catch (Exception e) {
            return Result.error("查询表失败: " + e.getMessage());
        }
    }
}
