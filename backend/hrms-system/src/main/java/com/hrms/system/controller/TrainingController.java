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
 * Training management APIs.
 */
@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/courses/page")
    public Result<PageResult<Map<String, Object>>> pageCourses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE c.deleted = 0 ");

        if (StringUtils.hasText(courseName)) {
            where.append(" AND c.course_name LIKE :courseName ");
            params.addValue("courseName", "%" + courseName + "%");
        }
        if (StringUtils.hasText(courseType)) {
            where.append(" AND c.course_type = :courseType ");
            params.addValue("courseType", courseType);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND c.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_training_course c " + where, params, Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT c.id,
                       c.course_code AS courseCode,
                       c.course_name AS courseName,
                       c.course_type AS courseType,
                       c.lecturer,
                       c.duration_hours AS durationHours,
                       c.description,
                       c.status,
                       c.industry_type AS industryType,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_training_course c
                """ + where + " ORDER BY c.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @PostMapping("/courses")
    public Result<Long> createCourse(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_training_course
                    (course_code, course_name, course_type, lecturer, duration_hours, description, status, industry_type, created_time, updated_time, deleted)
                VALUES
                    (:courseCode, :courseName, :courseType, :lecturer, :durationHours, :description, :status, :industryType, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("courseCode", body.get("courseCode"))
                .addValue("courseName", body.get("courseName"))
                .addValue("courseType", valueOrDefault(body.get("courseType"), "GENERAL"))
                .addValue("lecturer", body.get("lecturer"))
                .addValue("durationHours", body.get("durationHours"))
                .addValue("description", body.get("description"))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/courses/{id}")
    public Result<Boolean> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_training_course
                SET course_name = :courseName,
                    course_type = :courseType,
                    lecturer = :lecturer,
                    duration_hours = :durationHours,
                    description = :description,
                    status = :status,
                    industry_type = :industryType,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("courseName", body.get("courseName"))
                .addValue("courseType", valueOrDefault(body.get("courseType"), "GENERAL"))
                .addValue("lecturer", body.get("lecturer"))
                .addValue("durationHours", body.get("durationHours"))
                .addValue("description", body.get("description"))
                .addValue("status", valueOrDefault(body.get("status"), "ACTIVE"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/courses/{id}")
    public Result<Boolean> deleteCourse(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_training_course SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/sessions/page")
    public Result<PageResult<Map<String, Object>>> pageSessions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String sessionName,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE s.deleted = 0 ");
        if (courseId != null) {
            where.append(" AND s.course_id = :courseId ");
            params.addValue("courseId", courseId);
        }
        if (StringUtils.hasText(sessionName)) {
            where.append(" AND s.session_name LIKE :sessionName ");
            params.addValue("sessionName", "%" + sessionName + "%");
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND s.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_training_session s " + where, params, Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT s.id,
                       s.course_id AS courseId,
                       c.course_name AS courseName,
                       s.session_name AS sessionName,
                       s.start_time AS startTime,
                       s.end_time AS endTime,
                       s.location,
                       s.capacity,
                       s.status,
                       s.industry_type AS industryType,
                       s.created_time AS createdTime,
                       s.updated_time AS updatedTime
                FROM hr_training_session s
                LEFT JOIN hr_training_course c ON s.course_id = c.id
                """ + where + " ORDER BY s.start_time DESC, s.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @PostMapping("/sessions")
    public Result<Long> createSession(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_training_session
                    (course_id, session_name, start_time, end_time, location, capacity, status, industry_type, created_time, updated_time, deleted)
                VALUES
                    (:courseId, :sessionName, :startTime, :endTime, :location, :capacity, :status, :industryType, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("courseId", body.get("courseId"))
                .addValue("sessionName", body.get("sessionName"))
                .addValue("startTime", body.get("startTime"))
                .addValue("endTime", body.get("endTime"))
                .addValue("location", body.get("location"))
                .addValue("capacity", body.get("capacity"))
                .addValue("status", valueOrDefault(body.get("status"), "PLANNED"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/sessions/{id}")
    public Result<Boolean> updateSession(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_training_session
                SET course_id = :courseId,
                    session_name = :sessionName,
                    start_time = :startTime,
                    end_time = :endTime,
                    location = :location,
                    capacity = :capacity,
                    status = :status,
                    industry_type = :industryType,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("courseId", body.get("courseId"))
                .addValue("sessionName", body.get("sessionName"))
                .addValue("startTime", body.get("startTime"))
                .addValue("endTime", body.get("endTime"))
                .addValue("location", body.get("location"))
                .addValue("capacity", body.get("capacity"))
                .addValue("status", valueOrDefault(body.get("status"), "PLANNED"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/sessions/{id}")
    public Result<Boolean> deleteSession(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_training_session SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @GetMapping("/enrollments/page")
    public Result<PageResult<Map<String, Object>>> pageEnrollments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long employeeId
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE e.deleted = 0 ");
        if (sessionId != null) {
            where.append(" AND e.session_id = :sessionId ");
            params.addValue("sessionId", sessionId);
        }
        if (employeeId != null) {
            where.append(" AND e.employee_id = :employeeId ");
            params.addValue("employeeId", employeeId);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_training_enrollment e " + where, params, Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT e.id,
                       e.session_id AS sessionId,
                       s.session_name AS sessionName,
                       e.employee_id AS employeeId,
                       emp.employee_no AS employeeNo,
                       emp.name AS employeeName,
                       e.attendance_status AS attendanceStatus,
                       e.score,
                       e.feedback,
                       e.created_time AS createdTime,
                       e.updated_time AS updatedTime
                FROM hr_training_enrollment e
                LEFT JOIN hr_training_session s ON e.session_id = s.id
                LEFT JOIN hr_employee emp ON e.employee_id = emp.id
                """ + where + " ORDER BY e.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize
        ));
    }

    @PostMapping("/enrollments")
    public Result<Long> createEnrollment(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_training_enrollment
                    (session_id, employee_id, attendance_status, score, feedback, created_time, updated_time, deleted)
                VALUES
                    (:sessionId, :employeeId, :attendanceStatus, :score, :feedback, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("sessionId", body.get("sessionId"))
                .addValue("employeeId", body.get("employeeId"))
                .addValue("attendanceStatus", valueOrDefault(body.get("attendanceStatus"), "REGISTERED"))
                .addValue("score", body.get("score"))
                .addValue("feedback", body.get("feedback")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/enrollments/{id}")
    public Result<Boolean> updateEnrollment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_training_enrollment
                SET attendance_status = :attendanceStatus,
                    score = :score,
                    feedback = :feedback,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("attendanceStatus", valueOrDefault(body.get("attendanceStatus"), "REGISTERED"))
                .addValue("score", body.get("score"))
                .addValue("feedback", body.get("feedback")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/enrollments/{id}")
    public Result<Boolean> deleteEnrollment(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_training_enrollment SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

