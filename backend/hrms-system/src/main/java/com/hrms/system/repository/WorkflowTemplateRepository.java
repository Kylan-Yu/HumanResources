package com.hrms.system.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Workflow template data access.
 */
@Repository
@RequiredArgsConstructor
public class WorkflowTemplateRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Map<String, Object>> queryForList(String sql, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.queryForList(sql, params == null ? new MapSqlParameterSource() : params);
    }

    public <T> List<T> queryForList(String sql, MapSqlParameterSource params, Class<T> elementType) {
        return namedParameterJdbcTemplate.queryForList(
                sql,
                params == null ? new MapSqlParameterSource() : params,
                elementType
        );
    }

    public <T> T queryForObject(String sql, MapSqlParameterSource params, Class<T> requiredType) {
        return namedParameterJdbcTemplate.queryForObject(sql, params == null ? new MapSqlParameterSource() : params, requiredType);
    }

    public int update(String sql, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.update(sql, params == null ? new MapSqlParameterSource() : params);
    }

    public JdbcOperations getJdbcOperations() {
        return namedParameterJdbcTemplate.getJdbcOperations();
    }
}
