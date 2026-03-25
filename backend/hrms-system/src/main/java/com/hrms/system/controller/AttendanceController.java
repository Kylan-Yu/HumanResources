package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Attendance management APIs.
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/shifts/page")
    public Result<PageResult<Map<String, Object>>> pageShifts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String shiftCode,
            @RequestParam(required = false) String shiftName,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE s.deleted = 0 ");

        if (StringUtils.hasText(shiftCode)) {
            where.append(" AND s.shift_code LIKE :shiftCode ");
            params.addValue("shiftCode", "%" + shiftCode + "%");
        }
        if (StringUtils.hasText(shiftName)) {
            where.append(" AND s.shift_name LIKE :shiftName ");
            params.addValue("shiftName", "%" + shiftName + "%");
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND s.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_attendance_shift s " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT s.id,
                       s.shift_code AS shiftCode,
                       s.shift_name AS shiftName,
                       s.start_time AS startTime,
                       s.end_time AS endTime,
                       s.work_days AS workDays,
                       s.work_hours AS workHours,
                       s.late_tolerance_minutes AS lateToleranceMinutes,
                       s.early_tolerance_minutes AS earlyToleranceMinutes,
                       s.status,
                       s.industry_type AS industryType,
                       s.remark,
                       s.created_time AS createdTime,
                       s.updated_time AS updatedTime
                FROM hr_attendance_shift s
                """ + where + " ORDER BY s.sort_order ASC, s.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @GetMapping("/shifts/options")
    public Result<List<Map<String, Object>>> shiftOptions(@RequestParam(required = false) String keyword) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       shift_name AS shiftName,
                       shift_code AS shiftCode,
                       start_time AS startTime,
                       end_time AS endTime
                FROM hr_attendance_shift
                WHERE deleted = 0 AND status = 'ACTIVE'
                """);
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (shift_name LIKE :keyword OR shift_code LIKE :keyword) ");
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql.append(" ORDER BY sort_order ASC, id DESC ");
        return Result.success(namedParameterJdbcTemplate.queryForList(sql.toString(), params));
    }

    @PostMapping("/shifts")
    public Result<Long> createShift(@RequestBody Map<String, Object> body) {
        String shiftCode = StringUtils.hasText(stringValue(body.get("shiftCode")))
                ? stringValue(body.get("shiftCode"))
                : generateCode("SHIFT", "hr_attendance_shift");

        String sql = """
                INSERT INTO hr_attendance_shift
                    (shift_code, shift_name, start_time, end_time, work_days, work_hours,
                     late_tolerance_minutes, early_tolerance_minutes, status, industry_type, remark, sort_order,
                     created_time, updated_time, deleted)
                VALUES
                    (:shiftCode, :shiftName, :startTime, :endTime, :workDays, :workHours,
                     :lateToleranceMinutes, :earlyToleranceMinutes, :status, :industryType, :remark, :sortOrder,
                     NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shiftCode", shiftCode)
                .addValue("shiftName", body.get("shiftName"))
                .addValue("startTime", body.get("startTime"))
                .addValue("endTime", body.get("endTime"))
                .addValue("workDays", valueOrDefault(body.get("workDays"), "1,2,3,4,5"))
                .addValue("workHours", valueOrDefault(body.get("workHours"), 8))
                .addValue("lateToleranceMinutes", valueOrDefault(body.get("lateToleranceMinutes"), 5))
                .addValue("earlyToleranceMinutes", valueOrDefault(body.get("earlyToleranceMinutes"), 5))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("remark", body.get("remark"))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0)));

        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/shifts/{id}")
    public Result<Boolean> updateShift(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_attendance_shift
                SET shift_name = :shiftName,
                    start_time = :startTime,
                    end_time = :endTime,
                    work_days = :workDays,
                    work_hours = :workHours,
                    late_tolerance_minutes = :lateToleranceMinutes,
                    early_tolerance_minutes = :earlyToleranceMinutes,
                    status = :status,
                    industry_type = :industryType,
                    remark = :remark,
                    sort_order = :sortOrder,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("shiftName", body.get("shiftName"))
                .addValue("startTime", body.get("startTime"))
                .addValue("endTime", body.get("endTime"))
                .addValue("workDays", valueOrDefault(body.get("workDays"), "1,2,3,4,5"))
                .addValue("workHours", valueOrDefault(body.get("workHours"), 8))
                .addValue("lateToleranceMinutes", valueOrDefault(body.get("lateToleranceMinutes"), 5))
                .addValue("earlyToleranceMinutes", valueOrDefault(body.get("earlyToleranceMinutes"), 5))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("remark", body.get("remark"))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0)));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/shifts/{id}")
    public Result<Boolean> deleteShift(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_attendance_shift SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/records/page")
    public Result<PageResult<Map<String, Object>>> pageRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String attendanceDateStart,
            @RequestParam(required = false) String attendanceDateEnd,
            @RequestParam(required = false) String attendanceStatus
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE r.deleted = 0 ");

        if (employeeId != null) {
            where.append(" AND r.employee_id = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (StringUtils.hasText(employeeName)) {
            where.append(" AND e.name LIKE :employeeName ");
            params.addValue("employeeName", "%" + employeeName + "%");
        }
        if (StringUtils.hasText(attendanceDateStart)) {
            where.append(" AND r.attendance_date >= :attendanceDateStart ");
            params.addValue("attendanceDateStart", attendanceDateStart);
        }
        if (StringUtils.hasText(attendanceDateEnd)) {
            where.append(" AND r.attendance_date <= :attendanceDateEnd ");
            params.addValue("attendanceDateEnd", attendanceDateEnd);
        }
        if (StringUtils.hasText(attendanceStatus)) {
            where.append(" AND r.attendance_status = :attendanceStatus ");
            params.addValue("attendanceStatus", attendanceStatus);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_attendance_record r LEFT JOIN hr_employee e ON r.employee_id = e.id " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT r.id,
                       r.record_no AS recordNo,
                       r.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       r.attendance_date AS attendanceDate,
                       r.shift_id AS shiftId,
                       s.shift_name AS shiftName,
                       r.check_in_time AS checkInTime,
                       r.check_out_time AS checkOutTime,
                       r.attendance_status AS attendanceStatus,
                       r.late_minutes AS lateMinutes,
                       r.early_leave_minutes AS earlyLeaveMinutes,
                       r.overtime_minutes AS overtimeMinutes,
                       r.work_hours AS workHours,
                       r.source_type AS sourceType,
                       r.location,
                       r.remark,
                       r.created_time AS createdTime,
                       r.updated_time AS updatedTime
                FROM hr_attendance_record r
                LEFT JOIN hr_employee e ON r.employee_id = e.id
                LEFT JOIN hr_attendance_shift s ON r.shift_id = s.id
                """ + where + " ORDER BY r.attendance_date DESC, r.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @GetMapping("/employees/options")
    public Result<List<Map<String, Object>>> employeeOptions(@RequestParam(required = false) String keyword) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT id, employee_no AS employeeNo, name AS employeeName
                FROM hr_employee
                WHERE deleted = 0 AND employee_status = 1
                """);
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (employee_no LIKE :keyword OR name LIKE :keyword) ");
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql.append(" ORDER BY employee_no ASC LIMIT 200 ");
        return Result.success(namedParameterJdbcTemplate.queryForList(sql.toString(), params));
    }

    @PostMapping("/records")
    public Result<Long> createRecord(@RequestBody Map<String, Object> body) {
        String attendanceDate = StringUtils.hasText(stringValue(body.get("attendanceDate")))
                ? stringValue(body.get("attendanceDate"))
                : LocalDate.now().toString();
        Long shiftId = toLong(body.get("shiftId"));
        AttendanceMetric metric = calculateMetric(
                attendanceDate,
                shiftId,
                stringValue(body.get("checkInTime")),
                stringValue(body.get("checkOutTime"))
        );
        String sql = """
                INSERT INTO hr_attendance_record
                    (record_no, employee_id, attendance_date, shift_id, check_in_time, check_out_time, attendance_status,
                     late_minutes, early_leave_minutes, overtime_minutes, work_hours, source_type, location, remark,
                     created_time, updated_time, deleted)
                VALUES
                    (:recordNo, :employeeId, :attendanceDate, :shiftId, :checkInTime, :checkOutTime, :attendanceStatus,
                     :lateMinutes, :earlyLeaveMinutes, :overtimeMinutes, :workHours, :sourceType, :location, :remark,
                     NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("recordNo", generateCode("AT", "hr_attendance_record"))
                .addValue("employeeId", body.get("employeeId"))
                .addValue("attendanceDate", attendanceDate)
                .addValue("shiftId", shiftId)
                .addValue("checkInTime", body.get("checkInTime"))
                .addValue("checkOutTime", body.get("checkOutTime"))
                .addValue("attendanceStatus", valueOrDefault(body.get("attendanceStatus"), metric.status()))
                .addValue("lateMinutes", valueOrDefault(body.get("lateMinutes"), metric.lateMinutes()))
                .addValue("earlyLeaveMinutes", valueOrDefault(body.get("earlyLeaveMinutes"), metric.earlyLeaveMinutes()))
                .addValue("overtimeMinutes", valueOrDefault(body.get("overtimeMinutes"), metric.overtimeMinutes()))
                .addValue("workHours", valueOrDefault(body.get("workHours"), metric.workHours()))
                .addValue("sourceType", valueOrDefault(body.get("sourceType"), "MANUAL"))
                .addValue("location", body.get("location"))
                .addValue("remark", body.get("remark")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/records/{id}")
    public Result<Boolean> updateRecord(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long shiftId = toLong(body.get("shiftId"));
        String attendanceDate = stringValue(body.get("attendanceDate"));
        if (!StringUtils.hasText(attendanceDate)) {
            attendanceDate = String.valueOf(namedParameterJdbcTemplate.queryForMap(
                    "SELECT attendance_date AS attendanceDate FROM hr_attendance_record WHERE id = :id AND deleted = 0",
                    new MapSqlParameterSource("id", id)
            ).get("attendanceDate"));
        }
        AttendanceMetric metric = calculateMetric(
                attendanceDate,
                shiftId,
                stringValue(body.get("checkInTime")),
                stringValue(body.get("checkOutTime"))
        );
        String sql = """
                UPDATE hr_attendance_record
                SET employee_id = :employeeId,
                    attendance_date = :attendanceDate,
                    shift_id = :shiftId,
                    check_in_time = :checkInTime,
                    check_out_time = :checkOutTime,
                    attendance_status = :attendanceStatus,
                    late_minutes = :lateMinutes,
                    early_leave_minutes = :earlyLeaveMinutes,
                    overtime_minutes = :overtimeMinutes,
                    work_hours = :workHours,
                    source_type = :sourceType,
                    location = :location,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("employeeId", body.get("employeeId"))
                .addValue("attendanceDate", attendanceDate)
                .addValue("shiftId", shiftId)
                .addValue("checkInTime", body.get("checkInTime"))
                .addValue("checkOutTime", body.get("checkOutTime"))
                .addValue("attendanceStatus", valueOrDefault(body.get("attendanceStatus"), metric.status()))
                .addValue("lateMinutes", valueOrDefault(body.get("lateMinutes"), metric.lateMinutes()))
                .addValue("earlyLeaveMinutes", valueOrDefault(body.get("earlyLeaveMinutes"), metric.earlyLeaveMinutes()))
                .addValue("overtimeMinutes", valueOrDefault(body.get("overtimeMinutes"), metric.overtimeMinutes()))
                .addValue("workHours", valueOrDefault(body.get("workHours"), metric.workHours()))
                .addValue("sourceType", valueOrDefault(body.get("sourceType"), "MANUAL"))
                .addValue("location", body.get("location"))
                .addValue("remark", body.get("remark")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/records/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_attendance_record SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/appeals/page")
    public Result<PageResult<Map<String, Object>>> pageAppeals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE a.deleted = 0 ");
        if (employeeId != null) {
            where.append(" AND a.employee_id = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND a.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_attendance_appeal a " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT a.id,
                       a.appeal_no AS appealNo,
                       a.record_id AS recordId,
                       a.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       a.appeal_type AS appealType,
                       a.requested_check_in_time AS requestedCheckInTime,
                       a.requested_check_out_time AS requestedCheckOutTime,
                       a.reason,
                       a.status,
                       a.approver_id AS approverId,
                       a.approve_time AS approveTime,
                       a.approve_remark AS approveRemark,
                       a.created_time AS createdTime
                FROM hr_attendance_appeal a
                LEFT JOIN hr_employee e ON a.employee_id = e.id
                """ + where + " ORDER BY a.id DESC LIMIT :limit OFFSET :offset";
        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @PostMapping("/appeals")
    public Result<Long> createAppeal(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_attendance_appeal
                    (appeal_no, record_id, employee_id, appeal_type, requested_check_in_time, requested_check_out_time,
                     reason, status, created_time, updated_time, deleted)
                VALUES
                    (:appealNo, :recordId, :employeeId, :appealType, :requestedCheckInTime, :requestedCheckOutTime,
                     :reason, 'PENDING', NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("appealNo", generateCode("AP", "hr_attendance_appeal"))
                .addValue("recordId", body.get("recordId"))
                .addValue("employeeId", body.get("employeeId"))
                .addValue("appealType", valueOrDefault(body.get("appealType"), "BOTH"))
                .addValue("requestedCheckInTime", body.get("requestedCheckInTime"))
                .addValue("requestedCheckOutTime", body.get("requestedCheckOutTime"))
                .addValue("reason", body.get("reason")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/appeals/{id}/status")
    public Result<Boolean> updateAppealStatus(
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

        int rows = namedParameterJdbcTemplate.update("""
                        UPDATE hr_attendance_appeal
                        SET status = :status,
                            approver_id = :approverId,
                            approve_time = NOW(),
                            approve_remark = :approveRemark,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("status", finalStatus)
                        .addValue("approverId", body == null ? null : body.get("approverId"))
                        .addValue("approveRemark", body == null ? null : body.get("approveRemark"))
        );
        return Result.success(rows > 0);
    }

    @DeleteMapping("/appeals/{id}")
    public Result<Boolean> deleteAppeal(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_attendance_appeal SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/statistics/monthly")
    public Result<List<Map<String, Object>>> monthlyStatistics(@RequestParam(required = false) String month) {
        String targetMonth = StringUtils.hasText(month) ? month : YearMonth.now().toString();
        String sql = """
                SELECT r.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       COUNT(*) AS totalDays,
                       SUM(CASE WHEN r.attendance_status IN ('NORMAL','LATE','EARLY_LEAVE','LATE_EARLY') THEN 1 ELSE 0 END) AS attendedDays,
                       SUM(CASE WHEN r.attendance_status IN ('LATE','LATE_EARLY') THEN 1 ELSE 0 END) AS lateDays,
                       SUM(CASE WHEN r.attendance_status IN ('EARLY_LEAVE','LATE_EARLY') THEN 1 ELSE 0 END) AS earlyLeaveDays,
                       SUM(CASE WHEN r.attendance_status = 'ABSENT' THEN 1 ELSE 0 END) AS absentDays,
                       ROUND(SUM(IFNULL(r.work_hours, 0)), 2) AS totalWorkHours,
                       ROUND(SUM(IFNULL(r.overtime_minutes, 0)) / 60, 2) AS overtimeHours
                FROM hr_attendance_record r
                LEFT JOIN hr_employee e ON r.employee_id = e.id
                WHERE r.deleted = 0 AND DATE_FORMAT(r.attendance_date, '%Y-%m') = :month
                GROUP BY r.employee_id, e.employee_no, e.name
                ORDER BY e.employee_no ASC
                """;
        return Result.success(namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource("month", targetMonth)));
    }

    private AttendanceMetric calculateMetric(String attendanceDate, Long shiftId, String checkInText, String checkOutText) {
        LocalDate date = LocalDate.parse(attendanceDate.substring(0, 10));
        LocalDateTime checkIn = parseDateTime(checkInText);
        LocalDateTime checkOut = parseDateTime(checkOutText);

        if (checkIn == null && checkOut == null) {
            return new AttendanceMetric("ABSENT", 0, 0, 0, 0D);
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (shiftId != null) {
            List<Map<String, Object>> shifts = namedParameterJdbcTemplate.queryForList(
                    "SELECT start_time AS startTime, end_time AS endTime FROM hr_attendance_shift WHERE id = :id AND deleted = 0",
                    new MapSqlParameterSource("id", shiftId)
            );
            if (!shifts.isEmpty()) {
                Map<String, Object> shift = shifts.get(0);
                LocalTime startTime = toLocalTime(shift.get("startTime"));
                LocalTime endTime = toLocalTime(shift.get("endTime"));
                if (startTime != null) {
                    start = LocalDateTime.of(date, startTime);
                }
                if (endTime != null) {
                    end = LocalDateTime.of(date, endTime);
                    if (startTime != null && endTime.isBefore(startTime)) {
                        end = end.plusDays(1);
                    }
                }
            }
        }

        int late = 0;
        int early = 0;
        int overtime = 0;
        if (start != null && checkIn != null && checkIn.isAfter(start)) {
            late = (int) Duration.between(start, checkIn).toMinutes();
        }
        if (end != null && checkOut != null) {
            if (checkOut.isBefore(end)) {
                early = (int) Duration.between(checkOut, end).toMinutes();
            } else if (checkOut.isAfter(end)) {
                overtime = (int) Duration.between(end, checkOut).toMinutes();
            }
        }
        double workHours = 0D;
        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            workHours = Math.round((Duration.between(checkIn, checkOut).toMinutes() / 60.0) * 100) / 100.0;
        }

        String status;
        if (late > 0 && early > 0) {
            status = "LATE_EARLY";
        } else if (late > 0) {
            status = "LATE";
        } else if (early > 0) {
            status = "EARLY_LEAVE";
        } else {
            status = "NORMAL";
        }
        return new AttendanceMetric(status, late, early, overtime, workHours);
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

    private LocalTime toLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Time time) {
            return time.toLocalTime();
        }
        String text = String.valueOf(value);
        return text.length() > 8 ? LocalTime.parse(text.substring(0, 8)) : LocalTime.parse(text);
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

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    private record AttendanceMetric(String status, Integer lateMinutes, Integer earlyLeaveMinutes, Integer overtimeMinutes, Double workHours) {
    }
}
