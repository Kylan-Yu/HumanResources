package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Employee archive APIs aligned with `/employees`.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeArchiveController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) Integer employeeStatus,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String industryType
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE e.deleted = 0 ");

        if (StringUtils.hasText(employeeNo)) {
            where.append(" AND e.employee_no LIKE :employeeNo ");
            params.addValue("employeeNo", "%" + employeeNo + "%");
        }
        if (StringUtils.hasText(name)) {
            where.append(" AND e.name LIKE :name ");
            params.addValue("name", "%" + name + "%");
        }
        if (StringUtils.hasText(mobile)) {
            where.append(" AND e.mobile LIKE :mobile ");
            params.addValue("mobile", "%" + mobile + "%");
        }
        if (employeeStatus != null) {
            where.append(" AND e.employee_status = :employeeStatus ");
            params.addValue("employeeStatus", employeeStatus);
        }
        if (orgId != null) {
            where.append(" AND j.org_id = :orgId ");
            params.addValue("orgId", orgId);
        }
        if (deptId != null) {
            where.append(" AND j.dept_id = :deptId ");
            params.addValue("deptId", deptId);
        }
        if (StringUtils.hasText(industryType)) {
            where.append(" AND e.industry_type = :industryType ");
            params.addValue("industryType", industryType);
        }

        String countSql = """
                SELECT COUNT(*)
                FROM hr_employee e
                LEFT JOIN hr_employee_job j ON e.id = j.employee_id AND j.is_main_job = 1 AND j.deleted = 0
                """ + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT e.id,
                       e.employee_no AS employeeNo,
                       e.name,
                       e.gender,
                       CASE e.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '' END AS genderDesc,
                       e.birthday,
                       TIMESTAMPDIFF(YEAR, e.birthday, CURDATE()) AS age,
                       e.id_card_no AS idCardNo,
                       e.mobile,
                       e.email,
                       e.marital_status AS maritalStatus,
                       e.nationality,
                       e.domicile_address AS domicileAddress,
                       e.current_address AS currentAddress,
                       e.employee_status AS employeeStatus,
                       CASE e.employee_status WHEN 1 THEN '在职' WHEN 2 THEN '离职' WHEN 3 THEN '退休' ELSE '' END AS employeeStatusDesc,
                       e.industry_type AS industryType,
                       CASE e.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       e.ext_json AS extJson,
                       e.remark,
                       e.created_time AS createdTime,
                       e.updated_time AS updatedTime,
                       j.id AS jobId,
                       j.org_id AS orgId,
                       o.org_name AS orgName,
                       j.dept_id AS deptId,
                       d.dept_name AS deptName,
                       j.position_id AS positionId,
                       p.position_name AS positionName,
                       j.rank_id AS rankId,
                       r.rank_name AS rankName
                FROM hr_employee e
                LEFT JOIN hr_employee_job j ON e.id = j.employee_id AND j.is_main_job = 1 AND j.deleted = 0
                LEFT JOIN hr_org o ON j.org_id = o.id
                LEFT JOIN hr_dept d ON j.dept_id = d.id
                LEFT JOIN hr_position p ON j.position_id = p.id
                LEFT JOIN hr_rank r ON j.rank_id = r.id
                """ + where + " ORDER BY e.created_time DESC, e.id DESC LIMIT :limit OFFSET :offset";

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
                SELECT e.id,
                       e.employee_no AS employeeNo,
                       e.name,
                       e.gender,
                       CASE e.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '' END AS genderDesc,
                       e.birthday,
                       TIMESTAMPDIFF(YEAR, e.birthday, CURDATE()) AS age,
                       e.id_card_no AS idCardNo,
                       e.mobile,
                       e.email,
                       e.marital_status AS maritalStatus,
                       e.nationality,
                       e.domicile_address AS domicileAddress,
                       e.current_address AS currentAddress,
                       e.employee_status AS employeeStatus,
                       CASE e.employee_status WHEN 1 THEN '在职' WHEN 2 THEN '离职' WHEN 3 THEN '退休' ELSE '' END AS employeeStatusDesc,
                       e.industry_type AS industryType,
                       CASE e.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       e.ext_json AS extJson,
                       e.remark,
                       e.created_time AS createdTime,
                       e.updated_time AS updatedTime
                FROM hr_employee e
                WHERE e.id = :id AND e.deleted = 0
                """;
        Map<String, Object> employee = namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id));

        String jobSql = """
                SELECT j.id,
                       j.employee_id AS employeeId,
                       j.org_id AS orgId,
                       o.org_name AS orgName,
                       j.dept_id AS deptId,
                       d.dept_name AS deptName,
                       j.position_id AS positionId,
                       p.position_name AS positionName,
                       j.rank_id AS rankId,
                       r.rank_name AS rankName,
                       j.leader_id AS leaderId,
                       l.name AS leaderName,
                       j.employee_type AS employeeType,
                       j.employment_type AS employmentType,
                       j.entry_date AS entryDate,
                       j.regular_date AS regularDate,
                       j.work_location AS workLocation,
                       j.is_main_job AS isMainJob,
                       j.status
                FROM hr_employee_job j
                LEFT JOIN hr_org o ON j.org_id = o.id
                LEFT JOIN hr_dept d ON j.dept_id = d.id
                LEFT JOIN hr_position p ON j.position_id = p.id
                LEFT JOIN hr_rank r ON j.rank_id = r.id
                LEFT JOIN hr_employee l ON j.leader_id = l.id
                WHERE j.employee_id = :id AND j.deleted = 0
                ORDER BY j.is_main_job DESC, j.id DESC
                """;
        var jobs = namedParameterJdbcTemplate.queryForList(jobSql, new MapSqlParameterSource("id", id));
        employee.put("jobs", jobs);
        if (!jobs.isEmpty()) {
            employee.put("mainJob", jobs.get(0));
        }
        return Result.success(employee);
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        String employeeNo = generateEmployeeNo();
        String sql = """
                INSERT INTO hr_employee
                    (employee_no, name, gender, birthday, id_card_no, mobile, email, marital_status, nationality, domicile_address, current_address,
                     employee_status, industry_type, ext_json, remark, created_time, updated_time, deleted)
                VALUES
                    (:employeeNo, :name, :gender, :birthday, :idCardNo, :mobile, :email, :maritalStatus, :nationality, :domicileAddress, :currentAddress,
                     1, :industryType, :extJson, :remark, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("employeeNo", employeeNo)
                .addValue("name", body.get("name"))
                .addValue("gender", valueOrDefault(body.get("gender"), 1))
                .addValue("birthday", body.get("birthday"))
                .addValue("idCardNo", body.get("idCardNo"))
                .addValue("mobile", body.get("mobile"))
                .addValue("email", body.get("email"))
                .addValue("maritalStatus", body.get("maritalStatus"))
                .addValue("nationality", body.get("nationality"))
                .addValue("domicileAddress", body.get("domicileAddress"))
                .addValue("currentAddress", body.get("currentAddress"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);

        Object jobObj = body.get("jobInfo");
        if (id != null && jobObj instanceof Map<?, ?> job) {
            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_employee_job
                                (employee_id, org_id, dept_id, position_id, rank_id, leader_id, employee_type, employment_type, entry_date, regular_date, work_location, is_main_job, status, created_time, updated_time, deleted)
                            VALUES
                                (:employeeId, :orgId, :deptId, :positionId, :rankId, :leaderId, :employeeType, :employmentType, :entryDate, :regularDate, :workLocation, :isMainJob, 1, NOW(), NOW(), 0)
                            """,
                    new MapSqlParameterSource()
                            .addValue("employeeId", id)
                            .addValue("orgId", job.get("orgId"))
                            .addValue("deptId", job.get("deptId"))
                            .addValue("positionId", job.get("positionId"))
                            .addValue("rankId", job.get("rankId"))
                            .addValue("leaderId", job.get("leaderId"))
                            .addValue("employeeType", valueOrDefault(job.get("employeeType"), "formal"))
                            .addValue("employmentType", valueOrDefault(job.get("employmentType"), "fulltime"))
                            .addValue("entryDate", job.get("entryDate"))
                            .addValue("regularDate", job.get("regularDate"))
                            .addValue("workLocation", job.get("workLocation"))
                            .addValue("isMainJob", valueOrDefault(job.get("isMainJob"), 1)));
        }
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_employee
                SET name = :name,
                    gender = :gender,
                    birthday = :birthday,
                    id_card_no = :idCardNo,
                    mobile = :mobile,
                    email = :email,
                    marital_status = :maritalStatus,
                    nationality = :nationality,
                    domicile_address = :domicileAddress,
                    current_address = :currentAddress,
                    employee_status = :employeeStatus,
                    industry_type = :industryType,
                    ext_json = :extJson,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", body.get("name"))
                .addValue("gender", valueOrDefault(body.get("gender"), 1))
                .addValue("birthday", body.get("birthday"))
                .addValue("idCardNo", body.get("idCardNo"))
                .addValue("mobile", body.get("mobile"))
                .addValue("email", body.get("email"))
                .addValue("maritalStatus", body.get("maritalStatus"))
                .addValue("nationality", body.get("nationality"))
                .addValue("domicileAddress", body.get("domicileAddress"))
                .addValue("currentAddress", body.get("currentAddress"))
                .addValue("employeeStatus", valueOrDefault(body.get("employeeStatus"), 1))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        int rows1 = namedParameterJdbcTemplate.update(
                "UPDATE hr_employee SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                params
        );
        namedParameterJdbcTemplate.update(
                "UPDATE hr_employee_job SET deleted = 1, updated_time = NOW() WHERE employee_id = :id AND deleted = 0",
                params
        );
        return Result.success(rows1 > 0);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Integer finalStatus = status;
        if (finalStatus == null && body != null && body.get("status") != null) {
            finalStatus = Integer.valueOf(String.valueOf(body.get("status")));
        }
        if (finalStatus == null) {
            return Result.error("状态不能为空");
        }
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_employee SET employee_status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", finalStatus)
        );
        return Result.success(rows > 0);
    }

    private String generateEmployeeNo() {
        String prefix = "EMP" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Long count = namedParameterJdbcTemplate.queryForObject("""
                SELECT COUNT(*) + 1
                FROM hr_employee
                WHERE employee_no LIKE :prefix
                """, new MapSqlParameterSource("prefix", prefix + "%"), Long.class);
        if (count == null) {
            count = 1L;
        }
        return prefix + String.format("%04d", count);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

