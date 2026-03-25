package com.hrms.system.controller;

import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User custom field definition APIs.
 */
@RestController
@RequestMapping("/users/custom-fields")
@RequiredArgsConstructor
public class UserCustomFieldController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String industryType,
            @RequestParam(required = false) Integer status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       field_key AS fieldKey,
                       field_name AS fieldName,
                       field_type AS fieldType,
                       required_flag AS requiredFlag,
                       placeholder,
                       options_json AS optionsJson,
                       default_value AS defaultValue,
                       industry_type AS industryType,
                       sort_order AS sortOrder,
                       status,
                       created_time AS createdTime,
                       updated_time AS updatedTime
                FROM sys_user_custom_field
                WHERE deleted = 0
                """);

        if (StringUtils.hasText(industryType)) {
            sql.append(" AND (industry_type = :industryType OR industry_type IS NULL OR industry_type = '') ");
            params.addValue("industryType", industryType);
        }
        if (status != null) {
            sql.append(" AND status = :status ");
            params.addValue("status", status);
        }
        sql.append(" ORDER BY sort_order ASC, id ASC ");
        return Result.success(namedParameterJdbcTemplate.queryForList(sql.toString(), params));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        String fieldKey = String.valueOf(body.get("fieldKey"));
        Long exists = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_custom_field WHERE field_key = :fieldKey AND deleted = 0",
                new MapSqlParameterSource("fieldKey", fieldKey),
                Long.class
        );
        if (exists != null && exists > 0) {
            return Result.error("字段编码已存在");
        }

        String sql = """
                INSERT INTO sys_user_custom_field
                    (field_key, field_name, field_type, required_flag, placeholder, options_json, default_value,
                     industry_type, sort_order, status, created_time, updated_time, deleted)
                VALUES
                    (:fieldKey, :fieldName, :fieldType, :requiredFlag, :placeholder, :optionsJson, :defaultValue,
                     :industryType, :sortOrder, :status, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("fieldKey", fieldKey)
                .addValue("fieldName", body.get("fieldName"))
                .addValue("fieldType", valueOrDefault(body.get("fieldType"), "TEXT"))
                .addValue("requiredFlag", valueOrDefault(body.get("requiredFlag"), 0))
                .addValue("placeholder", body.get("placeholder"))
                .addValue("optionsJson", body.get("optionsJson"))
                .addValue("defaultValue", body.get("defaultValue"))
                .addValue("industryType", body.get("industryType"))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0))
                .addValue("status", valueOrDefault(body.get("status"), 1)));

        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE sys_user_custom_field
                SET field_name = :fieldName,
                    field_type = :fieldType,
                    required_flag = :requiredFlag,
                    placeholder = :placeholder,
                    options_json = :optionsJson,
                    default_value = :defaultValue,
                    industry_type = :industryType,
                    sort_order = :sortOrder,
                    status = :status,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("fieldName", body.get("fieldName"))
                .addValue("fieldType", valueOrDefault(body.get("fieldType"), "TEXT"))
                .addValue("requiredFlag", valueOrDefault(body.get("requiredFlag"), 0))
                .addValue("placeholder", body.get("placeholder"))
                .addValue("optionsJson", body.get("optionsJson"))
                .addValue("defaultValue", body.get("defaultValue"))
                .addValue("industryType", body.get("industryType"))
                .addValue("sortOrder", valueOrDefault(body.get("sortOrder"), 0))
                .addValue("status", valueOrDefault(body.get("status"), 1)));
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE sys_user_custom_field SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
}
