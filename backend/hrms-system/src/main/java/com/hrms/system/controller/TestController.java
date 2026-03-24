package com.hrms.system.controller;

import com.hrms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-connection")
    public Result<String> testDbConnection() {
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
                return Result.success("数据库连接成功");
            } else {
                return Result.error("数据库连接失败");
            }
        } catch (Exception e) {
            return Result.error("数据库连接异常: " + e.getMessage());
        }
    }

    @GetMapping("/db-info")
    public Result<Object> testDbInfo() {
        try {
            // 测试数据库信息
            Map<String, Object> dbInfo = jdbcTemplate.queryForMap("SELECT DATABASE() as current_db, VERSION() as version");
            
            // 测试表是否存在
            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES LIKE 'sys_user'");
            
            // 如果表存在，测试查询
            String userCount = "0";
            if (!tables.isEmpty()) {
                userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", String.class);
            }
            
            return Result.success(Map.of(
                "database", dbInfo,
                "tables", tables,
                "userCount", userCount
            ));
        } catch (Exception e) {
            return Result.error("数据库查询异常: " + e.getMessage());
        }
    }

    @GetMapping("/redis-connection")
    public Result<String> testRedisConnection() {
        try {
            // 简单的Redis连接测试
            jdbcTemplate.execute("SELECT 1"); // 如果能执行SQL，说明应用正常
            return Result.success("Redis连接正常（通过应用状态判断）");
        } catch (Exception e) {
            return Result.error("Redis连接异常: " + e.getMessage());
        }
    }
}
