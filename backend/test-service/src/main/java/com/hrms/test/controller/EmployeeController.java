package com.hrms.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 员工管理控制器
 *
 * @author HRMS
 */
@Tag(name = "员工管理", description = "员工管理相关接口")
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    // 模拟员工数据
    private static final List<Map<String, Object>> MOCK_EMPLOYEES = new ArrayList<>();
    
    static {
        // 初始化模拟数据
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
        String[] positions = {"开发工程师", "产品经理", "UI设计师", "测试工程师", "运维工程师", "项目经理", "架构师", "数据分析师", "前端开发", "后端开发"};
        String[] levels = {"初级", "中级", "高级", "专家", "资深"};
        String[] depts = {"技术部", "产品部", "设计部", "测试部", "运维部"};
        
        for (int i = 0; i < 50; i++) {
            Map<String, Object> emp = new HashMap<>();
            emp.put("id", i + 1);
            emp.put("name", names[i % names.length] + (i / names.length + 1));
            emp.put("code", "EMP" + String.format("%04d", i + 1));
            emp.put("gender", i % 2 == 0 ? "男" : "女");
            emp.put("mobile", "138" + String.format("%08d", 10000000 + i));
            emp.put("email", "emp" + (i + 1) + "@company.com");
            emp.put("position", positions[i % positions.length]);
            emp.put("level", levels[i % levels.length]);
            emp.put("deptId", (i % 5) + 1);
            emp.put("deptName", depts[i % depts.length]);
            emp.put("orgId", 1);
            emp.put("orgName", "总公司");
            emp.put("status", i % 10 == 0 ? 0 : 1); // 10%的员工离职
            emp.put("hireDate", LocalDateTime.now().minusDays(i % 365));
            emp.put("salary", 8000 + (i * 200));
            emp.put("address", "北京市朝阳区xxx路" + (i + 1) + "号");
            emp.put("education", i % 3 == 0 ? "本科" : (i % 3 == 1 ? "硕士" : "博士"));
            emp.put("birthday", LocalDateTime.now().minusYears(25 + i % 20));
            emp.put("createdTime", LocalDateTime.now().minusDays(i % 30));
            MOCK_EMPLOYEES.add(emp);
        }
    }

    @Operation(summary = "员工分页查询", description = "分页查询员工列表")
    @GetMapping("/page")
    public Map<String, Object> getEmployeePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer status
    ) {
        // 过滤数据
        List<Map<String, Object>> filteredEmployees = MOCK_EMPLOYEES.stream()
                .filter(emp -> name == null || emp.get("name").toString().contains(name))
                .filter(emp -> deptName == null || emp.get("deptName").toString().contains(deptName))
                .filter(emp -> position == null || emp.get("position").toString().contains(position))
                .filter(emp -> status == null || emp.get("status").equals(status))
                .toList();

        // 分页
        int total = filteredEmployees.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageData = start < total ? filteredEmployees.subList(start, end) : new ArrayList<>();

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

    @Operation(summary = "创建员工", description = "创建新员工")
    @PostMapping
    public Map<String, Object> createEmployee(@RequestBody Map<String, Object> empData) {
        Map<String, Object> newEmp = new HashMap<>(empData);
        newEmp.put("id", MOCK_EMPLOYEES.size() + 1);
        newEmp.put("code", "EMP" + String.format("%04d", MOCK_EMPLOYEES.size() + 1));
        newEmp.put("createdTime", LocalDateTime.now());
        if (newEmp.get("status") == null) {
            newEmp.put("status", 1);
        }
        MOCK_EMPLOYEES.add(newEmp);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", newEmp.get("id"));
        return result;
    }

    @Operation(summary = "更新员工", description = "更新员工信息")
    @PutMapping("/{id}")
    public Map<String, Object> updateEmployee(@PathVariable Integer id, @RequestBody Map<String, Object> empData) {
        for (int i = 0; i < MOCK_EMPLOYEES.size(); i++) {
            Map<String, Object> emp = MOCK_EMPLOYEES.get(i);
            if (emp.get("id").equals(id)) {
                empData.put("id", id);
                MOCK_EMPLOYEES.set(i, empData);
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "删除员工", description = "删除员工")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEmployee(@PathVariable Integer id) {
        MOCK_EMPLOYEES.removeIf(emp -> emp.get("id").equals(id));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "获取员工详情", description = "获取员工详细信息")
    @GetMapping("/{id}")
    public Map<String, Object> getEmployeeDetail(@PathVariable Integer id) {
        Map<String, Object> emp = MOCK_EMPLOYEES.stream()
                .filter(e -> e.get("id").equals(id))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (emp != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", emp);
        } else {
            result.put("code", 404);
            result.put("message", "员工不存在");
        }
        return result;
    }

    @Operation(summary = "员工统计", description = "获取员工统计信息")
    @GetMapping("/statistics")
    public Map<String, Object> getEmployeeStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总人数
        long totalCount = MOCK_EMPLOYEES.size();
        statistics.put("totalCount", totalCount);
        
        // 在职人数
        long activeCount = MOCK_EMPLOYEES.stream()
                .filter(emp -> emp.get("status").equals(1))
                .count();
        statistics.put("activeCount", activeCount);
        
        // 离职人数
        long inactiveCount = totalCount - activeCount;
        statistics.put("inactiveCount", inactiveCount);
        
        // 按部门统计
        Map<String, Long> deptStats = new HashMap<>();
        MOCK_EMPLOYEES.stream()
                .filter(emp -> emp.get("status").equals(1))
                .forEach(emp -> {
                    String deptName = emp.get("deptName").toString();
                    deptStats.put(deptName, deptStats.getOrDefault(deptName, 0L) + 1);
                });
        statistics.put("departmentStats", deptStats);
        
        // 按职位统计
        Map<String, Long> positionStats = new HashMap<>();
        MOCK_EMPLOYEES.stream()
                .filter(emp -> emp.get("status").equals(1))
                .forEach(emp -> {
                    String position = emp.get("position").toString();
                    positionStats.put(position, positionStats.getOrDefault(position, 0L) + 1);
                });
        statistics.put("positionStats", positionStats);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", statistics);
        return result;
    }
}
