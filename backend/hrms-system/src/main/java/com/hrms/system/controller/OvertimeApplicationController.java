package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.security.SecurityUtils;
import com.hrms.system.service.WorkflowRuntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Overtime application APIs.
 */
@RestController
@RequestMapping("/overtime-applications")
@RequiredArgsConstructor
public class OvertimeApplicationController {

    private static final Pattern SIMPLE_EXPRESSION = Pattern.compile("(?i)^\\s*([a-z_]+)\\s*(<=|>=|<|>|==|!=)\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final WorkflowRuntimeService workflowRuntimeService;

    @GetMapping("/my/page")
    public Result<PageResult<Map<String, Object>>> myPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String month
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", currentUserId);
        StringBuilder where = new StringBuilder(" WHERE o.deleted = 0 AND o.user_id = :userId ");
        if (StringUtils.hasText(applyNo)) {
            where.append(" AND o.apply_no LIKE :applyNo ");
            params.addValue("applyNo", "%" + applyNo + "%");
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND o.status = :status ");
            params.addValue("status", status);
        }
        if (StringUtils.hasText(month)) {
            where.append(" AND DATE_FORMAT(o.overtime_date, '%Y-%m') = :month ");
            params.addValue("month", month);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_overtime_apply o " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT o.id,
                       o.apply_no AS applyNo,
                       o.overtime_date AS overtimeDate,
                       o.start_time AS startTime,
                       o.end_time AS endTime,
                       o.hours,
                       o.reason,
                       o.status,
                       o.current_instance_id AS currentInstanceId,
                       wi.current_node_name AS currentNodeName,
                       o.created_time AS createdTime,
                       o.updated_time AS updatedTime
                FROM hr_overtime_apply o
                LEFT JOIN hr_workflow_instance wi ON o.current_instance_id = wi.id
                """ + where + " ORDER BY o.created_time DESC, o.id DESC LIMIT :limit OFFSET :offset";
        return Result.success(PageResult.of(namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT o.id,
                               o.apply_no AS applyNo,
                               o.user_id AS userId,
                               o.employee_id AS employeeId,
                               o.overtime_date AS overtimeDate,
                               o.start_time AS startTime,
                               o.end_time AS endTime,
                               o.hours,
                               o.reason,
                               o.status,
                               o.current_instance_id AS currentInstanceId,
                               wi.current_node_name AS currentNodeName,
                               o.created_time AS createdTime,
                               o.updated_time AS updatedTime
                        FROM hr_overtime_apply o
                        LEFT JOIN hr_workflow_instance wi ON o.current_instance_id = wi.id
                        WHERE o.id = :id AND o.deleted = 0
                        """,
                new MapSqlParameterSource("id", id)
        );
        if (rows.isEmpty()) {
            return Result.error("加班申请不存在");
        }

        Map<String, Object> row = rows.get(0);
        Long userId = toLong(row.get("userId"));
        if (!Objects.equals(userId, currentUserId) && !isCurrentUserAdmin()) {
            return Result.error("无权限查看该加班申请");
        }
        return Result.success(row);
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        String overtimeDate = stringValue(body.get("overtimeDate"));
        String startTime = stringValue(body.get("startTime"));
        String endTime = stringValue(body.get("endTime"));
        if (!StringUtils.hasText(overtimeDate) || !StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Result.error("请完整填写加班信息");
        }

        BigDecimal hours = toBigDecimal(body.get("hours"));
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            hours = calculateHours(startTime, endTime);
        }
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("加班时长必须大于 0");
        }

        Long employeeId = resolveEmployeeIdByUserId(currentUserId);
        String applyNo = generateCode("OT", "hr_overtime_apply");
        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_overtime_apply
                            (apply_no, user_id, employee_id, overtime_date, start_time, end_time, hours, reason,
                             status, current_instance_id, created_time, updated_time, deleted)
                        VALUES
                            (:applyNo, :userId, :employeeId, :overtimeDate, :startTime, :endTime, :hours, :reason,
                             'SUBMITTED', NULL, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("applyNo", applyNo)
                        .addValue("userId", currentUserId)
                        .addValue("employeeId", employeeId)
                        .addValue("overtimeDate", overtimeDate)
                        .addValue("startTime", startTime)
                        .addValue("endTime", endTime)
                        .addValue("hours", hours)
                        .addValue("reason", body.get("reason"))
        );

        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (id == null) {
            return Result.error("加班申请创建失败");
        }

        WorkflowRuntimeService.WorkflowStartResult startResult = workflowRuntimeService.startWorkflow(
                "OVERTIME",
                id,
                currentUserId,
                body,
                Map.of("hours", hours)
        );
        namedParameterJdbcTemplate.update("""
                        UPDATE hr_overtime_apply
                        SET status = :status,
                            current_instance_id = :instanceId,
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("instanceId", startResult.instanceId())
                        .addValue("status", startResult.businessStatus())
        );
        return Result.success(id);
    }

    @PostMapping("/{id}/withdraw")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> withdraw(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT id,
                               user_id AS userId,
                               status,
                               current_instance_id AS currentInstanceId
                        FROM hr_overtime_apply
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource("id", id)
        );
        if (rows.isEmpty()) {
            return Result.error("加班申请不存在");
        }

        Map<String, Object> apply = rows.get(0);
        Long ownerId = toLong(apply.get("userId"));
        if (!Objects.equals(ownerId, currentUserId)) {
            return Result.error("只能撤回自己的加班申请");
        }

        String status = stringValue(apply.get("status"));
        if (List.of("APPROVED", "REJECTED", "WITHDRAWN").contains(status)) {
            return Result.error("当前状态不允许撤回");
        }

        Long instanceId = toLong(apply.get("currentInstanceId"));
        if (instanceId != null) {
            namedParameterJdbcTemplate.update("""
                            UPDATE hr_workflow_task
                            SET status = 'CANCELED',
                                updated_time = NOW()
                            WHERE instance_id = :instanceId
                              AND status IN ('PENDING', 'WAITING')
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource("instanceId", instanceId)
            );

            namedParameterJdbcTemplate.update("""
                            UPDATE hr_workflow_instance
                            SET status = 'CANCELED',
                                current_node_order = NULL,
                                current_node_name = NULL,
                                finished_time = NOW(),
                                updated_time = NOW()
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource("id", instanceId)
            );

            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_workflow_record
                                (instance_id, task_id, node_order, node_name, approver_id, action, result, comment, action_time, created_time)
                            VALUES
                                (:instanceId, NULL, 0, '发起人撤回', :approverId, 'WITHDRAW', 'WITHDRAW', '申请人主动撤回', NOW(), NOW())
                            """,
                    new MapSqlParameterSource()
                            .addValue("instanceId", instanceId)
                            .addValue("approverId", currentUserId)
            );
        }

        namedParameterJdbcTemplate.update("""
                        UPDATE hr_overtime_apply
                        SET status = 'WITHDRAWN',
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource("id", id)
        );
        return Result.success(true);
    }

    private void createWorkflowForOvertime(Long applyId, Long initiatorId, BigDecimal hours, Map<String, Object> body) {
        List<Map<String, Object>> templates = namedParameterJdbcTemplate.queryForList("""
                        SELECT id
                        FROM hr_workflow_template
                        WHERE business_type = 'OVERTIME'
                          AND status = 'ENABLED'
                          AND deleted = 0
                        ORDER BY version_no DESC, id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
        );
        if (templates.isEmpty()) {
            throw new RuntimeException("未找到启用的加班流程模板");
        }

        Long templateId = toLong(templates.get(0).get("id"));
        List<Map<String, Object>> allNodes = namedParameterJdbcTemplate.queryForList("""
                        SELECT node_order AS nodeOrder,
                               node_name AS nodeName,
                               approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId,
                               condition_expression AS conditionExpression
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId
                          AND deleted = 0
                        ORDER BY node_order ASC, id ASC
                        """,
                new MapSqlParameterSource("templateId", templateId)
        );

        Map<String, BigDecimal> variables = Map.of("hours", hours);
        List<Map<String, Object>> nodes = allNodes.stream()
                .filter(node -> matchCondition(stringValue(node.get("conditionExpression")), variables))
                .sorted(Comparator.comparing(node -> toInteger(node.get("nodeOrder"))))
                .toList();

        Integer firstNodeOrder = nodes.isEmpty() ? null : toInteger(nodes.get(0).get("nodeOrder"));
        String firstNodeName = nodes.isEmpty() ? null : stringValue(nodes.get(0).get("nodeName"));
        String instanceStatus = nodes.isEmpty() ? "APPROVED" : "IN_PROGRESS";

        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_instance
                            (template_id, business_type, business_id, initiator_id, status, current_node_order, current_node_name,
                             created_time, updated_time, finished_time, deleted)
                        VALUES
                            (:templateId, 'OVERTIME', :businessId, :initiatorId, :status, :currentNodeOrder, :currentNodeName,
                             NOW(), NOW(), CASE WHEN :status = 'APPROVED' THEN NOW() ELSE NULL END, 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("businessId", applyId)
                        .addValue("initiatorId", initiatorId)
                        .addValue("status", instanceStatus)
                        .addValue("currentNodeOrder", firstNodeOrder)
                        .addValue("currentNodeName", firstNodeName)
        );

        Long instanceId = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (instanceId == null) {
            throw new RuntimeException("加班流程实例创建失败");
        }

        for (Map<String, Object> node : nodes) {
            Integer nodeOrder = toInteger(node.get("nodeOrder"));
            String nodeName = stringValue(node.get("nodeName"));
            Long assigneeId = resolveAssigneeId(node, initiatorId, body);
            if (assigneeId == null) {
                assigneeId = resolveFallbackApprover();
            }
            String taskStatus = Objects.equals(nodeOrder, firstNodeOrder) ? "PENDING" : "WAITING";
            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_workflow_task
                                (instance_id, node_order, node_name, assignee_id, status, result, comment, action_time,
                                 created_time, updated_time, deleted)
                            VALUES
                                (:instanceId, :nodeOrder, :nodeName, :assigneeId, :status, NULL, NULL, NULL,
                                 NOW(), NOW(), 0)
                            """,
                    new MapSqlParameterSource()
                            .addValue("instanceId", instanceId)
                            .addValue("nodeOrder", nodeOrder)
                            .addValue("nodeName", nodeName)
                            .addValue("assigneeId", assigneeId)
                            .addValue("status", taskStatus)
            );
        }

        namedParameterJdbcTemplate.update("""
                        UPDATE hr_overtime_apply
                        SET status = :status,
                            current_instance_id = :instanceId,
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", applyId)
                        .addValue("instanceId", instanceId)
                        .addValue("status", nodes.isEmpty() ? "APPROVED" : "IN_APPROVAL")
        );
    }

    private Long resolveAssigneeId(Map<String, Object> node, Long initiatorId, Map<String, Object> body) {
        String approverType = stringValue(node.get("approverType"));
        if (!StringUtils.hasText(approverType)) {
            return null;
        }

        approverType = approverType.toUpperCase();
        if ("SELF".equals(approverType)) {
            return initiatorId;
        }
        if ("DIRECT_LEADER".equals(approverType)) {
            Long leaderFromBody = toLong(body.get("leaderUserId"));
            if (leaderFromBody != null) {
                return leaderFromBody;
            }
            return resolveLeaderFromUserExt(initiatorId);
        }
        if ("ROLE".equals(approverType) || "SPECIFIED_ROLE".equals(approverType)) {
            String roleCode = stringValue(node.get("approverRoleCode"));
            if (!StringUtils.hasText(roleCode)) {
                return null;
            }
            List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList("""
                            SELECT u.id
                            FROM sys_user u
                            INNER JOIN sys_user_role ur ON u.id = ur.user_id
                            INNER JOIN sys_role r ON ur.role_id = r.id
                            WHERE r.role_code = :roleCode
                              AND r.deleted = 0
                              AND r.status = 1
                              AND u.deleted = 0
                              AND u.status = 1
                            ORDER BY u.id ASC
                            LIMIT 1
                            """,
                    new MapSqlParameterSource("roleCode", roleCode)
            );
            return users.isEmpty() ? null : toLong(users.get(0).get("id"));
        }
        if ("USER".equals(approverType) || "SPECIFIED_USER".equals(approverType)) {
            return toLong(node.get("approverUserId"));
        }
        return null;
    }

    private Long resolveLeaderFromUserExt(Long userId) {
        List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(
                "SELECT ext_json AS extJson FROM sys_user WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", userId)
        );
        if (users.isEmpty()) {
            return null;
        }
        return extractLongFromJson(users.get(0).get("extJson"), "leaderUserId");
    }

    private Long resolveFallbackApprover() {
        List<Map<String, Object>> admins = namedParameterJdbcTemplate.queryForList("""
                        SELECT u.id
                        FROM sys_user u
                        INNER JOIN sys_user_role ur ON u.id = ur.user_id
                        INNER JOIN sys_role r ON ur.role_id = r.id
                        WHERE r.role_code = 'ADMIN'
                          AND r.deleted = 0
                          AND r.status = 1
                          AND u.deleted = 0
                          AND u.status = 1
                        ORDER BY u.id ASC
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
        );
        if (!admins.isEmpty()) {
            return toLong(admins.get(0).get("id"));
        }
        List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(
                "SELECT id FROM sys_user WHERE deleted = 0 AND status = 1 ORDER BY id ASC LIMIT 1",
                new MapSqlParameterSource()
        );
        return users.isEmpty() ? null : toLong(users.get(0).get("id"));
    }

    private Long resolveEmployeeIdByUserId(Long userId) {
        List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(
                "SELECT ext_json AS extJson, phone, email FROM sys_user WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", userId)
        );
        if (users.isEmpty()) {
            return null;
        }

        Map<String, Object> user = users.get(0);
        Long employeeId = extractLongFromJson(user.get("extJson"), "employeeId");
        if (employeeId != null) {
            return employeeId;
        }

        List<Map<String, Object>> leaveRows = namedParameterJdbcTemplate.queryForList(
                "SELECT employee_id AS employeeId FROM hr_leave_apply WHERE user_id = :userId AND employee_id IS NOT NULL AND deleted = 0 ORDER BY id DESC LIMIT 1",
                new MapSqlParameterSource("userId", userId)
        );
        if (!leaveRows.isEmpty()) {
            return toLong(leaveRows.get(0).get("employeeId"));
        }

        String phone = stringValue(user.get("phone"));
        String email = stringValue(user.get("email"));
        if (!StringUtils.hasText(phone) && !StringUtils.hasText(email)) {
            return null;
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT id FROM hr_employee WHERE deleted = 0 ");
        if (StringUtils.hasText(phone)) {
            sql.append(" AND mobile = :phone ");
            params.addValue("phone", phone);
        }
        if (StringUtils.hasText(email)) {
            if (StringUtils.hasText(phone)) {
                sql.append(" OR (deleted = 0 AND email = :email) ");
            } else {
                sql.append(" AND email = :email ");
            }
            params.addValue("email", email);
        }
        sql.append(" ORDER BY id ASC LIMIT 1 ");
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql.toString(), params);
        return rows.isEmpty() ? null : toLong(rows.get(0).get("id"));
    }

    private BigDecimal calculateHours(String startTime, String endTime) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        if (start == null || end == null || !end.isAfter(start)) {
            return null;
        }
        BigDecimal minutes = BigDecimal.valueOf(Duration.between(start, end).toMinutes());
        return minutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim().replace(" ", "T");
        if (text.length() == 16) {
            text = text + ":00";
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean matchCondition(String conditionExpression, Map<String, BigDecimal> variables) {
        if (!StringUtils.hasText(conditionExpression)) {
            return true;
        }
        Matcher matcher = SIMPLE_EXPRESSION.matcher(conditionExpression.trim());
        if (!matcher.matches()) {
            return true;
        }
        String variable = matcher.group(1).toLowerCase();
        String operator = matcher.group(2);
        BigDecimal expected = new BigDecimal(matcher.group(3));
        BigDecimal actual = variables.get(variable);
        if (actual == null) {
            return true;
        }
        int compare = actual.compareTo(expected);
        return switch (operator) {
            case "<" -> compare < 0;
            case "<=" -> compare <= 0;
            case ">" -> compare > 0;
            case ">=" -> compare >= 0;
            case "==" -> compare == 0;
            case "!=" -> compare != 0;
            default -> true;
        };
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

    private String generateCode(String prefix, String tableName) {
        Long seq = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) + 1 FROM " + tableName + " WHERE DATE(created_time) = CURDATE()",
                new MapSqlParameterSource(),
                Long.class
        );
        if (seq == null) {
            seq = 1L;
        }
        return prefix + LocalDate.now().toString().replace("-", "") + String.format("%03d", seq);
    }

    private Long extractLongFromJson(Object jsonObject, String key) {
        if (jsonObject == null || !StringUtils.hasText(key)) {
            return null;
        }
        String text = String.valueOf(jsonObject);
        int idx = text.indexOf("\"" + key + "\"");
        if (idx < 0) {
            return null;
        }
        String right = text.substring(idx);
        int colon = right.indexOf(':');
        if (colon < 0) {
            return null;
        }
        String numberPart = right.substring(colon + 1).trim();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numberPart.length(); i++) {
            char c = numberPart.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            } else if (builder.length() > 0) {
                break;
            }
        }
        if (builder.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(builder.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
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

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
