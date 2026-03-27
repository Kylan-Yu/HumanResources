package com.hrms.system.service;

import com.hrms.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Workflow runtime helper for linear approval flows.
 */
@Service
@RequiredArgsConstructor
public class WorkflowRuntimeService {

    private static final Pattern SIMPLE_EXPRESSION = Pattern.compile("(?i)^\\s*([a-z_]+)\\s*(<=|>=|<|>|==|!=)\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public WorkflowStartResult startWorkflow(
            String businessType,
            Long businessId,
            Long initiatorId,
            Map<String, Object> requestBody,
            Map<String, BigDecimal> variables
    ) {
        if (!StringUtils.hasText(businessType) || businessId == null || initiatorId == null) {
            throw new RuntimeException("流程启动参数不完整");
        }

        String normalizedBusinessType = businessType.toUpperCase();
        List<Map<String, Object>> templates = namedParameterJdbcTemplate.queryForList("""
                        SELECT id
                        FROM hr_workflow_template
                        WHERE business_type = :businessType
                          AND (LOWER(status) = 'published' OR UPPER(status) = 'ENABLED')
                          AND deleted = 0
                        ORDER BY version_no DESC, id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource("businessType", normalizedBusinessType)
        );
        if (templates.isEmpty()) {
            throw new RuntimeException("未找到启用的流程模板");
        }
        Long templateId = toLong(templates.get(0).get("id"));
        if (templateId == null) {
            throw new RuntimeException("流程模板无效");
        }

        List<Map<String, Object>> allNodes = namedParameterJdbcTemplate.queryForList("""
                        SELECT id,
                               node_order AS nodeOrder,
                               node_name AS nodeName,
                               IFNULL(node_type, 'APPROVAL') AS nodeType,
                               IFNULL(approval_mode, 'ANY') AS approvalMode,
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

        List<Map<String, Object>> matchedNodes = allNodes.stream()
                .filter(node -> matchCondition(stringValue(node.get("conditionExpression")), variables))
                .toList();

        List<ApprovalNode> approvalNodes = matchedNodes.stream()
                .filter(node -> "APPROVAL".equalsIgnoreCase(stringValue(node.get("nodeType"))))
                .map(node -> toApprovalNode(node, initiatorId, requestBody))
                .sorted(Comparator.comparing(ApprovalNode::nodeOrder))
                .toList();
        List<CcNode> ccNodes = matchedNodes.stream()
                .filter(node -> "CC".equalsIgnoreCase(stringValue(node.get("nodeType"))))
                .map(this::toCcNode)
                .toList();

        ApprovalNode firstNode = approvalNodes.isEmpty() ? null : approvalNodes.get(0);
        Integer firstOrder = firstNode == null ? null : firstNode.nodeOrder();
        String firstName = firstNode == null ? null : firstNode.nodeName();
        String instanceStatus = firstNode == null ? "APPROVED" : "IN_PROGRESS";

        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_instance
                            (template_id, business_type, business_id, initiator_id, status, current_node_order, current_node_name,
                             created_time, updated_time, finished_time, deleted)
                        VALUES
                            (:templateId, :businessType, :businessId, :initiatorId, :status, :currentNodeOrder, :currentNodeName,
                             NOW(), NOW(), CASE WHEN :status = 'APPROVED' THEN NOW() ELSE NULL END, 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("businessType", normalizedBusinessType)
                        .addValue("businessId", businessId)
                        .addValue("initiatorId", initiatorId)
                        .addValue("status", instanceStatus)
                        .addValue("currentNodeOrder", firstOrder)
                        .addValue("currentNodeName", firstName)
        );
        Long instanceId = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (instanceId == null) {
            throw new RuntimeException("流程实例创建失败");
        }

        for (CcNode ccNode : ccNodes) {
            for (Long ccUserId : ccNode.ccUserIds()) {
                createCcRecord(instanceId, ccNode.nodeOrder(), ccNode.nodeName(), ccUserId);
            }
        }

        for (ApprovalNode node : approvalNodes) {
            boolean currentNode = Objects.equals(node.nodeOrder(), firstOrder);
            if ("SEQUENTIAL".equalsIgnoreCase(node.approvalMode())) {
                for (int i = 0; i < node.approverIds().size(); i++) {
                    Long assigneeId = node.approverIds().get(i);
                    String taskStatus = currentNode ? (i == 0 ? "PENDING" : "WAITING") : "WAITING";
                    createTask(instanceId, node.nodeOrder(), node.nodeName(), assigneeId, taskStatus);
                }
            } else {
                String taskStatus = currentNode ? "PENDING" : "WAITING";
                for (Long assigneeId : node.approverIds()) {
                    createTask(instanceId, node.nodeOrder(), node.nodeName(), assigneeId, taskStatus);
                }
            }
        }

        return new WorkflowStartResult(
                instanceId,
                firstNode == null ? "APPROVED" : "IN_APPROVAL"
        );
    }

    public WorkflowTaskActionResult handleTaskAction(Long taskId, Long currentUserId, String action, String comment) {
        List<Map<String, Object>> tasks = namedParameterJdbcTemplate.queryForList("""
                        SELECT t.id,
                               t.instance_id AS instanceId,
                               t.node_order AS nodeOrder,
                               t.node_name AS nodeName,
                               t.assignee_id AS assigneeId,
                               t.status,
                               i.template_id AS templateId,
                               i.business_type AS businessType,
                               i.business_id AS businessId
                        FROM hr_workflow_task t
                        INNER JOIN hr_workflow_instance i ON t.instance_id = i.id
                        WHERE t.id = :taskId AND t.deleted = 0
                        """,
                new MapSqlParameterSource("taskId", taskId)
        );
        if (tasks.isEmpty()) {
            throw new RuntimeException("审批任务不存在");
        }

        Map<String, Object> task = tasks.get(0);
        Long assigneeId = toLong(task.get("assigneeId"));
        if (!Objects.equals(assigneeId, currentUserId) && !isAdmin(currentUserId)) {
            throw new RuntimeException("当前任务不属于你");
        }
        String taskStatus = stringValue(task.get("status"));
        if (!"PENDING".equalsIgnoreCase(taskStatus)) {
            throw new RuntimeException("该任务已处理");
        }

        Long instanceId = toLong(task.get("instanceId"));
        Integer nodeOrder = toInteger(task.get("nodeOrder"));
        String nodeName = stringValue(task.get("nodeName"));
        Long templateId = toLong(task.get("templateId"));
        String businessType = stringValue(task.get("businessType"));
        Long businessId = toLong(task.get("businessId"));

        namedParameterJdbcTemplate.update("""
                        UPDATE hr_workflow_task
                        SET status = 'COMPLETED',
                            result = :result,
                            comment = :comment,
                            action_time = NOW(),
                            updated_time = NOW()
                        WHERE id = :taskId
                        """,
                new MapSqlParameterSource()
                        .addValue("taskId", taskId)
                        .addValue("result", action)
                        .addValue("comment", comment)
        );

        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_record
                            (instance_id, task_id, node_order, node_name, approver_id, action, result, comment, action_time, created_time)
                        VALUES
                            (:instanceId, :taskId, :nodeOrder, :nodeName, :approverId, :action, :result, :comment, NOW(), NOW())
                        """,
                new MapSqlParameterSource()
                        .addValue("instanceId", instanceId)
                        .addValue("taskId", taskId)
                        .addValue("nodeOrder", nodeOrder)
                        .addValue("nodeName", nodeName)
                        .addValue("approverId", currentUserId)
                        .addValue("action", action)
                        .addValue("result", action)
                        .addValue("comment", comment)
        );

        if ("APPROVE".equalsIgnoreCase(action)) {
            String mode = resolveApprovalMode(templateId, nodeOrder);
            if ("SEQUENTIAL".equalsIgnoreCase(mode)) {
                Long nextTaskId = namedParameterJdbcTemplate.queryForObject("""
                                SELECT id
                                FROM hr_workflow_task
                                WHERE instance_id = :instanceId
                                  AND node_order = :nodeOrder
                                  AND status = 'WAITING'
                                  AND deleted = 0
                                ORDER BY id ASC
                                LIMIT 1
                                """,
                        new MapSqlParameterSource()
                                .addValue("instanceId", instanceId)
                                .addValue("nodeOrder", nodeOrder),
                        Long.class
                );
                if (nextTaskId != null) {
                    namedParameterJdbcTemplate.update("""
                                    UPDATE hr_workflow_task
                                    SET status = 'PENDING',
                                        updated_time = NOW()
                                    WHERE id = :id
                                    """,
                            new MapSqlParameterSource("id", nextTaskId)
                    );
                    return new WorkflowTaskActionResult(businessType, businessId, "IN_APPROVAL");
                }
            } else if ("ANY".equalsIgnoreCase(mode)) {
                namedParameterJdbcTemplate.update("""
                                UPDATE hr_workflow_task
                                SET status = 'CANCELED',
                                    updated_time = NOW()
                                WHERE instance_id = :instanceId
                                  AND node_order = :nodeOrder
                                  AND status IN ('PENDING', 'WAITING')
                                  AND deleted = 0
                                """,
                        new MapSqlParameterSource()
                                .addValue("instanceId", instanceId)
                                .addValue("nodeOrder", nodeOrder)
                );
            } else {
                Long remainCount = namedParameterJdbcTemplate.queryForObject("""
                                SELECT COUNT(*)
                                FROM hr_workflow_task
                                WHERE instance_id = :instanceId
                                  AND node_order = :nodeOrder
                                  AND status IN ('PENDING', 'WAITING')
                                  AND deleted = 0
                                """,
                        new MapSqlParameterSource()
                                .addValue("instanceId", instanceId)
                                .addValue("nodeOrder", nodeOrder),
                        Long.class
                );
                if (remainCount != null && remainCount > 0) {
                    return new WorkflowTaskActionResult(businessType, businessId, "IN_APPROVAL");
                }
            }

            Integer nextOrder = namedParameterJdbcTemplate.queryForObject("""
                            SELECT MIN(node_order)
                            FROM hr_workflow_task
                            WHERE instance_id = :instanceId
                              AND status = 'WAITING'
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource("instanceId", instanceId),
                    Integer.class
            );
            if (nextOrder == null) {
                namedParameterJdbcTemplate.update("""
                                UPDATE hr_workflow_instance
                                SET status = 'APPROVED',
                                    current_node_order = NULL,
                                    current_node_name = NULL,
                                    finished_time = NOW(),
                                    updated_time = NOW()
                                WHERE id = :id
                                """,
                        new MapSqlParameterSource("id", instanceId)
                );
                return new WorkflowTaskActionResult(businessType, businessId, "APPROVED");
            }

            String nextMode = resolveApprovalMode(templateId, nextOrder);
            if ("SEQUENTIAL".equalsIgnoreCase(nextMode)) {
                Long nextTaskId = namedParameterJdbcTemplate.queryForObject("""
                                SELECT id
                                FROM hr_workflow_task
                                WHERE instance_id = :instanceId
                                  AND node_order = :nodeOrder
                                  AND status = 'WAITING'
                                  AND deleted = 0
                                ORDER BY id ASC
                                LIMIT 1
                                """,
                        new MapSqlParameterSource()
                                .addValue("instanceId", instanceId)
                                .addValue("nodeOrder", nextOrder),
                        Long.class
                );
                if (nextTaskId != null) {
                    namedParameterJdbcTemplate.update("""
                                    UPDATE hr_workflow_task
                                    SET status = 'PENDING',
                                        updated_time = NOW()
                                    WHERE id = :id
                                    """,
                            new MapSqlParameterSource("id", nextTaskId)
                    );
                }
            } else {
                namedParameterJdbcTemplate.update("""
                                UPDATE hr_workflow_task
                                SET status = 'PENDING',
                                    updated_time = NOW()
                                WHERE instance_id = :instanceId
                                  AND node_order = :nodeOrder
                                  AND status = 'WAITING'
                                  AND deleted = 0
                                """,
                        new MapSqlParameterSource()
                                .addValue("instanceId", instanceId)
                                .addValue("nodeOrder", nextOrder)
                );
            }

            String nextNodeName = namedParameterJdbcTemplate.queryForObject("""
                            SELECT node_name
                            FROM hr_workflow_task
                            WHERE instance_id = :instanceId
                              AND node_order = :nodeOrder
                              AND deleted = 0
                            ORDER BY id ASC
                            LIMIT 1
                            """,
                    new MapSqlParameterSource()
                            .addValue("instanceId", instanceId)
                            .addValue("nodeOrder", nextOrder),
                    String.class
            );
            namedParameterJdbcTemplate.update("""
                            UPDATE hr_workflow_instance
                            SET status = 'IN_PROGRESS',
                                current_node_order = :nodeOrder,
                                current_node_name = :nodeName,
                                updated_time = NOW()
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource()
                            .addValue("id", instanceId)
                            .addValue("nodeOrder", nextOrder)
                            .addValue("nodeName", nextNodeName)
            );
            return new WorkflowTaskActionResult(businessType, businessId, "IN_APPROVAL");
        }

        String finalStatus = "REJECT".equalsIgnoreCase(action) ? "REJECTED" : "RETURNED";
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
                        SET status = :status,
                            current_node_order = NULL,
                            current_node_name = NULL,
                            finished_time = NOW(),
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", instanceId)
                        .addValue("status", finalStatus)
        );
        return new WorkflowTaskActionResult(businessType, businessId, finalStatus);
    }

    private ApprovalNode toApprovalNode(Map<String, Object> node, Long initiatorId, Map<String, Object> body) {
        Long nodeId = toLong(node.get("id"));
        Integer nodeOrder = toInteger(node.get("nodeOrder"));
        String nodeName = stringValue(node.get("nodeName"));
        String approvalMode = normalizeApprovalMode(stringValue(node.get("approvalMode")));

        List<Map<String, Object>> approvers = nodeId == null ? List.of() : namedParameterJdbcTemplate.queryForList("""
                        SELECT approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId
                        FROM hr_workflow_template_node_approver
                        WHERE template_node_id = :nodeId
                          AND deleted = 0
                        ORDER BY approver_order ASC, id ASC
                        """,
                new MapSqlParameterSource("nodeId", nodeId)
        );

        if (approvers.isEmpty() && StringUtils.hasText(stringValue(node.get("approverType")))) {
            approvers = List.of(Map.of(
                    "approverType", stringValue(node.get("approverType")),
                    "approverRoleCode", node.get("approverRoleCode"),
                    "approverUserId", node.get("approverUserId")
            ));
        }

        LinkedHashSet<Long> assigneeIds = new LinkedHashSet<>();
        for (Map<String, Object> approver : approvers) {
            assigneeIds.addAll(resolveAssigneeIds(approver, initiatorId, body));
        }
        if (assigneeIds.isEmpty()) {
            Long fallback = resolveFallbackApprover();
            if (fallback != null) {
                assigneeIds.add(fallback);
            }
        }
        return new ApprovalNode(nodeOrder, nodeName, approvalMode, new ArrayList<>(assigneeIds));
    }

    private CcNode toCcNode(Map<String, Object> node) {
        Long nodeId = toLong(node.get("id"));
        Integer nodeOrder = toInteger(node.get("nodeOrder"));
        String nodeName = stringValue(node.get("nodeName"));
        if (nodeId == null) {
            return new CcNode(nodeOrder, nodeName, List.of());
        }

        List<Map<String, Object>> ccs = namedParameterJdbcTemplate.queryForList("""
                        SELECT cc_type AS ccType,
                               cc_role_code AS ccRoleCode,
                               cc_user_id AS ccUserId,
                               cc_dept_id AS ccDeptId
                        FROM hr_workflow_template_node_cc
                        WHERE template_node_id = :nodeId
                          AND deleted = 0
                        ORDER BY cc_order ASC, id ASC
                        """,
                new MapSqlParameterSource("nodeId", nodeId)
        );

        LinkedHashSet<Long> ccUserIds = new LinkedHashSet<>();
        for (Map<String, Object> cc : ccs) {
            ccUserIds.addAll(resolveCcUserIds(cc));
        }
        return new CcNode(nodeOrder, nodeName, new ArrayList<>(ccUserIds));
    }

    private List<Long> resolveCcUserIds(Map<String, Object> cc) {
        String type = stringValue(cc.get("ccType"));
        if (!StringUtils.hasText(type)) {
            return List.of();
        }
        type = type.toUpperCase();
        if ("USER".equals(type) || "SPECIFIED_USER".equals(type)) {
            Long userId = toLong(cc.get("ccUserId"));
            return userId == null ? List.of() : List.of(userId);
        }
        if ("ROLE".equals(type) || "SPECIFIED_ROLE".equals(type)) {
            String roleCode = stringValue(cc.get("ccRoleCode"));
            if (!StringUtils.hasText(roleCode)) {
                return List.of();
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
                            """,
                    new MapSqlParameterSource("roleCode", roleCode)
            );
            return users.stream().map(row -> toLong(row.get("id"))).filter(Objects::nonNull).toList();
        }
        if ("DEPT".equals(type) || "SPECIFIED_DEPT".equals(type)) {
            Long deptId = toLong(cc.get("ccDeptId"));
            if (deptId == null) {
                return List.of();
            }
            List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList("""
                            SELECT u.id
                            FROM hr_employee e
                            INNER JOIN sys_user u ON e.user_id = u.id
                            WHERE e.dept_id = :deptId
                              AND e.deleted = 0
                              AND u.deleted = 0
                              AND u.status = 1
                            ORDER BY u.id ASC
                            """,
                    new MapSqlParameterSource("deptId", deptId)
            );
            return users.stream().map(row -> toLong(row.get("id"))).filter(Objects::nonNull).toList();
        }
        return List.of();
    }

    private List<Long> resolveAssigneeIds(Map<String, Object> approver, Long initiatorId, Map<String, Object> body) {
        String type = stringValue(approver.get("approverType"));
        if (!StringUtils.hasText(type)) {
            return List.of();
        }
        type = type.toUpperCase();
        if ("SELF".equals(type)) {
            return List.of(initiatorId);
        }
        if ("DIRECT_LEADER".equals(type)) {
            Long leader = toLong(body == null ? null : body.get("leaderUserId"));
            if (leader != null) {
                return List.of(leader);
            }
            Long fromExt = resolveLeaderFromUserExt(initiatorId);
            return fromExt == null ? List.of() : List.of(fromExt);
        }
        if ("ROLE".equals(type) || "SPECIFIED_ROLE".equals(type)) {
            String roleCode = stringValue(approver.get("approverRoleCode"));
            if (!StringUtils.hasText(roleCode)) {
                return List.of();
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
                            """,
                    new MapSqlParameterSource("roleCode", roleCode)
            );
            return users.stream().map(row -> toLong(row.get("id"))).filter(Objects::nonNull).toList();
        }
        if ("USER".equals(type) || "SPECIFIED_USER".equals(type)) {
            Long userId = toLong(approver.get("approverUserId"));
            return userId == null ? List.of() : List.of(userId);
        }
        return List.of();
    }

    private String resolveApprovalMode(Long templateId, Integer nodeOrder) {
        String mode = namedParameterJdbcTemplate.queryForObject("""
                        SELECT IFNULL(approval_mode, 'ANY')
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId
                          AND node_order = :nodeOrder
                          AND deleted = 0
                        ORDER BY id ASC
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("nodeOrder", nodeOrder),
                String.class
        );
        return normalizeApprovalMode(mode);
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
        BigDecimal actual = (variables == null ? Collections.<String, BigDecimal>emptyMap() : variables).get(variable);
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

    private void createTask(Long instanceId, Integer nodeOrder, String nodeName, Long assigneeId, String status) {
        if (assigneeId == null) {
            return;
        }
        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_task
                            (instance_id, node_order, node_name, assignee_id, status, result, comment, action_time, created_time, updated_time, deleted)
                        VALUES
                            (:instanceId, :nodeOrder, :nodeName, :assigneeId, :status, NULL, NULL, NULL, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("instanceId", instanceId)
                        .addValue("nodeOrder", nodeOrder)
                        .addValue("nodeName", nodeName)
                        .addValue("assigneeId", assigneeId)
                        .addValue("status", status)
        );
    }

    private void createCcRecord(Long instanceId, Integer nodeOrder, String nodeName, Long ccUserId) {
        if (instanceId == null || ccUserId == null) {
            return;
        }
        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_workflow_record
                            (instance_id, task_id, node_order, node_name, approver_id, action, result, comment, action_time, created_time)
                        VALUES
                            (:instanceId, NULL, :nodeOrder, :nodeName, :approverId, 'CC', 'CC', 'AUTO_CC', NOW(), NOW())
                        """,
                new MapSqlParameterSource()
                        .addValue("instanceId", instanceId)
                        .addValue("nodeOrder", nodeOrder)
                        .addValue("nodeName", nodeName)
                        .addValue("approverId", ccUserId)
        );
    }

    private Long resolveLeaderFromUserExt(Long userId) {
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(
                "SELECT ext_json AS extJson FROM sys_user WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", userId)
        );
        if (rows.isEmpty()) {
            return null;
        }
        return extractLongFromJson(rows.get(0).get("extJson"), "leaderUserId");
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

    public boolean isAdmin(Long userId) {
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record WorkflowStartResult(Long instanceId, String businessStatus) {
    }

    public record WorkflowTaskActionResult(String businessType, Long businessId, String businessStatus) {
    }

    private record ApprovalNode(Integer nodeOrder, String nodeName, String approvalMode, List<Long> approverIds) {
    }

    private record CcNode(Integer nodeOrder, String nodeName, List<Long> ccUserIds) {
    }
}
