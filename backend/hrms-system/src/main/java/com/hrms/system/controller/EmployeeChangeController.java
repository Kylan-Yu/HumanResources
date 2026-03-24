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
 * Employee change (transfer/promotion/resign...) management.
 */
@RestController
@RequestMapping("/employee-changes")
@RequiredArgsConstructor
public class EmployeeChangeController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> pageChanges(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE c.deleted = 0 ");

        if (employeeId != null) {
            where.append(" AND c.employee_id = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (StringUtils.hasText(employeeName)) {
            where.append(" AND e.name LIKE :employeeName ");
            params.addValue("employeeName", "%" + employeeName + "%");
        }
        if (StringUtils.hasText(changeType)) {
            where.append(" AND c.change_type = :changeType ");
            params.addValue("changeType", changeType);
        }
        if (StringUtils.hasText(beginDate)) {
            where.append(" AND c.change_date >= :beginDate ");
            params.addValue("beginDate", beginDate);
        }
        if (StringUtils.hasText(endDate)) {
            where.append(" AND c.change_date <= :endDate ");
            params.addValue("endDate", endDate);
        }

        String countSql = """
                SELECT COUNT(*)
                FROM hr_employee_change_record c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                """ + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT c.id,
                       c.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       c.change_type AS changeType,
                       c.change_date AS changeDate,
                       c.before_value AS beforeValue,
                       c.after_value AS afterValue,
                       c.change_reason AS changeReason,
                       c.approver_id AS approverId,
                       c.approve_time AS approveTime,
                       c.remark,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_employee_change_record c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                """ + where + " ORDER BY c.change_date DESC, c.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(dataSql, params),
                total,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result<Object> getChangeById(@PathVariable Long id) {
        String sql = """
                SELECT c.id,
                       c.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       c.change_type AS changeType,
                       c.change_date AS changeDate,
                       c.before_value AS beforeValue,
                       c.after_value AS afterValue,
                       c.change_reason AS changeReason,
                       c.approver_id AS approverId,
                       c.approve_time AS approveTime,
                       c.remark,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_employee_change_record c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                WHERE c.id = :id AND c.deleted = 0
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id)));
    }

    @PostMapping
    public Result<Long> createChange(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_employee_change_record
                    (employee_id, change_type, change_date, before_value, after_value, change_reason, approver_id, approve_time, remark, created_time, updated_time, deleted)
                VALUES
                    (:employeeId, :changeType, :changeDate, :beforeValue, :afterValue, :changeReason, :approverId, :approveTime, :remark, NOW(), NOW(), 0)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("employeeId", body.get("employeeId"))
                .addValue("changeType", body.get("changeType"))
                .addValue("changeDate", body.get("changeDate"))
                .addValue("beforeValue", body.get("beforeValue"))
                .addValue("afterValue", body.get("afterValue"))
                .addValue("changeReason", body.get("changeReason"))
                .addValue("approverId", body.get("approverId"))
                .addValue("approveTime", body.get("approveTime"))
                .addValue("remark", body.get("remark"));
        namedParameterJdbcTemplate.update(sql, params);
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateChange(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_employee_change_record
                SET change_type = :changeType,
                    change_date = :changeDate,
                    before_value = :beforeValue,
                    after_value = :afterValue,
                    change_reason = :changeReason,
                    approver_id = :approverId,
                    approve_time = :approveTime,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("changeType", body.get("changeType"))
                .addValue("changeDate", body.get("changeDate"))
                .addValue("beforeValue", body.get("beforeValue"))
                .addValue("afterValue", body.get("afterValue"))
                .addValue("changeReason", body.get("changeReason"))
                .addValue("approverId", body.get("approverId"))
                .addValue("approveTime", body.get("approveTime"))
                .addValue("remark", body.get("remark")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteChange(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_employee_change_record SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }
}

