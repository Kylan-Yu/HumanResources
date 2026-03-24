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
 * Payroll standard API aligned with frontend path `/payroll-standards`.
 */
@RestController
@RequestMapping("/payroll-standards")
@RequiredArgsConstructor
public class PayrollStandardController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String standardName,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String industryType
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE s.deleted = 0 ");

        if (StringUtils.hasText(standardName)) {
            where.append(" AND s.standard_name LIKE :standardName ");
            params.addValue("standardName", "%" + standardName + "%");
        }
        if (orgId != null) {
            where.append(" AND s.org_id = :orgId ");
            params.addValue("orgId", orgId);
        }
        if (deptId != null) {
            where.append(" AND s.dept_id = :deptId ");
            params.addValue("deptId", deptId);
        }
        if (positionId != null) {
            where.append(" AND s.position_id = :positionId ");
            params.addValue("positionId", positionId);
        }
        if (StringUtils.hasText(gradeLevel)) {
            where.append(" AND s.grade_level = :gradeLevel ");
            params.addValue("gradeLevel", gradeLevel);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND s.status = :status ");
            params.addValue("status", status);
        }
        if (StringUtils.hasText(industryType)) {
            where.append(" AND s.industry_type = :industryType ");
            params.addValue("industryType", industryType);
        }

        String countSql = "SELECT COUNT(*) FROM hr_payroll_standard s " + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT s.id,
                       s.standard_name AS standardName,
                       s.org_id AS orgId,
                       o.org_name AS orgName,
                       s.dept_id AS deptId,
                       d.dept_name AS deptName,
                       s.position_id AS positionId,
                       p.position_name AS positionName,
                       s.grade_level AS gradeLevel,
                       s.base_salary AS baseSalary,
                       s.performance_salary AS performanceSalary,
                       s.position_allowance AS positionAllowance,
                       s.meal_allowance AS mealAllowance,
                       s.transport_allowance AS transportAllowance,
                       s.communication_allowance AS communicationAllowance,
                       s.housing_allowance AS housingAllowance,
                       s.other_allowance AS otherAllowance,
                       (IFNULL(s.base_salary,0)+IFNULL(s.performance_salary,0)+IFNULL(s.position_allowance,0)+IFNULL(s.meal_allowance,0)+IFNULL(s.transport_allowance,0)+IFNULL(s.communication_allowance,0)+IFNULL(s.housing_allowance,0)+IFNULL(s.other_allowance,0)) AS totalSalary,
                       s.status,
                       CASE s.status WHEN 'ACTIVE' THEN '启用' WHEN 'INACTIVE' THEN '禁用' ELSE s.status END AS statusDesc,
                       s.industry_type AS industryType,
                       CASE s.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       s.ext_json AS extJson,
                       s.remark,
                       s.created_time AS createdTime,
                       s.updated_time AS updatedTime
                FROM hr_payroll_standard s
                LEFT JOIN hr_org o ON s.org_id = o.id
                LEFT JOIN hr_dept d ON s.dept_id = d.id
                LEFT JOIN hr_position p ON s.position_id = p.id
                """ + where + " ORDER BY s.created_time DESC, s.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(dataSql, params),
                total,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        String sql = """
                SELECT s.id,
                       s.standard_name AS standardName,
                       s.org_id AS orgId,
                       o.org_name AS orgName,
                       s.dept_id AS deptId,
                       d.dept_name AS deptName,
                       s.position_id AS positionId,
                       p.position_name AS positionName,
                       s.grade_level AS gradeLevel,
                       s.base_salary AS baseSalary,
                       s.performance_salary AS performanceSalary,
                       s.position_allowance AS positionAllowance,
                       s.meal_allowance AS mealAllowance,
                       s.transport_allowance AS transportAllowance,
                       s.communication_allowance AS communicationAllowance,
                       s.housing_allowance AS housingAllowance,
                       s.other_allowance AS otherAllowance,
                       (IFNULL(s.base_salary,0)+IFNULL(s.performance_salary,0)+IFNULL(s.position_allowance,0)+IFNULL(s.meal_allowance,0)+IFNULL(s.transport_allowance,0)+IFNULL(s.communication_allowance,0)+IFNULL(s.housing_allowance,0)+IFNULL(s.other_allowance,0)) AS totalSalary,
                       s.status,
                       CASE s.status WHEN 'ACTIVE' THEN '启用' WHEN 'INACTIVE' THEN '禁用' ELSE s.status END AS statusDesc,
                       s.industry_type AS industryType,
                       CASE s.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       s.ext_json AS extJson,
                       s.remark,
                       s.created_time AS createdTime,
                       s.updated_time AS updatedTime
                FROM hr_payroll_standard s
                LEFT JOIN hr_org o ON s.org_id = o.id
                LEFT JOIN hr_dept d ON s.dept_id = d.id
                LEFT JOIN hr_position p ON s.position_id = p.id
                WHERE s.id = :id AND s.deleted = 0
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id)));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<Object> getByEmployeeId(@PathVariable Long employeeId) {
        String sql = """
                SELECT s.id,
                       s.standard_name AS standardName,
                       s.org_id AS orgId,
                       o.org_name AS orgName,
                       s.dept_id AS deptId,
                       d.dept_name AS deptName,
                       s.position_id AS positionId,
                       p.position_name AS positionName,
                       s.grade_level AS gradeLevel,
                       s.base_salary AS baseSalary,
                       s.performance_salary AS performanceSalary,
                       s.position_allowance AS positionAllowance,
                       s.meal_allowance AS mealAllowance,
                       s.transport_allowance AS transportAllowance,
                       s.communication_allowance AS communicationAllowance,
                       s.housing_allowance AS housingAllowance,
                       s.other_allowance AS otherAllowance,
                       (IFNULL(s.base_salary,0)+IFNULL(s.performance_salary,0)+IFNULL(s.position_allowance,0)+IFNULL(s.meal_allowance,0)+IFNULL(s.transport_allowance,0)+IFNULL(s.communication_allowance,0)+IFNULL(s.housing_allowance,0)+IFNULL(s.other_allowance,0)) AS totalSalary,
                       s.status,
                       CASE s.status WHEN 'ACTIVE' THEN '启用' WHEN 'INACTIVE' THEN '禁用' ELSE s.status END AS statusDesc,
                       s.industry_type AS industryType,
                       CASE s.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       s.ext_json AS extJson,
                       s.remark,
                       s.created_time AS createdTime,
                       s.updated_time AS updatedTime
                FROM hr_employee e
                LEFT JOIN hr_payroll_standard s ON
                    s.deleted = 0 AND s.status = 'ACTIVE' AND
                    (s.org_id IS NULL OR s.org_id = e.org_id) AND
                    (s.dept_id IS NULL OR s.dept_id = e.dept_id) AND
                    (s.position_id IS NULL OR s.position_id = e.position_id) AND
                    (s.grade_level IS NULL OR s.grade_level = e.grade_level)
                LEFT JOIN hr_org o ON s.org_id = o.id
                LEFT JOIN hr_dept d ON s.dept_id = d.id
                LEFT JOIN hr_position p ON s.position_id = p.id
                WHERE e.id = :employeeId AND e.deleted = 0
                ORDER BY s.position_id IS NOT NULL DESC, s.dept_id IS NOT NULL DESC, s.org_id IS NOT NULL DESC
                LIMIT 1
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("employeeId", employeeId)));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_payroll_standard
                    (standard_name, org_id, dept_id, position_id, grade_level, base_salary, performance_salary, position_allowance,
                     meal_allowance, transport_allowance, communication_allowance, housing_allowance, other_allowance,
                     status, industry_type, ext_json, remark, created_time, updated_time, deleted)
                VALUES
                    (:standardName, :orgId, :deptId, :positionId, :gradeLevel, :baseSalary, :performanceSalary, :positionAllowance,
                     :mealAllowance, :transportAllowance, :communicationAllowance, :housingAllowance, :otherAllowance,
                     :status, :industryType, :extJson, :remark, NOW(), NOW(), 0)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("standardName", body.get("standardName"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("positionId", body.get("positionId"))
                .addValue("gradeLevel", body.get("gradeLevel"))
                .addValue("baseSalary", valueOrDefault(body.get("baseSalary"), 0))
                .addValue("performanceSalary", valueOrDefault(body.get("performanceSalary"), 0))
                .addValue("positionAllowance", valueOrDefault(body.get("positionAllowance"), 0))
                .addValue("mealAllowance", valueOrDefault(body.get("mealAllowance"), 0))
                .addValue("transportAllowance", valueOrDefault(body.get("transportAllowance"), 0))
                .addValue("communicationAllowance", valueOrDefault(body.get("communicationAllowance"), 0))
                .addValue("housingAllowance", valueOrDefault(body.get("housingAllowance"), 0))
                .addValue("otherAllowance", valueOrDefault(body.get("otherAllowance"), 0))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark"));
        namedParameterJdbcTemplate.update(sql, params);
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_payroll_standard
                SET standard_name = :standardName,
                    org_id = :orgId,
                    dept_id = :deptId,
                    position_id = :positionId,
                    grade_level = :gradeLevel,
                    base_salary = :baseSalary,
                    performance_salary = :performanceSalary,
                    position_allowance = :positionAllowance,
                    meal_allowance = :mealAllowance,
                    transport_allowance = :transportAllowance,
                    communication_allowance = :communicationAllowance,
                    housing_allowance = :housingAllowance,
                    other_allowance = :otherAllowance,
                    status = :status,
                    industry_type = :industryType,
                    ext_json = :extJson,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("standardName", body.get("standardName"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("positionId", body.get("positionId"))
                .addValue("gradeLevel", body.get("gradeLevel"))
                .addValue("baseSalary", valueOrDefault(body.get("baseSalary"), 0))
                .addValue("performanceSalary", valueOrDefault(body.get("performanceSalary"), 0))
                .addValue("positionAllowance", valueOrDefault(body.get("positionAllowance"), 0))
                .addValue("mealAllowance", valueOrDefault(body.get("mealAllowance"), 0))
                .addValue("transportAllowance", valueOrDefault(body.get("transportAllowance"), 0))
                .addValue("communicationAllowance", valueOrDefault(body.get("communicationAllowance"), 0))
                .addValue("housingAllowance", valueOrDefault(body.get("housingAllowance"), 0))
                .addValue("otherAllowance", valueOrDefault(body.get("otherAllowance"), 0))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_payroll_standard SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
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
                "UPDATE hr_payroll_standard SET status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", finalStatus)
        );
        return Result.success(rows > 0);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

