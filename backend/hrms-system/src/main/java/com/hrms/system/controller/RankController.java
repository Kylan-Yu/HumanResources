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
 * Rank management controller.
 */
@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> pageRanks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String rankName,
            @RequestParam(required = false) String rankCode,
            @RequestParam(required = false) String rankSeries,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String industryType
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE deleted = 0 ");

        if (StringUtils.hasText(rankName)) {
            where.append(" AND rank_name LIKE :rankName ");
            params.addValue("rankName", "%" + rankName + "%");
        }
        if (StringUtils.hasText(rankCode)) {
            where.append(" AND rank_code LIKE :rankCode ");
            params.addValue("rankCode", "%" + rankCode + "%");
        }
        if (StringUtils.hasText(rankSeries)) {
            where.append(" AND rank_series = :rankSeries ");
            params.addValue("rankSeries", rankSeries);
        }
        if (status != null) {
            where.append(" AND status = :status ");
            params.addValue("status", status);
        }
        if (StringUtils.hasText(industryType)) {
            where.append(" AND industry_type = :industryType ");
            params.addValue("industryType", industryType);
        }

        String countSql = "SELECT COUNT(*) FROM hr_rank " + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT id, rank_code AS rankCode, rank_name AS rankName, rank_series AS rankSeries,
                       rank_level AS rankLevel, description, status, sort_order AS sortOrder,
                       industry_type AS industryType, ext_json AS extJson, created_time AS createdTime, updated_time AS updatedTime
                FROM hr_rank
                """ + where + " ORDER BY sort_order ASC, id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(dataSql, params),
                total,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/list")
    public Result<Object> listRanks(@RequestParam(required = false) Integer status) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT id, rank_code AS rankCode, rank_name AS rankName, rank_series AS rankSeries,
                       rank_level AS rankLevel, description, status, sort_order AS sortOrder,
                       industry_type AS industryType, ext_json AS extJson, created_time AS createdTime, updated_time AS updatedTime
                FROM hr_rank WHERE deleted = 0
                """);
        if (status != null) {
            sql.append(" AND status = :status");
            params.addValue("status", status);
        }
        sql.append(" ORDER BY sort_order ASC, id ASC");
        return Result.success(namedParameterJdbcTemplate.queryForList(sql.toString(), params));
    }

    @GetMapping("/{id}")
    public Result<Object> getRankById(@PathVariable Long id) {
        String sql = """
                SELECT id, rank_code AS rankCode, rank_name AS rankName, rank_series AS rankSeries,
                       rank_level AS rankLevel, description, status, sort_order AS sortOrder,
                       industry_type AS industryType, ext_json AS extJson, created_time AS createdTime, updated_time AS updatedTime
                FROM hr_rank WHERE id = :id AND deleted = 0
                """;
        return Result.success(namedParameterJdbcTemplate.queryForMap(sql, new MapSqlParameterSource("id", id)));
    }

    @PostMapping
    public Result<Long> createRank(@RequestBody Map<String, Object> body) {
        String sql = """
                INSERT INTO hr_rank(rank_code, rank_name, rank_series, rank_level, description, status, sort_order, industry_type, ext_json, created_time, updated_time, deleted)
                VALUES(:rankCode, :rankName, :rankSeries, :rankLevel, :description, :status, :sortOrder, :industryType, :extJson, NOW(), NOW(), 0)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("rankCode", body.get("rankCode"))
                .addValue("rankName", body.get("rankName"))
                .addValue("rankSeries", body.get("rankSeries"))
                .addValue("rankLevel", valueOrDefault(body.get("rankLevel"), 1))
                .addValue("description", body.get("description"))
                .addValue("status", valueOrDefault(body.get("status"), 1))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"));
        namedParameterJdbcTemplate.update(sql, params);
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateRank(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_rank
                SET rank_name = :rankName,
                    rank_series = :rankSeries,
                    rank_level = :rankLevel,
                    description = :description,
                    status = :status,
                    sort_order = :sortOrder,
                    industry_type = :industryType,
                    ext_json = :extJson,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("rankName", body.get("rankName"))
                .addValue("rankSeries", body.get("rankSeries"))
                .addValue("rankLevel", valueOrDefault(body.get("rankLevel"), 1))
                .addValue("description", body.get("description"))
                .addValue("status", valueOrDefault(body.get("status"), 1))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson")));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRank(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_rank SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateRankStatus(@PathVariable Long id, @RequestParam Integer status) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_rank SET status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", status)
        );
        return Result.success(rows > 0);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}

