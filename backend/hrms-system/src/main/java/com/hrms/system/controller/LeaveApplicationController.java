package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.security.SecurityUtils;
import com.hrms.system.service.WorkflowRuntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Leave application and workflow task APIs.
 */
@RestController
@RequiredArgsConstructor
public class LeaveApplicationController {

    private static final Pattern DAY_EXPRESSION = Pattern.compile("(?i)^\\s*days\\s*(<=|>=|<|>|==|!=)\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final WorkflowRuntimeService workflowRuntimeService;

    @GetMapping("/leave-applications/page")
    @PreAuthorize("hasAnyAuthority('*:*:*','leave:manage')")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId
    ) {
        return pageInternal(pageNum, pageSize, applyNo, status, userId, false);
    }

    @GetMapping("/leave-applications/my/page")
    public Result<PageResult<Map<String, Object>>> myPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String applyNo,
            @RequestParam(required = false) String status
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }
        return pageInternal(pageNum, pageSize, applyNo, status, currentUserId, true);
    }

    @PostMapping("/leave-applications")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        String startTime = stringValue(body.get("startTime"));
        String endTime = stringValue(body.get("endTime"));
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Result.error("请填写请假开始和结束时间");
        }

        BigDecimal leaveDays = toBigDecimal(body.get("leaveDays"));
        if (leaveDays == null || leaveDays.compareTo(BigDecimal.ZERO) <= 0) {
            leaveDays = BigDecimal.ONE;
        }

        String applyNo = generateCode("LV", "hr_leave_apply");
        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_leave_apply
                            (apply_no, user_id, employee_id, leave_type, start_time, end_time, leave_days, reason,
                             status, current_instance_id, created_time, updated_time, deleted)
                        VALUES
                            (:applyNo, :userId, :employeeId, :leaveType, :startTime, :endTime, :leaveDays, :reason,
                             'SUBMITTED', NULL, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("applyNo", applyNo)
                        .addValue("userId", currentUserId)
                        .addValue("employeeId", body.get("employeeId"))
                        .addValue("leaveType", valueOrDefault(body.get("leaveType"), "ANNUAL"))
                        .addValue("startTime", startTime)
                        .addValue("endTime", endTime)
                        .addValue("leaveDays", leaveDays)
                        .addValue("reason", body.get("reason"))
        );

        Long leaveId = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (leaveId == null) {
            return Result.error("请假申请创建失败");
        }

        WorkflowRuntimeService.WorkflowStartResult startResult = workflowRuntimeService.startWorkflow(
                "LEAVE",
                leaveId,
                currentUserId,
                body,
                Map.of("days", leaveDays)
        );
        namedParameterJdbcTemplate.update("""
                        UPDATE hr_leave_apply
                        SET status = :status,
                            current_instance_id = :instanceId,
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", leaveId)
                        .addValue("instanceId", startResult.instanceId())
                        .addValue("status", startResult.businessStatus())
        );
        return Result.success(leaveId);
    }

    private Result<PageResult<Map<String, Object>>> pageInternal(
            Integer pageNum,
            Integer pageSize,
            String applyNo,
            String status,
            Long userId,
            boolean forceSelf
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE l.deleted = 0 ");

        if (StringUtils.hasText(applyNo)) {
            where.append(" AND l.apply_no LIKE :applyNo ");
            params.addValue("applyNo", "%" + applyNo + "%");
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND l.status = :status ");
            params.addValue("status", status);
        }
        if (userId != null) {
            where.append(" AND l.user_id = :userId ");
            params.addValue("userId", userId);
        } else if (forceSelf) {
            where.append(" AND 1 = 0 ");
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_leave_apply l " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);

        String sql = """
                SELECT l.id,
                       l.apply_no AS applyNo,
                       l.user_id AS userId,
                       u.real_name AS applicantName,
                       l.leave_type AS leaveType,
                       l.start_time AS startTime,
                       l.end_time AS endTime,
                       l.leave_days AS leaveDays,
                       l.reason,
                       l.status,
                       l.current_instance_id AS currentInstanceId,
                       wi.current_node_name AS currentNodeName,
                       wi.status AS workflowStatus,
                       l.created_time AS createdTime,
                       l.updated_time AS updatedTime
                FROM hr_leave_apply l
                LEFT JOIN sys_user u ON l.user_id = u.id
                LEFT JOIN hr_workflow_instance wi ON l.current_instance_id = wi.id
                """ + where + " ORDER BY l.created_time DESC, l.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, params);
        return Result.success(PageResult.of(rows, total, pageNum, pageSize));
    }

    @GetMapping("/leave-applications/{id}/progress")
    public Result<Map<String, Object>> progress(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        List<Map<String, Object>> applies = namedParameterJdbcTemplate.queryForList("""
                        SELECT l.id,
                               l.apply_no AS applyNo,
                               l.user_id AS userId,
                               l.leave_type AS leaveType,
                               l.start_time AS startTime,
                               l.end_time AS endTime,
                               l.leave_days AS leaveDays,
                               l.reason,
                               l.status,
                               l.current_instance_id AS currentInstanceId,
                               l.created_time AS createdTime,
                               l.updated_time AS updatedTime,
                               u.real_name AS applicantName
                        FROM hr_leave_apply l
                        LEFT JOIN sys_user u ON l.user_id = u.id
                        WHERE l.id = :id AND l.deleted = 0
                        """,
                new MapSqlParameterSource("id", id)
        );

        if (applies.isEmpty()) {
            return Result.error("请假申请不存在");
        }

        Map<String, Object> apply = applies.get(0);
        Long ownerId = toLong(apply.get("userId"));
        if (!Objects.equals(ownerId, currentUserId) && !isCurrentUserAdmin()) {
            Long taskCount = namedParameterJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hr_workflow_task t LEFT JOIN hr_workflow_instance i ON t.instance_id = i.id WHERE i.business_type = 'LEAVE' AND i.business_id = :leaveId AND t.assignee_id = :userId",
                    new MapSqlParameterSource().addValue("leaveId", id).addValue("userId", currentUserId),
                    Long.class
            );
            if (taskCount == null || taskCount == 0) {
                return Result.error("无权限查看该申请");
            }
        }

        Long instanceId = toLong(apply.get("currentInstanceId"));
        List<Map<String, Object>> tasks = new ArrayList<>();
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> instance = null;

        if (instanceId != null) {
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
                            WHERE id = :id AND deleted = 0
                            """,
                    new MapSqlParameterSource("id", instanceId)
            );
            if (!instances.isEmpty()) {
                instance = instances.get(0);
            }

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
                            WHERE t.instance_id = :instanceId AND t.deleted = 0
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

        return Result.success(Map.of(
                "application", apply,
                "instance", instance,
                "tasks", tasks,
                "records", records
        ));
    }

    @GetMapping("/workflow/tasks/todo/page")
    public Result<PageResult<Map<String, Object>>> todoTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String businessType
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", currentUserId);

        StringBuilder where = new StringBuilder("""
                WHERE t.deleted = 0
                  AND t.status = 'PENDING'
                  AND t.assignee_id = :userId
                """);

        if (StringUtils.hasText(businessType)) {
            where.append(" AND i.business_type = :businessType ");
            params.addValue("businessType", businessType);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_workflow_task t INNER JOIN hr_workflow_instance i ON t.instance_id = i.id " + where,
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
                       t.instance_id AS instanceId,
                       t.node_order AS nodeOrder,
                       t.node_name AS nodeName,
                       t.assignee_id AS assigneeId,
                       t.status,
                       t.result,
                       t.comment,
                       t.created_time AS createdTime,
                       i.business_type AS businessType,
                       i.business_id AS businessId,
                       i.status AS instanceStatus,
                       COALESCE(l.apply_no, p.apply_no, o.apply_no) AS applyNo,
                       l.leave_type AS leaveType,
                       l.start_time AS leaveStartTime,
                       l.end_time AS leaveEndTime,
                       l.leave_days AS leaveDays,
                       p.attendance_date AS patchDate,
                       p.patch_time AS patchTime,
                       p.patch_type AS patchType,
                       o.overtime_date AS overtimeDate,
                       o.start_time AS overtimeStartTime,
                       o.end_time AS overtimeEndTime,
                       o.hours AS overtimeHours,
                       COALESCE(l.reason, p.reason, o.reason) AS leaveReason,
                       COALESCE(ul.real_name, up.real_name, uo.real_name, u.real_name) AS applicantName,
                       CASE
                           WHEN i.business_type = 'LEAVE' THEN CONCAT('请假 ', IFNULL(l.leave_type, ''), ' ', IFNULL(DATE_FORMAT(l.start_time, '%Y-%m-%d %H:%i'), ''), ' ~ ', IFNULL(DATE_FORMAT(l.end_time, '%Y-%m-%d %H:%i'), ''))
                           WHEN i.business_type = 'PATCH' THEN CONCAT('补卡 ', IFNULL(p.patch_type, ''), ' ', IFNULL(DATE_FORMAT(p.attendance_date, '%Y-%m-%d'), ''), ' ', IFNULL(DATE_FORMAT(p.patch_time, '%H:%i'), ''))
                           WHEN i.business_type = 'OVERTIME' THEN CONCAT('加班 ', IFNULL(DATE_FORMAT(o.overtime_date, '%Y-%m-%d'), ''), ' ', IFNULL(DATE_FORMAT(o.start_time, '%H:%i'), ''), ' ~ ', IFNULL(DATE_FORMAT(o.end_time, '%H:%i'), ''))
                           ELSE ''
                       END AS businessSummary
                FROM hr_workflow_task t
                INNER JOIN hr_workflow_instance i ON t.instance_id = i.id
                LEFT JOIN hr_leave_apply l ON i.business_type = 'LEAVE' AND i.business_id = l.id
                LEFT JOIN sys_user ul ON l.user_id = ul.id
                LEFT JOIN hr_patch_apply p ON i.business_type = 'PATCH' AND i.business_id = p.id
                LEFT JOIN sys_user up ON p.user_id = up.id
                LEFT JOIN hr_overtime_apply o ON i.business_type = 'OVERTIME' AND i.business_id = o.id
                LEFT JOIN sys_user uo ON o.user_id = uo.id
                LEFT JOIN sys_user u ON i.initiator_id = u.id
                """ + where + " ORDER BY t.created_time ASC, t.id ASC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize));
    }

    @PostMapping("/workflow/tasks/{taskId}/action")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:task:approve')")
    public Result<Boolean> taskAction(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未获取到当前用户");
        }

        String action = stringValue(body.get("action"));
        if (!StringUtils.hasText(action)) {
            return Result.error("审批动作不能为空");
        }
        action = action.toUpperCase();
        if (!List.of("APPROVE", "REJECT", "RETURN").contains(action)) {
            return Result.error("不支持的审批动作");
        }

        String directComment = stringValue(body.get("comment"));
        if (taskId != null) {
            try {
                WorkflowRuntimeService.WorkflowTaskActionResult result =
                        workflowRuntimeService.handleTaskAction(taskId, currentUserId, action, directComment);
                syncBusinessStatus(result.businessType(), result.businessId(), result.businessStatus());
                return Result.success(true);
            } catch (RuntimeException ex) {
                return Result.error(ex.getMessage());
            } catch (Exception ex) {
                return Result.error("task action failed");
            }
        }

        List<Map<String, Object>> tasks = namedParameterJdbcTemplate.queryForList("""
                        SELECT t.id,
                               t.instance_id AS instanceId,
                               t.node_order AS nodeOrder,
                               t.node_name AS nodeName,
                               t.assignee_id AS assigneeId,
                               t.status,
                               i.business_type AS businessType,
                               i.business_id AS businessId
                        FROM hr_workflow_task t
                        INNER JOIN hr_workflow_instance i ON t.instance_id = i.id
                        WHERE t.id = :taskId AND t.deleted = 0
                        """,
                new MapSqlParameterSource("taskId", taskId)
        );

        if (tasks.isEmpty()) {
            return Result.error("审批任务不存在");
        }

        Map<String, Object> task = tasks.get(0);
        Long assigneeId = toLong(task.get("assigneeId"));
        if (!Objects.equals(assigneeId, currentUserId) && !isCurrentUserAdmin()) {
            return Result.error("当前任务不属于你");
        }

        String status = stringValue(task.get("status"));
        if (!"PENDING".equalsIgnoreCase(status)) {
            return Result.error("该任务已处理");
        }

        Long instanceId = toLong(task.get("instanceId"));
        Integer nodeOrder = toInteger(task.get("nodeOrder"));
        String nodeName = stringValue(task.get("nodeName"));
        String businessType = stringValue(task.get("businessType"));
        Long businessId = toLong(task.get("businessId"));
        String comment = stringValue(body.get("comment"));

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

        if ("APPROVE".equals(action)) {
            Integer nextOrder = namedParameterJdbcTemplate.queryForObject(
                    "SELECT MIN(node_order) FROM hr_workflow_task WHERE instance_id = :instanceId AND status = 'WAITING' AND deleted = 0",
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
                syncBusinessStatus(businessType, businessId, "APPROVED");
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
                        new MapSqlParameterSource().addValue("instanceId", instanceId).addValue("nodeOrder", nextOrder)
                );

                String nextNodeName = namedParameterJdbcTemplate.queryForObject(
                        "SELECT node_name FROM hr_workflow_task WHERE instance_id = :instanceId AND node_order = :nodeOrder AND deleted = 0 ORDER BY id LIMIT 1",
                        new MapSqlParameterSource().addValue("instanceId", instanceId).addValue("nodeOrder", nextOrder),
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
                syncBusinessStatus(businessType, businessId, "IN_APPROVAL");
            }
        } else {
            String instanceStatus = "REJECT".equals(action) ? "REJECTED" : "RETURNED";
            String businessStatus = "REJECT".equals(action) ? "REJECTED" : "RETURNED";

            namedParameterJdbcTemplate.update("""
                            UPDATE hr_workflow_task
                            SET status = 'CANCELED',
                                updated_time = NOW()
                            WHERE instance_id = :instanceId
                              AND status = 'WAITING'
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
                    new MapSqlParameterSource().addValue("id", instanceId).addValue("status", instanceStatus)
            );

            syncBusinessStatus(businessType, businessId, businessStatus);
        }

        return Result.success(true);
    }

    private void createWorkflowForLeave(Long leaveId, Long initiatorId, BigDecimal leaveDays, Map<String, Object> body) {
        List<Map<String, Object>> templates = namedParameterJdbcTemplate.queryForList("""
                        SELECT id, version_no AS versionNo
                        FROM hr_workflow_template
                        WHERE business_type = 'LEAVE'
                          AND (LOWER(status) = 'published' OR UPPER(status) = 'ENABLED')
                          AND deleted = 0
                        ORDER BY version_no DESC, id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
        );

        if (templates.isEmpty()) {
            throw new RuntimeException("未找到启用的请假流程模板");
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

        List<Map<String, Object>> nodes = allNodes.stream()
                .filter(node -> matchCondition(stringValue(node.get("conditionExpression")), leaveDays))
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
                            (:templateId, 'LEAVE', :businessId, :initiatorId, :status, :currentNodeOrder, :currentNodeName,
                             NOW(), NOW(), CASE WHEN :status = 'APPROVED' THEN NOW() ELSE NULL END, 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("businessId", leaveId)
                        .addValue("initiatorId", initiatorId)
                        .addValue("status", instanceStatus)
                        .addValue("currentNodeOrder", firstNodeOrder)
                        .addValue("currentNodeName", firstNodeName)
        );

        Long instanceId = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (instanceId == null) {
            throw new RuntimeException("流程实例创建失败");
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
                        UPDATE hr_leave_apply
                        SET status = :status,
                            current_instance_id = :instanceId,
                            updated_time = NOW()
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", leaveId)
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
        try {
            List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(
                    "SELECT ext_json AS extJson FROM sys_user WHERE id = :id AND deleted = 0",
                    new MapSqlParameterSource("id", userId)
            );
            if (rows.isEmpty()) {
                return null;
            }
            String text = stringValue(rows.get(0).get("extJson"));
            if (!StringUtils.hasText(text)) {
                return null;
            }
            int idx = text.indexOf("\"leaderUserId\"");
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
            return Long.valueOf(builder.toString());
        } catch (Exception ignored) {
            return null;
        }
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

    private boolean matchCondition(String conditionExpression, BigDecimal leaveDays) {
        if (!StringUtils.hasText(conditionExpression)) {
            return true;
        }
        Matcher matcher = DAY_EXPRESSION.matcher(conditionExpression.trim());
        if (!matcher.matches()) {
            return true;
        }
        String operator = matcher.group(1);
        BigDecimal expected = new BigDecimal(matcher.group(2));
        int compare = leaveDays.compareTo(expected);
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

    private void syncBusinessStatus(String businessType, Long businessId, String status) {
        if (businessId == null || !StringUtils.hasText(businessType)) {
            return;
        }
        if ("LEAVE".equalsIgnoreCase(businessType)) {
            namedParameterJdbcTemplate.update("""
                            UPDATE hr_leave_apply
                            SET status = :status,
                                updated_time = NOW()
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource().addValue("id", businessId).addValue("status", status)
            );
            return;
        }
        if ("PATCH".equalsIgnoreCase(businessType)) {
            namedParameterJdbcTemplate.update("""
                            UPDATE hr_patch_apply
                            SET status = :status,
                                updated_time = NOW()
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource().addValue("id", businessId).addValue("status", status)
            );
            return;
        }
        if ("OVERTIME".equalsIgnoreCase(businessType)) {
            namedParameterJdbcTemplate.update("""
                            UPDATE hr_overtime_apply
                            SET status = :status,
                                updated_time = NOW()
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource().addValue("id", businessId).addValue("status", status)
            );
        }
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

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}
