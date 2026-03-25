package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Notice center APIs.
 */
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('*:*:*','notice:manage')")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE n.deleted = 0 ");

        if (StringUtils.hasText(title)) {
            where.append(" AND n.title LIKE :title ");
            params.addValue("title", "%" + title + "%");
        }
        if (StringUtils.hasText(category)) {
            where.append(" AND n.category = :category ");
            params.addValue("category", category);
        }
        if (StringUtils.hasText(status)) {
            where.append(" AND n.status = :status ");
            params.addValue("status", status);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_notice n " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);

        String sql = """
                SELECT n.id,
                       n.title,
                       n.content,
                       n.category,
                       n.top_flag AS topFlag,
                       n.status,
                       n.publish_scope AS publishScope,
                       n.target_dept_ids AS targetDeptIds,
                       n.attachment_json AS attachmentJson,
                       n.published_by AS publishedBy,
                       u.real_name AS publishedByName,
                       n.published_time AS publishedTime,
                       n.created_time AS createdTime,
                       n.updated_time AS updatedTime
                FROM hr_notice n
                LEFT JOIN sys_user u ON n.published_by = u.id
                """ + where + " ORDER BY n.top_flag DESC, n.published_time DESC, n.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> records = namedParameterJdbcTemplate.queryForList(sql, params);
        return Result.success(PageResult.of(records, total, pageNum, pageSize));
    }

    @GetMapping("/current/page")
    public Result<PageResult<Map<String, Object>>> currentPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer readStatus
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        String deptId = resolveCurrentUserDeptId(userId);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("deptId", deptId == null ? "" : deptId);

        StringBuilder where = new StringBuilder("""
                WHERE n.deleted = 0
                  AND n.status = 'PUBLISHED'
                  AND (
                      n.publish_scope = 'ALL'
                      OR (:deptId <> '' AND n.publish_scope = 'DEPT' AND FIND_IN_SET(:deptId, IFNULL(n.target_dept_ids, '')) > 0)
                  )
                """);

        if (StringUtils.hasText(title)) {
            where.append(" AND n.title LIKE :title ");
            params.addValue("title", "%" + title + "%");
        }
        if (StringUtils.hasText(category)) {
            where.append(" AND n.category = :category ");
            params.addValue("category", category);
        }
        if (readStatus != null) {
            if (readStatus == 1) {
                where.append(" AND rr.id IS NOT NULL ");
            } else if (readStatus == 0) {
                where.append(" AND rr.id IS NULL ");
            }
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_notice n LEFT JOIN hr_notice_read_record rr ON rr.notice_id = n.id AND rr.user_id = :userId " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);

        String sql = """
                SELECT n.id,
                       n.title,
                       n.content,
                       n.category,
                       n.top_flag AS topFlag,
                       n.publish_scope AS publishScope,
                       n.target_dept_ids AS targetDeptIds,
                       n.attachment_json AS attachmentJson,
                       n.published_time AS publishedTime,
                       CASE WHEN rr.id IS NULL THEN 0 ELSE 1 END AS readFlag,
                       rr.read_time AS readTime
                FROM hr_notice n
                LEFT JOIN hr_notice_read_record rr ON rr.notice_id = n.id AND rr.user_id = :userId
                """ + where + " ORDER BY n.top_flag DESC, n.published_time DESC, n.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> records = namedParameterJdbcTemplate.queryForList(sql, params);
        return Result.success(PageResult.of(records, total, pageNum, pageSize));
    }

    @GetMapping("/dept/page")
    public Result<PageResult<Map<String, Object>>> deptPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        String deptId = resolveCurrentUserDeptId(userId);
        if (!StringUtils.hasText(deptId)) {
            return Result.success(PageResult.of(List.of(), 0L, pageNum, pageSize));
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("deptId", deptId);
        StringBuilder where = new StringBuilder("""
                WHERE n.deleted = 0
                  AND n.status = 'PUBLISHED'
                  AND n.publish_scope = 'DEPT'
                  AND FIND_IN_SET(:deptId, IFNULL(n.target_dept_ids, '')) > 0
                """);
        if (StringUtils.hasText(title)) {
            where.append(" AND n.title LIKE :title ");
            params.addValue("title", "%" + title + "%");
        }
        if (StringUtils.hasText(category)) {
            where.append(" AND n.category = :category ");
            params.addValue("category", category);
        }

        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_notice n LEFT JOIN hr_notice_read_record rr ON rr.notice_id = n.id AND rr.user_id = :userId " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String sql = """
                SELECT n.id,
                       n.title,
                       n.content,
                       n.category,
                       n.top_flag AS topFlag,
                       n.publish_scope AS publishScope,
                       n.target_dept_ids AS targetDeptIds,
                       n.attachment_json AS attachmentJson,
                       n.published_time AS publishedTime,
                       CASE WHEN rr.id IS NULL THEN 0 ELSE 1 END AS readFlag,
                       rr.read_time AS readTime
                FROM hr_notice n
                LEFT JOIN hr_notice_read_record rr ON rr.notice_id = n.id AND rr.user_id = :userId
                """ + where + " ORDER BY n.top_flag DESC, n.published_time DESC, n.id DESC LIMIT :limit OFFSET :offset";
        return Result.success(PageResult.of(namedParameterJdbcTemplate.queryForList(sql, params), total, pageNum, pageSize));
    }

    @GetMapping("/dept/{id}")
    public Result<Map<String, Object>> deptDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }
        String deptId = resolveCurrentUserDeptId(userId);
        if (!StringUtils.hasText(deptId)) {
            return Result.error("无权限查看该公告");
        }

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT n.id,
                               n.title,
                               n.content,
                               n.category,
                               n.top_flag AS topFlag,
                               n.status,
                               n.publish_scope AS publishScope,
                               n.target_dept_ids AS targetDeptIds,
                               n.attachment_json AS attachmentJson,
                               n.published_by AS publishedBy,
                               n.published_time AS publishedTime,
                               n.created_time AS createdTime,
                               n.updated_time AS updatedTime
                        FROM hr_notice n
                        WHERE n.id = :id
                          AND n.deleted = 0
                          AND n.status = 'PUBLISHED'
                          AND n.publish_scope = 'DEPT'
                          AND FIND_IN_SET(:deptId, IFNULL(n.target_dept_ids, '')) > 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("deptId", deptId)
        );
        if (rows.isEmpty()) {
            return Result.error("公告不存在或无权限查看");
        }
        return Result.success(rows.get(0));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList("""
                        SELECT n.id,
                               n.title,
                               n.content,
                               n.category,
                               n.top_flag AS topFlag,
                               n.status,
                               n.publish_scope AS publishScope,
                               n.target_dept_ids AS targetDeptIds,
                               n.attachment_json AS attachmentJson,
                               n.published_by AS publishedBy,
                               n.published_time AS publishedTime,
                               n.created_time AS createdTime,
                               n.updated_time AS updatedTime
                        FROM hr_notice n
                        WHERE n.id = :id AND n.deleted = 0
                        """,
                new MapSqlParameterSource("id", id));

        if (rows.isEmpty()) {
            return Result.error("公告不存在");
        }
        return Result.success(rows.get(0));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*:*:*','notice:manage')")
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String status = StringUtils.hasText(stringValue(body.get("status")))
                ? stringValue(body.get("status"))
                : "PUBLISHED";

        String sql = """
                INSERT INTO hr_notice
                    (title, content, category, top_flag, status, publish_scope, target_dept_ids, attachment_json,
                     published_by, published_time, created_time, updated_time, deleted)
                VALUES
                    (:title, :content, :category, :topFlag, :status, :publishScope, :targetDeptIds, :attachmentJson,
                     :publishedBy,
                     CASE WHEN :status = 'PUBLISHED' THEN NOW() ELSE NULL END,
                     NOW(), NOW(), 0)
                """;

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("title", body.get("title"))
                .addValue("content", body.get("content"))
                .addValue("category", valueOrDefault(body.get("category"), "COMPANY"))
                .addValue("topFlag", valueOrDefault(body.get("topFlag"), 0))
                .addValue("status", status)
                .addValue("publishScope", valueOrDefault(body.get("publishScope"), "ALL"))
                .addValue("targetDeptIds", body.get("targetDeptIds"))
                .addValue("attachmentJson", body.get("attachmentJson"))
                .addValue("publishedBy", currentUserId));

        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*:*:*','notice:manage')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = StringUtils.hasText(stringValue(body.get("status")))
                ? stringValue(body.get("status"))
                : "PUBLISHED";

        int rows = namedParameterJdbcTemplate.update("""
                        UPDATE hr_notice
                        SET title = :title,
                            content = :content,
                            category = :category,
                            top_flag = :topFlag,
                            status = :status,
                            publish_scope = :publishScope,
                            target_dept_ids = :targetDeptIds,
                            attachment_json = :attachmentJson,
                            published_time = CASE
                                WHEN status <> 'PUBLISHED' AND :status = 'PUBLISHED' THEN NOW()
                                ELSE published_time
                            END,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("title", body.get("title"))
                        .addValue("content", body.get("content"))
                        .addValue("category", valueOrDefault(body.get("category"), "COMPANY"))
                        .addValue("topFlag", valueOrDefault(body.get("topFlag"), 0))
                        .addValue("status", status)
                        .addValue("publishScope", valueOrDefault(body.get("publishScope"), "ALL"))
                        .addValue("targetDeptIds", body.get("targetDeptIds"))
                        .addValue("attachmentJson", body.get("attachmentJson"))
        );

        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*:*:*','notice:manage')")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_notice SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        return Result.success(rows > 0);
    }

    @PostMapping("/{id}/read")
    public Result<Boolean> markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户");
        }

        Long exists = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_notice_read_record WHERE notice_id = :noticeId AND user_id = :userId",
                new MapSqlParameterSource().addValue("noticeId", id).addValue("userId", userId),
                Long.class
        );

        if (exists == null || exists == 0) {
            namedParameterJdbcTemplate.update("""
                            INSERT INTO hr_notice_read_record (notice_id, user_id, read_time, created_time)
                            VALUES (:noticeId, :userId, NOW(), NOW())
                            """,
                    new MapSqlParameterSource().addValue("noticeId", id).addValue("userId", userId)
            );
        }

        return Result.success(true);
    }

    private String resolveCurrentUserDeptId(Long userId) {
        try {
            List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(
                    "SELECT ext_json AS extJson FROM sys_user WHERE id = :id AND deleted = 0",
                    new MapSqlParameterSource("id", userId)
            );
            if (users.isEmpty()) {
                return "";
            }

            Object ext = users.get(0).get("extJson");
            if (ext == null) {
                return "";
            }
            String text = String.valueOf(ext);
            if (!text.contains("deptId")) {
                return "";
            }

            int idx = text.indexOf("\"deptId\"");
            if (idx < 0) {
                return "";
            }
            String right = text.substring(idx);
            int colon = right.indexOf(':');
            if (colon < 0) {
                return "";
            }
            String numberPart = right.substring(colon + 1).trim();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < numberPart.length(); i++) {
                char c = numberPart.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else if (builder.length() > 0) {
                    break;
                }
            }
            return builder.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
