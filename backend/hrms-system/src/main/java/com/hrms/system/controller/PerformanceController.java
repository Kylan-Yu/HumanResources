package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Performance management APIs.
 */
@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/plans/page")
    public Result<PageResult<Map<String, Object>>> pagePlans(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) Integer planYear,
            @RequestParam(required = false) String planPeriod,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE p.deleted = 0 ");

        if (StringUtils.hasText(planName)) {
            where.append(" AND p.plan_name LIKE :planName ");
            params.addValue("planName", "%" + planName + "%");
        }
        if (planYear != null) {
            where.append(" AND p.plan_year = :planYear ");
            params.addValue("planYear", planYear);
        }
        if (StringUtils.hasText(planPeriod)) {
            where.append(" AND p.plan_period = :planPeriod ");
            params.addValue("planPeriod", planPeriod);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND p.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_performance_plan p " + where, params, Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT p.id,
                       p.plan_name AS planName,
                       p.plan_year AS planYear,
                       p.plan_period AS planPeriod,
                       p.org_id AS orgId,
                       o.org_name AS orgName,
                       p.dept_id AS deptId,
                       d.dept_name AS deptName,
                       p.status,
                       p.description,
                       p.industry_type AS industryType,
                       p.created_time AS createdTime,
                       p.updated_time AS updatedTime
                FROM hr_performance_plan p
                LEFT JOIN hr_org o ON p.org_id = o.id
                LEFT JOIN hr_dept d ON p.dept_id = d.id
                """ + where + " ORDER BY p.plan_year DESC, p.plan_period DESC, p.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @GetMapping("/plans/{id}")
    public Result<Object> getPlan(@PathVariable Long id) {
        String sql = """
                SELECT p.id,
                       p.plan_name AS planName,
                       p.plan_year AS planYear,
                       p.plan_period AS planPeriod,
                       p.org_id AS orgId,
                       o.org_name AS orgName,
                       p.dept_id AS deptId,
                       d.dept_name AS deptName,
                       p.status,
                       p.description,
                       p.industry_type AS industryType,
                       p.created_time AS createdTime,
                       p.updated_time AS updatedTime
                FROM hr_performance_plan p
                LEFT JOIN hr_org o ON p.org_id = o.id
                LEFT JOIN hr_dept d ON p.dept_id = d.id
                WHERE p.id = :id AND p.deleted = 0
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id)));
    }

    @PostMapping("/plans")
    public Result<Long> createPlan(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_performance_plan
                    (plan_name, plan_year, plan_period, org_id, dept_id, status, description, industry_type, created_time, updated_time, deleted)
                VALUES
                    (:planName, :planYear, :planPeriod, :orgId, :deptId, :status, :description, :industryType, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("planName", body.get("planName"))
                .addValue("planYear", body.get("planYear"))
                .addValue("planPeriod", body.get("planPeriod"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("status", valueOrDefault(body.get("status"), "DRAFT"))
                .addValue("description", body.get("description"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/plans/{id}")
    public Result<Boolean> updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_performance_plan
                SET plan_name = :planName,
                    plan_year = :planYear,
                    plan_period = :planPeriod,
                    org_id = :orgId,
                    dept_id = :deptId,
                    status = :status,
                    description = :description,
                    industry_type = :industryType,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("planName", body.get("planName"))
                .addValue("planYear", body.get("planYear"))
                .addValue("planPeriod", body.get("planPeriod"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("status", valueOrDefault(body.get("status"), "DRAFT"))
                .addValue("description", body.get("description"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/plans/{id}")
    public Result<Boolean> deletePlan(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_performance_plan SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @PutMapping("/plans/{id}/status")
    public Result<Boolean> updatePlanStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String finalStatus = status;
        if (!StringUtils.hasText(finalStatus) && body != null && body.get("status") != null) {
            finalStatus = String.valueOf(body.get("status"));
        }
        if (!StringUtils.hasText(finalStatus)) {
            return Result.error("状态不能为空");
        }
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_performance_plan SET status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", finalStatus)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/records/page")
    public Result<PageResult<Map<String, Object>>> pageRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String resultStatus
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE r.deleted = 0 ");

        if (planId != null) {
            where.append(" AND r.plan_id = :planId ");
            params.addValue("planId", planId);
        }
        if (StringUtils.hasText(employeeName)) {
            where.append(" AND r.employee_name LIKE :employeeName ");
            params.addValue("employeeName", "%" + employeeName + "%");
        }
        if (StringUtils.hasText(resultStatus)) {
            where.append(" AND r.result_status = :resultStatus ");
            params.addValue("resultStatus", resultStatus);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_performance_record r " + where, params, Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT r.id,
                       r.plan_id AS planId,
                       p.plan_name AS planName,
                       r.employee_id AS employeeId,
                       r.employee_no AS employeeNo,
                       r.employee_name AS employeeName,
                       r.org_id AS orgId,
                       o.org_name AS orgName,
                       r.dept_id AS deptId,
                       d.dept_name AS deptName,
                       r.position_id AS positionId,
                       pos.position_name AS positionName,
                       r.score,
                       r.grade,
                       r.result_status AS resultStatus,
                       r.self_summary AS selfSummary,
                       r.manager_comment AS managerComment,
                       r.hr_comment AS hrComment,
                       r.created_time AS createdTime,
                       r.updated_time AS updatedTime
                FROM hr_performance_record r
                LEFT JOIN hr_performance_plan p ON r.plan_id = p.id
                LEFT JOIN hr_org o ON r.org_id = o.id
                LEFT JOIN hr_dept d ON r.dept_id = d.id
                LEFT JOIN hr_position pos ON r.position_id = pos.id
                """ + where + " ORDER BY r.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @PostMapping("/records")
    public Result<Long> createRecord(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_performance_record
                    (plan_id, employee_id, employee_no, employee_name, org_id, dept_id, position_id, score, grade, result_status,
                     self_summary, manager_comment, hr_comment, industry_type, created_time, updated_time, deleted)
                VALUES
                    (:planId, :employeeId, :employeeNo, :employeeName, :orgId, :deptId, :positionId, :score, :grade, :resultStatus,
                     :selfSummary, :managerComment, :hrComment, :industryType, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("planId", body.get("planId"))
                .addValue("employeeId", body.get("employeeId"))
                .addValue("employeeNo", body.get("employeeNo"))
                .addValue("employeeName", body.get("employeeName"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("positionId", body.get("positionId"))
                .addValue("score", valueOrDefault(body.get("score"), 0))
                .addValue("grade", body.get("grade"))
                .addValue("resultStatus", valueOrDefault(body.get("resultStatus"), "PENDING"))
                .addValue("selfSummary", body.get("selfSummary"))
                .addValue("managerComment", body.get("managerComment"))
                .addValue("hrComment", body.get("hrComment"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/records/{id}")
    public Result<Boolean> updateRecord(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_performance_record
                SET score = :score,
                    grade = :grade,
                    result_status = :resultStatus,
                    self_summary = :selfSummary,
                    manager_comment = :managerComment,
                    hr_comment = :hrComment,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("score", valueOrDefault(body.get("score"), 0))
                .addValue("grade", body.get("grade"))
                .addValue("resultStatus", valueOrDefault(body.get("resultStatus"), "PENDING"))
                .addValue("selfSummary", body.get("selfSummary"))
                .addValue("managerComment", body.get("managerComment"))
                .addValue("hrComment", body.get("hrComment")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/records/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_performance_record SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

