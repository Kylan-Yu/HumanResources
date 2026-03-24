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
 * 测试组织相关数据库连接
 *
 * @author HRMS
 */
@RestController
@RequestMapping("/test-org")
@RequiredArgsConstructor
public class TestOrgController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/tables")
    public Result<String> testTables() {
        try {
            // 检查组织表是否存在
            List<Map<String, Object>> orgTables = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'hrms_db' AND table_name = 'hr_org'"
            );
            
            List<Map<String, Object>> deptTables = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'hrms_db' AND table_name = 'hr_dept'"
            );
            
            List<Map<String, Object>> positionTables = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'hrms_db' AND table_name = 'hr_position'"
            );
            
            String result = String.format(
                "hr_org: %s, hr_dept: %s, hr_position: %s",
                orgTables.get(0).get("count"),
                deptTables.get(0).get("count"),
                positionTables.get(0).get("count")
            );
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("数据库连接错误: " + e.getMessage());
        }
    }

    @GetMapping("/data")
    public Result<String> testData() {
        try {
            // 检查组织表数据
            Long orgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hr_org", Long.class);
            Long deptCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hr_dept", Long.class);
            Long positionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hr_position", Long.class);
            
            String result = String.format(
                "hr_org数据: %d条, hr_dept数据: %d条, hr_position数据: %d条",
                orgCount, deptCount, positionCount
            );
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("数据查询错误: " + e.getMessage());
        }
    }
}
