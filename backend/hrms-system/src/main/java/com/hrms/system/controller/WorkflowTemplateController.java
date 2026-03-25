package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Workflow template APIs.
 */
@RestController
@RequestMapping("/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE t.deleted = 0 ");

        if (StringUtils.hasText(templateName)) {
            where.append(" AND t.template_name LIKE :templateName ");
            params.addValue("templateName", "%" + templateName + "%");
        }
        if (StringUtils.hasText(businessType)) {
            where.append(" AND t.business_type = :businessType ");
            params.addValue("businessType", businessType);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND t.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_workflow_template t " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);

        String sql = """
                SELECT t.id,
                       t.template_name AS templateName,
                       t.business_type AS businessType,
                       t.status,
                       t.version_no AS versionNo,
                       t.remark,
                       t.created_time AS createdTime,
                       t.updated_time AS updatedTime
                FROM hr_workflow_template t
                """ + where + " ORDER BY t.updated_time DESC, t.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT id,
                               template_name AS templateName,
                               business_type AS businessType,
                               status,
                               version_no AS versionNo,
                               remark,
                               created_time AS createdTime,
                               updated_time AS updatedTime
                        FROM hr_workflow_template
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource("id", id)
        );

        if (rows.isEmpty()) {
            return Result.error("流程模板不存在");
        }
        return Result.success(rows.get(0));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        String businessType = stringValue(body.get("businessType"));
        if (!StringUtils.hasText(businessType)) {
            return Result.error("businessType不能为空");
        }

        Integer version = body.get("versionNo") == null ? null : Integer.valueOf(String.valueOf(body.get("versionNo")));
        if (version == null) {
            Integer maxVersion = namedParameterJdbcTemplate.queryForObject(
                    "SELECT IFNULL(MAX(version_no), 0) FROM hr_workflow_template WHERE business_type = :businessType AND deleted = 0",
                    new MapSqlParameterSource("businessType", businessType),
                    Integer.class
            );
            version = (maxVersion == null ? 0 : maxVersion) + 1;
        }

        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_template
                            (template_name, business_type, status, version_no, remark, created_time, updated_time, deleted)
                        VALUES
                            (:templateName, :businessType, :status, :versionNo, :remark, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateName", body.get("templateName"))
                        .addValue("businessType", businessType)
                        .addValue("status", valueOrDefault(body.get("status"), "ENABLED"))
                        .addValue("versionNo", version)
                        .addValue("remark", body.get("remark"))
        );

        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int rows = namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_template
                        SET template_name = :templateName,
                            business_type = :businessType,
                            status = :status,
                            version_no = :versionNo,
                            remark = :remark,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("templateName", body.get("templateName"))
                        .addValue("businessType", body.get("businessType"))
                        .addValue("status", valueOrDefault(body.get("status"), "ENABLED"))
                        .addValue("versionNo", valueOrDefault(body.get("versionNo"), 1))
                        .addValue("remark", body.get("remark"))
        );

        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_workflow_template SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_template_node_approver a
                        INNER JOIN hr_workflow_template_node n ON a.template_node_id = n.id
                        SET a.deleted = 1,
                            a.updated_time = NOW()
                        WHERE n.template_id = :templateId
                        """,
                new MapSqlParameterSource("templateId", id)
        );
        namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_template_node_cc c
                        INNER JOIN hr_workflow_template_node n ON c.template_node_id = n.id
                        SET c.deleted = 1,
                            c.updated_time = NOW()
                        WHERE n.template_id = :templateId
                        """,
                new MapSqlParameterSource("templateId", id)
        );
        namedParameterJdbcTemplate.update(
                "UPDATE hr_workflow_template_node SET deleted = 1, updated_time = NOW() WHERE template_id = :templateId",
                new MapSqlParameterSource("templateId", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/{id}/nodes")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<Map<String, Object>>> nodes(@PathVariable Long id) {
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT id,
                               template_id AS templateId,
                               node_order AS nodeOrder,
                               node_name AS nodeName,
                               IFNULL(node_type, 'APPROVAL') AS nodeType,
                               IFNULL(approval_mode, 'ANY') AS approvalMode,
                               approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId,
                               condition_expression AS conditionExpression,
                               required_flag AS requiredFlag,
                               created_time AS createdTime,
                               updated_time AS updatedTime
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId AND deleted = 0
                        ORDER BY node_order ASC, id ASC
                        """,
                new MapSqlParameterSource("templateId", id)
        );

        if (rows.isEmpty()) {
            return Result.success(rows);
        }

        List<Long> nodeIds = rows.stream()
                .map(row -> toLong(row.get("id")))
                .filter(Objects::nonNull)
                .toList();

        Map<Long, List<Map<String, Object>>> approverMap = loadApproverMap(nodeIds);
        Map<Long, List<Map<String, Object>>> ccMap = loadCcMap(nodeIds);

        for (Map<String, Object> row : rows) {
            Long nodeId = toLong(row.get("id"));
            List<Map<String, Object>> approvers = new ArrayList<>(approverMap.getOrDefault(nodeId, Collections.emptyList()));
            if (approvers.isEmpty() && StringUtils.hasText(stringValue(row.get("approverType")))) {
                Map<String, Object> fallback = new LinkedHashMap<>();
                fallback.put("approverOrder", 1);
                fallback.put("approverType", row.get("approverType"));
                fallback.put("approverRoleCode", row.get("approverRoleCode"));
                fallback.put("approverUserId", row.get("approverUserId"));
                approvers.add(fallback);
            }
            row.put("approvers", approvers);
            row.put("ccUsers", new ArrayList<>(ccMap.getOrDefault(nodeId, Collections.emptyList())));
        }
        return Result.success(rows);
    }

    @PostMapping("/{id}/nodes")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> saveNodes(@PathVariable Long id, @RequestBody List<Map<String, Object>> nodes) {
        if (nodes == null) {
            nodes = Collections.emptyList();
        }

        namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_template_node_approver a
                        INNER JOIN hr_workflow_template_node n ON a.template_node_id = n.id
                        SET a.deleted = 1,
                            a.updated_time = NOW()
                        WHERE n.template_id = :templateId
                        """,
                new MapSqlParameterSource("templateId", id)
        );
        namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_template_node_cc c
                        INNER JOIN hr_workflow_template_node n ON c.template_node_id = n.id
                        SET c.deleted = 1,
                            c.updated_time = NOW()
                        WHERE n.template_id = :templateId
                        """,
                new MapSqlParameterSource("templateId", id)
        );
        namedParameterJdbcTemplate.update(
                "UPDATE hr_workflow_template_node SET deleted = 1, updated_time = NOW() WHERE template_id = :templateId",
                new MapSqlParameterSource("templateId", id)
        );

        for (Map<String, Object> node : nodes) {
            String nodeType = normalizeNodeType(stringValue(node.get("nodeType")));
            String approvalMode = normalizeApprovalMode(stringValue(node.get("approvalMode")));
            List<Map<String, Object>> approvers = toMapList(node.get("approvers"));
            List<Map<String, Object>> ccUsers = toMapList(node.get("ccUsers"));
            if (ccUsers.isEmpty()) {
                ccUsers = toMapList(node.get("ccs"));
            }

            if (approvers.isEmpty()
                    && "APPROVAL".equals(nodeType)
                    && StringUtils.hasText(stringValue(node.get("approverType")))) {
                Map<String, Object> fallback = new LinkedHashMap<>();
                fallback.put("approverType", node.get("approverType"));
                fallback.put("approverRoleCode", node.get("approverRoleCode"));
                fallback.put("approverUserId", node.get("approverUserId"));
                approvers = List.of(fallback);
            }

            Map<String, Object> firstApprover = approvers.isEmpty() ? null : approvers.get(0);
            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_workflow_template_node
                                (template_id, node_order, node_name, node_type, approval_mode,
                                 approver_type, approver_role_code, approver_user_id,
                                 condition_expression, required_flag, created_time, updated_time, deleted)
                            VALUES
                                (:templateId, :nodeOrder, :nodeName, :nodeType, :approvalMode,
                                 :approverType, :approverRoleCode, :approverUserId,
                                 :conditionExpression, :requiredFlag, NOW(), NOW(), 0)
                            """,
                    new MapSqlParameterSource()
                            .addValue("templateId", id)
                            .addValue("nodeOrder", valueOrDefault(node.get("nodeOrder"), 1))
                            .addValue("nodeName", node.get("nodeName"))
                            .addValue("nodeType", nodeType)
                            .addValue("approvalMode", approvalMode)
                            .addValue("approverType", firstApprover == null ? null : firstApprover.get("approverType"))
                            .addValue("approverRoleCode", firstApprover == null ? null : firstApprover.get("approverRoleCode"))
                            .addValue("approverUserId", firstApprover == null ? null : firstApprover.get("approverUserId"))
                            .addValue("conditionExpression", node.get("conditionExpression"))
                            .addValue("requiredFlag", valueOrDefault(node.get("requiredFlag"), 1))
            );

            Long templateNodeId = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
            if (templateNodeId == null) {
                continue;
            }

            for (int i = 0; i < approvers.size(); i++) {
                Map<String, Object> approver = approvers.get(i);
                namedParameterJdbcTemplate.update("""
                                INSERT INTO hr_workflow_template_node_approver
                                    (template_node_id, approver_order, approver_type, approver_role_code, approver_user_id,
                                     created_time, updated_time, deleted)
                                VALUES
                                    (:nodeId, :approverOrder, :approverType, :approverRoleCode, :approverUserId,
                                     NOW(), NOW(), 0)
                                """,
                        new MapSqlParameterSource()
                                .addValue("nodeId", templateNodeId)
                                .addValue("approverOrder", valueOrDefault(approver.get("approverOrder"), i + 1))
                                .addValue("approverType", approver.get("approverType"))
                                .addValue("approverRoleCode", approver.get("approverRoleCode"))
                                .addValue("approverUserId", approver.get("approverUserId"))
                );
            }

            for (int i = 0; i < ccUsers.size(); i++) {
                Map<String, Object> cc = ccUsers.get(i);
                namedParameterJdbcTemplate.update("""
                                INSERT INTO hr_workflow_template_node_cc
                                    (template_node_id, cc_order, cc_type, cc_role_code, cc_user_id, cc_dept_id, cc_timing,
                                     created_time, updated_time, deleted)
                                VALUES
                                    (:nodeId, :ccOrder, :ccType, :ccRoleCode, :ccUserId, :ccDeptId, :ccTiming,
                                     NOW(), NOW(), 0)
                                """,
                        new MapSqlParameterSource()
                                .addValue("nodeId", templateNodeId)
                                .addValue("ccOrder", valueOrDefault(cc.get("ccOrder"), i + 1))
                                .addValue("ccType", valueOrDefault(cc.get("ccType"), "SPECIFIED_USER"))
                                .addValue("ccRoleCode", cc.get("ccRoleCode"))
                                .addValue("ccUserId", cc.get("ccUserId"))
                                .addValue("ccDeptId", cc.get("ccDeptId"))
                                .addValue("ccTiming", valueOrDefault(cc.get("ccTiming"), "AFTER_APPROVAL"))
                );
            }
        }

        return Result.success(true);
    }

    private Map<Long, List<Map<String, Object>>> loadApproverMap(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> approvers = namedParameterJdbcTemplate.queryForList("""
                        SELECT template_node_id AS nodeId,
                               approver_order AS approverOrder,
                               approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId
                        FROM hr_workflow_template_node_approver
                        WHERE template_node_id IN (:nodeIds)
                          AND deleted = 0
                        ORDER BY template_node_id ASC, approver_order ASC, id ASC
                        """,
                new MapSqlParameterSource("nodeIds", nodeIds)
        );

        return approvers.stream()
                .collect(Collectors.groupingBy(
                        row -> toLong(row.get("nodeId")),
                        HashMap::new,
                        Collectors.mapping(row -> new LinkedHashMap<>(row), Collectors.toCollection(ArrayList::new))
                ));
    }

    private Map<Long, List<Map<String, Object>>> loadCcMap(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> ccs = namedParameterJdbcTemplate.queryForList("""
                        SELECT template_node_id AS nodeId,
                               cc_order AS ccOrder,
                               cc_type AS ccType,
                               cc_role_code AS ccRoleCode,
                               cc_user_id AS ccUserId,
                               cc_dept_id AS ccDeptId,
                               cc_timing AS ccTiming
                        FROM hr_workflow_template_node_cc
                        WHERE template_node_id IN (:nodeIds)
                          AND deleted = 0
                        ORDER BY template_node_id ASC, cc_order ASC, id ASC
                        """,
                new MapSqlParameterSource("nodeIds", nodeIds)
        );

        return ccs.stream()
                .collect(Collectors.groupingBy(
                        row -> toLong(row.get("nodeId")),
                        HashMap::new,
                        Collectors.mapping(row -> new LinkedHashMap<>(row), Collectors.toCollection(ArrayList::new))
                ));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMapList(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> castMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                castMap.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            result.add(castMap);
        }
        return result;
    }

    private String normalizeNodeType(String nodeType) {
        if (!StringUtils.hasText(nodeType)) {
            return "APPROVAL";
        }
        return "CC".equalsIgnoreCase(nodeType) ? "CC" : "APPROVAL";
    }

    private String normalizeApprovalMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "ANY";
        }
        mode = mode.toUpperCase();
        return switch (mode) {
            case "ALL", "SEQUENTIAL" -> mode;
            default -> "ANY";
        };
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }
}
