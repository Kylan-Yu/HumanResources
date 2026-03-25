package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Application center APIs for ESS.
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/my/page")
    public Result<PageResult<Map<String, Object>>> myApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String businessType
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        if (StringUtils.hasText(applyNo)) {
            where.append(" AND t.applyNo LIKE :applyNo ");
            params.addValue("applyNo", "%" + applyNo + "%");
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND t.status = :status ");
            params.addValue("status", status);
        }
        if (StringUtils.hasText(businessType)) {
            where.append(" AND t.businessType = :businessType ");
            params.addValue("businessType", businessType.toUpperCase());
        }

        String unionSql = buildMyApplicationUnionSql();
        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + unionSql + ") t " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = "SELECT * FROM (" + unionSql + ") t " + where + " ORDER BY t.createdTime DESC, t.businessId DESC LIMIT :limit OFFSET :offset";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(dataSql, params);
        return Result.success(PageResult.of(rows, total, pageNum, pageSize));
    }

    @GetMapping("/{businessType}/{businessId}/progress")
    public Result<Map<String, Object>> progress(
            @PathVariable String businessType,
            @PathVariable Long businessId
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        String normalizedType = StringUtils.hasText(businessType) ? businessType.toUpperCase() : "";
        if (!List.of("LEAVE", "PATCH", "OVERTIME").contains(normalizedType)) {
            return Result.error("不支持的业务类型");
        }

        Map<String, Object> application = loadApplication(normalizedType, businessId);
        if (application == null) {
            return Result.error("申请不存在");
        }

        Long ownerId = toLong(application.get("userId"));
        if (!Objects.equals(ownerId, currentUserId) && !isCurrentUserAdmin()) {
            Long taskCount = namedParameterJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hr_workflow_task t INNER JOIN hr_workflow_instance i ON t.instance_id = i.id WHERE i.business_type = :businessType AND i.business_id = :businessId AND t.assignee_id = :userId",
                    new MapSqlParameterSource()
                            .addValue("businessType", normalizedType)
                            .addValue("businessId", businessId)
                            .addValue("userId", currentUserId),
                    Long.class
            );
            if (taskCount == null || taskCount == 0) {
                return Result.error("无权限查看该申请");
            }
        }

        Map<String, Object> instance = null;
        List<Map<String, Object>> tasks = new ArrayList<>();
        List<Map<String, Object>> records = new ArrayList<>();

        List<Map<String, Object>> instances = namedParameterJdbcTemplate.queryForList("""
                        SELECT id,
                               template_id AS templateId,
                               business_type AS businessType,
                               business_id AS businessId,
                               initiator_id AS initiatorId,
                               status,
                               current_node_order AS currentNodeOrder,
                               current_node_name AS currentNodeName,
                               created_time AS createdTime,
                               updated_time AS updatedTime,
                               finished_time AS finishedTime
                        FROM hr_workflow_instance
                        WHERE business_type = :businessType
                          AND business_id = :businessId
                          AND deleted = 0
                        ORDER BY id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
                        .addValue("businessType", normalizedType)
                        .addValue("businessId", businessId)
        );
        if (!instances.isEmpty()) {
            instance = instances.get(0);
            Long instanceId = toLong(instance.get("id"));
            if (instanceId != null) {
                tasks = namedParameterJdbcTemplate.queryForList("""
                                SELECT t.id,
                                       t.instance_id AS instanceId,
                                       t.node_order AS nodeOrder,
                                       t.node_name AS nodeName,
                                       t.assignee_id AS assigneeId,
                                       u.real_name AS assigneeName,
                                       t.status,
                                       t.result,
                                       t.comment,
                                       t.action_time AS actionTime,
                                       t.created_time AS createdTime,
                                       t.updated_time AS updatedTime
                                FROM hr_workflow_task t
                                LEFT JOIN sys_user u ON t.assignee_id = u.id
                                WHERE t.instance_id = :instanceId
                                  AND t.deleted = 0
                                ORDER BY t.node_order ASC, t.id ASC
                                """,
                        new MapSqlParameterSource("instanceId", instanceId)
                );

                records = namedParameterJdbcTemplate.queryForList("""
                                SELECT r.id,
                                       r.instance_id AS instanceId,
                                       r.task_id AS taskId,
                                       r.node_order AS nodeOrder,
                                       r.node_name AS nodeName,
                                       r.approver_id AS approverId,
                                       u.real_name AS approverName,
                                       r.action,
                                       r.result,
                                       r.comment,
                                       r.action_time AS actionTime,
                                       r.created_time AS createdTime
                                FROM hr_workflow_record r
                                LEFT JOIN sys_user u ON r.approver_id = u.id
                                WHERE r.instance_id = :instanceId
                                ORDER BY r.id DESC
                                """,
                        new MapSqlParameterSource("instanceId", instanceId)
                );
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("application", application);
        payload.put("instance", instance);
        payload.put("tasks", tasks);
        payload.put("records", records);
        return Result.success(payload);
    }

    private String buildMyApplicationUnionSql() {
        return """
                SELECT 'LEAVE' AS businessType,
                       l.id AS businessId,
                       l.apply_no AS applyNo,
                       '请假申请' AS applyTypeName,
                       l.leave_type AS applyCategory,
                       l.reason,
                       l.status,
                       wi.current_node_name AS currentNodeName,
                       l.created_time AS createdTime,
                       CONCAT(DATE_FORMAT(l.start_time, '%Y-%m-%d %H:%i'), ' ~ ', DATE_FORMAT(l.end_time, '%Y-%m-%d %H:%i')) AS businessSummary
                FROM hr_leave_apply l
                LEFT JOIN hr_workflow_instance wi ON l.current_instance_id = wi.id
                WHERE l.deleted = 0
                  AND l.user_id = :userId
                UNION ALL
                SELECT 'PATCH' AS businessType,
                       p.id AS businessId,
                       p.apply_no AS applyNo,
                       '补卡申请' AS applyTypeName,
                       p.patch_type AS applyCategory,
                       p.reason,
                       p.status,
                       wi.current_node_name AS currentNodeName,
                       p.created_time AS createdTime,
                       CONCAT(DATE_FORMAT(p.attendance_date, '%Y-%m-%d'), ' ', p.patch_type, ' ', DATE_FORMAT(p.patch_time, '%H:%i')) AS businessSummary
                FROM hr_patch_apply p
                LEFT JOIN hr_workflow_instance wi ON p.current_instance_id = wi.id
                WHERE p.deleted = 0
                  AND p.user_id = :userId
                UNION ALL
                SELECT 'OVERTIME' AS businessType,
                       o.id AS businessId,
                       o.apply_no AS applyNo,
                       '加班申请' AS applyTypeName,
                       '' AS applyCategory,
                       o.reason,
                       o.status,
                       wi.current_node_name AS currentNodeName,
                       o.created_time AS createdTime,
                       CONCAT(DATE_FORMAT(o.overtime_date, '%Y-%m-%d'), ' ', DATE_FORMAT(o.start_time, '%H:%i'), ' ~ ', DATE_FORMAT(o.end_time, '%H:%i'), ' (', o.hours, 'h)') AS businessSummary
                FROM hr_overtime_apply o
                LEFT JOIN hr_workflow_instance wi ON o.current_instance_id = wi.id
                WHERE o.deleted = 0
                  AND o.user_id = :userId
                """;
    }

    private Map<String, Object> loadApplication(String businessType, Long businessId) {
        String sql;
        if ("LEAVE".equals(businessType)) {
            sql = """
                    SELECT id,
                           apply_no AS applyNo,
                           user_id AS userId,
                           leave_type AS applyCategory,
                           reason,
                           status,
                           current_instance_id AS currentInstanceId,
                           created_time AS createdTime,
                           updated_time AS updatedTime
                    FROM hr_leave_apply
                    WHERE id = :id AND deleted = 0
                    """;
        } else if ("PATCH".equals(businessType)) {
            sql = """
                    SELECT id,
                           apply_no AS applyNo,
                           user_id AS userId,
                           patch_type AS applyCategory,
                           reason,
                           status,
                           current_instance_id AS currentInstanceId,
                           created_time AS createdTime,
                           updated_time AS updatedTime
                    FROM hr_patch_apply
                    WHERE id = :id AND deleted = 0
                    """;
        } else {
            sql = """
                    SELECT id,
                           apply_no AS applyNo,
                           user_id AS userId,
                           '' AS applyCategory,
                           reason,
                           status,
                           current_instance_id AS currentInstanceId,
                           created_time AS createdTime,
                           updated_time AS updatedTime
                    FROM hr_overtime_apply
                    WHERE id = :id AND deleted = 0
                    """;
        }

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource("id", businessId));
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> result = rows.get(0);
        result.put("businessType", businessType);
        return result;
    }

    private boolean isCurrentUserAdmin() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        Long count = namedParameterJdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM sys_user_role ur
                        INNER JOIN sys_role r ON ur.role_id = r.id
                        WHERE ur.user_id = :userId
                          AND r.role_code = 'ADMIN'
                          AND r.deleted = 0
                          AND r.status = 1
                        """,
                new MapSqlParameterSource("userId", userId),
                Long.class
        );
        return count != null && count > 0;
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
