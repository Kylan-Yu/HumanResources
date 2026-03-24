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
 * Recruit requirement API aligned with frontend path `/recruit-requirements`.
 */
@RestController
@RequestMapping("/recruit-requirements")
@RequiredArgsConstructor
public class RecruitRequirementController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String requirementStatus,
            @RequestParam(required = false) String urgencyLevel,
            @RequestParam(required = false) String industryType,
            @RequestParam(required = false) String expectedEntryDateBegin,
            @RequestParam(required = false) String expectedEntryDateEnd
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE r.deleted = 0 ");

        if (StringUtils.hasText(title)) {
            where.append(" AND r.title LIKE :title ");
            params.addValue("title", "%" + title + "%");
        }
        if (orgId != null) {
            where.append(" AND r.org_id = :orgId ");
            params.addValue("orgId", orgId);
        }
        if (deptId != null) {
            where.append(" AND r.dept_id = :deptId ");
            params.addValue("deptId", deptId);
        }
        if (positionId != null) {
            where.append(" AND r.position_id = :positionId ");
            params.addValue("positionId", positionId);
        }
        if (StringUtils.hasText(requirementStatus)) {
            where.append(" AND r.requirement_status = :requirementStatus ");
            params.addValue("requirementStatus", requirementStatus);
        }
        if (StringUtils.hasText(urgencyLevel)) {
            where.append(" AND r.urgency_level = :urgencyLevel ");
            params.addValue("urgencyLevel", urgencyLevel);
        }
        if (StringUtils.hasText(industryType)) {
            where.append(" AND r.industry_type = :industryType ");
            params.addValue("industryType", industryType);
        }
        if (StringUtils.hasText(expectedEntryDateBegin)) {
            where.append(" AND r.expected_entry_date >= :expectedEntryDateBegin ");
            params.addValue("expectedEntryDateBegin", expectedEntryDateBegin);
        }
        if (StringUtils.hasText(expectedEntryDateEnd)) {
            where.append(" AND r.expected_entry_date <= :expectedEntryDateEnd ");
            params.addValue("expectedEntryDateEnd", expectedEntryDateEnd);
        }

        String countSql = "SELECT COUNT(*) FROM hr_recruit_requirement r " + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT r.id,
                       r.requirement_no AS requirementNo,
                       r.title,
                       r.org_id AS orgId,
                       o.org_name AS orgName,
                       r.dept_id AS deptId,
                       d.dept_name AS deptName,
                       r.position_id AS positionId,
                       p.position_name AS positionName,
                       r.headcount,
                       r.urgency_level AS urgencyLevel,
                       CASE r.urgency_level
                           WHEN 'HIGH' THEN '高'
                           WHEN 'MEDIUM' THEN '中'
                           WHEN 'LOW' THEN '低'
                           ELSE r.urgency_level END AS urgencyLevelDesc,
                       r.requirement_status AS requirementStatus,
                       CASE r.requirement_status
                           WHEN 'DRAFT' THEN '草稿'
                           WHEN 'OPEN' THEN '开放'
                           WHEN 'CLOSED' THEN '关闭'
                           WHEN 'CANCELLED' THEN '取消'
                           ELSE r.requirement_status END AS requirementStatusDesc,
                       r.expected_entry_date AS expectedEntryDate,
                       r.reason,
                       r.industry_type AS industryType,
                       CASE r.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       r.ext_json AS extJson,
                       r.remark,
                       r.created_time AS createdTime,
                       r.updated_time AS updatedTime
                FROM hr_recruit_requirement r
                LEFT JOIN hr_org o ON r.org_id = o.id
                LEFT JOIN hr_dept d ON r.dept_id = d.id
                LEFT JOIN hr_position p ON r.position_id = p.id
                """ + where + " ORDER BY r.created_time DESC, r.id DESC LIMIT :limit OFFSET :offset";

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
                SELECT r.id,
                       r.requirement_no AS requirementNo,
                       r.title,
                       r.org_id AS orgId,
                       o.org_name AS orgName,
                       r.dept_id AS deptId,
                       d.dept_name AS deptName,
                       r.position_id AS positionId,
                       p.position_name AS positionName,
                       r.headcount,
                       r.urgency_level AS urgencyLevel,
                       CASE r.urgency_level
                           WHEN 'HIGH' THEN '高'
                           WHEN 'MEDIUM' THEN '中'
                           WHEN 'LOW' THEN '低'
                           ELSE r.urgency_level END AS urgencyLevelDesc,
                       r.requirement_status AS requirementStatus,
                       CASE r.requirement_status
                           WHEN 'DRAFT' THEN '草稿'
                           WHEN 'OPEN' THEN '开放'
                           WHEN 'CLOSED' THEN '关闭'
                           WHEN 'CANCELLED' THEN '取消'
                           ELSE r.requirement_status END AS requirementStatusDesc,
                       r.expected_entry_date AS expectedEntryDate,
                       r.reason,
                       r.industry_type AS industryType,
                       CASE r.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       r.ext_json AS extJson,
                       r.remark,
                       r.created_time AS createdTime,
                       r.updated_time AS updatedTime
                FROM hr_recruit_requirement r
                LEFT JOIN hr_org o ON r.org_id = o.id
                LEFT JOIN hr_dept d ON r.dept_id = d.id
                LEFT JOIN hr_position p ON r.position_id = p.id
                WHERE r.id = :id AND r.deleted = 0
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id)));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        String requirementNo = generateRequirementNo(String.valueOf(valueOrDefault(body.get("industryType"), "company")));
        String sql = """
                INSERT INTO hr_recruit_requirement(requirement_no, title, org_id, dept_id, position_id, headcount, urgency_level, requirement_status,
                                                   expected_entry_date, reason, industry_type, ext_json, remark, created_time, updated_time, deleted)
                VALUES(:requirementNo, :title, :orgId, :deptId, :positionId, :headcount, :urgencyLevel, :requirementStatus,
                       :expectedEntryDate, :reason, :industryType, :extJson, :remark, NOW(), NOW(), 0)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("requirementNo", requirementNo)
                .addValue("title", body.get("title"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("positionId", body.get("positionId"))
                .addValue("headcount", valueOrDefault(body.get("headcount"), 1))
                .addValue("urgencyLevel", valueOrDefault(body.get("urgencyLevel"), "MEDIUM"))
                .addValue("requirementStatus", valueOrDefault(body.get("requirementStatus"), "DRAFT"))
                .addValue("expectedEntryDate", body.get("expectedEntryDate"))
                .addValue("reason", body.get("reason"))
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
                UPDATE hr_recruit_requirement
                SET title = :title,
                    org_id = :orgId,
                    dept_id = :deptId,
                    position_id = :positionId,
                    headcount = :headcount,
                    urgency_level = :urgencyLevel,
                    expected_entry_date = :expectedEntryDate,
                    reason = :reason,
                    industry_type = :industryType,
                    ext_json = :extJson,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("title", body.get("title"))
                .addValue("orgId", body.get("orgId"))
                .addValue("deptId", body.get("deptId"))
                .addValue("positionId", body.get("positionId"))
                .addValue("headcount", valueOrDefault(body.get("headcount"), 1))
                .addValue("urgencyLevel", valueOrDefault(body.get("urgencyLevel"), "MEDIUM"))
                .addValue("expectedEntryDate", body.get("expectedEntryDate"))
                .addValue("reason", body.get("reason"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_recruit_requirement SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
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
                "UPDATE hr_recruit_requirement SET requirement_status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", finalStatus)
        );
        return Result.success(rows > 0);
    }

    private String generateRequirementNo(String industryType) {
        String prefix = "hospital".equalsIgnoreCase(industryType) ? "HREQ" : "REQ";
        Long seq = namedParameterJdbcTemplate.queryForObject("""
                SELECT COUNT(*) + 1 FROM hr_recruit_requirement
                WHERE DATE(created_time) = CURDATE() AND deleted = 0
                """, new MapSqlParameterSource(), Long.class);
        if (seq == null) {
            seq = 1L;
        }
        return prefix + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + String.format("%03d", seq);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

