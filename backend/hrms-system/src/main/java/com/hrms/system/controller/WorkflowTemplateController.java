package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return Result.success(rows);
    }

    @PostMapping("/{id}/nodes")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> saveNodes(@PathVariable Long id, @RequestBody List<Map<String, Object>> nodes) {
        if (nodes == null) {
            nodes = Collections.emptyList();
        }

        namedParameterJdbcTemplate.update(
                "UPDATE hr_workflow_template_node SET deleted = 1, updated_time = NOW() WHERE template_id = :templateId",
                new MapSqlParameterSource("templateId", id)
        );

        for (Map<String, Object> node : nodes) {
            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_workflow_template_node
                                (template_id, node_order, node_name, approver_type, approver_role_code, approver_user_id,
                                 condition_expression, required_flag, created_time, updated_time, deleted)
                            VALUES
                                (:templateId, :nodeOrder, :nodeName, :approverType, :approverRoleCode, :approverUserId,
                                 :conditionExpression, :requiredFlag, NOW(), NOW(), 0)
                            """,
                    new MapSqlParameterSource()
                            .addValue("templateId", id)
                            .addValue("nodeOrder", valueOrDefault(node.get("nodeOrder"), 1))
                            .addValue("nodeName", node.get("nodeName"))
                            .addValue("approverType", valueOrDefault(node.get("approverType"), "DIRECT_LEADER"))
                            .addValue("approverRoleCode", node.get("approverRoleCode"))
                            .addValue("approverUserId", node.get("approverUserId"))
                            .addValue("conditionExpression", node.get("conditionExpression"))
                            .addValue("requiredFlag", valueOrDefault(node.get("requiredFlag"), 1))
            );
        }

        return Result.success(true);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
