package com.hrms.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 组织管理控制器
 *
 * @author HRMS
 */
@Tag(name = "组织管理", description = "组织管理相关接口")
@RestController
@RequestMapping("/orgs")
public class OrganizationController {

    // 模拟组织数据
    private static final List<Map<String, Object>> MOCK_ORGS = new ArrayList<>();
    
    static {
        // 初始化模拟数据
        String[] orgNames = {"总公司", "技术部", "产品部", "市场部", "人事部", "财务部", "开发组", "测试组", "设计组", "运维组"};
        String[] orgCodes = {"HQ", "TECH", "PROD", "MKT", "HR", "FIN", "DEV", "QA", "UI", "OPS"};
        
        for (int i = 0; i < orgNames.length; i++) {
            Map<String, Object> org = new HashMap<>();
            org.put("id", i + 1);
            org.put("name", orgNames[i]);
            org.put("code", orgCodes[i]);
            org.put("type", i == 0 ? "company" : (i <= 5 ? "department" : "team"));
            org.put("parentId", i == 0 ? null : (i <= 5 ? 1 : i - 5));
            org.put("status", 1);
            org.put("description", orgNames[i] + "的描述信息");
            org.put("leader", "负责人" + (i + 1));
            org.put("phone", "010-" + String.format("%04d", i + 1));
            org.put("email", orgCodes[i].toLowerCase() + "@company.com");
            org.put("createdTime", LocalDateTime.now().minusDays(i % 30));
            MOCK_ORGS.add(org);
        }
    }

    @Operation(summary = "组织分页查询", description = "分页查询组织列表")
    @GetMapping("/page")
    public Map<String, Object> getOrgPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type
    ) {
        // 过滤数据
        List<Map<String, Object>> filteredOrgs = MOCK_ORGS.stream()
                .filter(org -> name == null || org.get("name").toString().contains(name))
                .filter(org -> type == null || org.get("type").equals(type))
                .toList();

        // 分页
        int total = filteredOrgs.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageData = start < total ? filteredOrgs.subList(start, end) : new ArrayList<>();

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

    @Operation(summary = "获取组织树", description = "获取组织树结构")
    @GetMapping("/tree")
    public Map<String, Object> getOrgTree() {
        // 构建树结构
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<Integer, Map<String, Object>> orgMap = new HashMap<>();
        
        // 先创建所有节点的映射
        for (Map<String, Object> org : MOCK_ORGS) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", org.get("id"));
            node.put("name", org.get("name"));
            node.put("code", org.get("code"));
            node.put("type", org.get("type"));
            node.put("parentId", org.get("parentId"));
            node.put("children", new ArrayList<>());
            orgMap.put((Integer) org.get("id"), node);
        }
        
        // 构建父子关系
        for (Map<String, Object> node : orgMap.values()) {
            Integer parentId = (Integer) node.get("parentId");
            if (parentId == null) {
                tree.add(node);
            } else {
                Map<String, Object> parent = orgMap.get(parentId);
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", tree);
        return result;
    }

    @Operation(summary = "创建组织", description = "创建新组织")
    @PostMapping
    public Map<String, Object> createOrg(@RequestBody Map<String, Object> orgData) {
        Map<String, Object> newOrg = new HashMap<>(orgData);
        newOrg.put("id", MOCK_ORGS.size() + 1);
        newOrg.put("createdTime", LocalDateTime.now());
        if (newOrg.get("status") == null) {
            newOrg.put("status", 1);
        }
        MOCK_ORGS.add(newOrg);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", newOrg.get("id"));
        return result;
    }

    @Operation(summary = "更新组织", description = "更新组织信息")
    @PutMapping("/{id}")
    public Map<String, Object> updateOrg(@PathVariable Integer id, @RequestBody Map<String, Object> orgData) {
        for (int i = 0; i < MOCK_ORGS.size(); i++) {
            Map<String, Object> org = MOCK_ORGS.get(i);
            if (org.get("id").equals(id)) {
                orgData.put("id", id);
                MOCK_ORGS.set(i, orgData);
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "删除组织", description = "删除组织")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteOrg(@PathVariable Integer id) {
        MOCK_ORGS.removeIf(org -> org.get("id").equals(id));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        result.put("data", true);
        return result;
    }

    @Operation(summary = "获取组织详情", description = "获取组织详细信息")
    @GetMapping("/{id}")
    public Map<String, Object> getOrgDetail(@PathVariable Integer id) {
        Map<String, Object> org = MOCK_ORGS.stream()
                .filter(o -> o.get("id").equals(id))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (org != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", org);
        } else {
            result.put("code", 404);
            result.put("message", "组织不存在");
        }
        return result;
    }
}
