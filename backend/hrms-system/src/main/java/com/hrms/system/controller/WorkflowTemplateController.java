package com.hrms.system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Workflow template controller for list/detail/designer save/publish/versioning.
 */
@Slf4j
@RestController
@RequestMapping("/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_DISABLED = "disabled";
    private static final AtomicBoolean SCHEMA_READY = new AtomicBoolean(false);

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        ensureSchemaReady();

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE t.deleted = 0 ");

        if (StringUtils.hasText(keyword)) {
            where.append(" AND (t.template_name LIKE :keyword OR t.template_code LIKE :keyword OR t.template_id LIKE :keyword) ");
            params.addValue("keyword", "%" + keyword.trim() + "%");
        }
        if (StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)) {
            where.append(" AND COALESCE(NULLIF(t.category, ''), NULLIF(t.business_type, ''), '通用') = :category ");
            params.addValue("category", category.trim());
        }

        String clientStatus = StringUtils.hasText(status) ? normalizeClientStatus(status) : "all";
        if (StringUtils.hasText(clientStatus) && !"all".equals(clientStatus)) {
            where.append("""
                     AND (
                        CASE
                           WHEN UPPER(IFNULL(t.status, '')) IN ('ENABLED', 'PUBLISHED') THEN 'published'
                           WHEN UPPER(IFNULL(t.status, '')) = 'DISABLED' THEN 'disabled'
                           ELSE 'draft'
                        END
                     ) = :clientStatus
                    """);
            params.addValue("clientStatus", clientStatus);
        }

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM hr_workflow_template t " + where, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", Math.max(pageNum - 1, 0) * pageSize);

        String sql = """
                SELECT t.id,
                       t.template_id AS templateId,
                       t.template_name AS templateName,
                       t.template_code AS templateCode,
                       COALESCE(NULLIF(t.category, ''), NULLIF(t.business_type, ''), '通用') AS category,
                       t.business_type AS businessType,
                       t.status,
                       COALESCE(t.current_version, t.version_no, 1) AS version,
                       COALESCE(t.current_version, t.version_no, 1) AS currentVersion,
                       DATE_FORMAT(t.updated_time, '%Y-%m-%d %H:%i:%s') AS updatedAt
                FROM hr_workflow_template t
                """ + where + " ORDER BY t.updated_time DESC, t.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        for (Map<String, Object> row : rows) {
            String safeTemplateId = nonEmptyOrDefault(stringValue(row.get("templateId")), "tpl_" + row.get("id"));
            row.put("templateId", safeTemplateId);
            row.put("status", normalizeClientStatus(stringValue(row.get("status"))));
            if (!StringUtils.hasText(stringValue(row.get("templateCode")))) {
                row.put("templateCode", buildTemplateCodeByTemplateId(safeTemplateId));
            }
        }
        return Result.success(PageResult.of(rows, total, pageNum, pageSize));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<String>> categories() {
        ensureSchemaReady();
        List<String> values = jdbc.queryForList("""
                        SELECT DISTINCT COALESCE(NULLIF(category, ''), NULLIF(business_type, ''), '通用') AS category
                        FROM hr_workflow_template
                        WHERE deleted = 0
                        ORDER BY category ASC
                        """,
                new MapSqlParameterSource(),
                String.class
        );
        return Result.success(values.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> detail(@PathVariable String templateId) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, false);
        if (row == null) {
            return Result.error("流程模板不存在");
        }
        return Result.success(buildTemplateResponse(row));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        ensureSchemaReady();

        String templateName = nonEmptyOrDefault(stringValue(body.get("templateName")), "未命名流程模板");
        String category = nonEmptyOrDefault(stringValue(body.get("category")), "通用");
        String clientStatus = normalizeClientStatus(nonEmptyOrDefault(stringValue(body.get("status")), STATUS_DRAFT));
        String dbStatus = toDbStatus(clientStatus);

        String templateId = StringUtils.hasText(stringValue(body.get("templateId")))
                ? stringValue(body.get("templateId")).trim()
                : generateTemplateId();
        while (existsTemplateId(templateId)) {
            templateId = generateTemplateId();
        }

        String requestedCode = stringValue(body.get("templateCode"));
        String templateCode = StringUtils.hasText(requestedCode)
                ? normalizeTemplateCode(requestedCode)
                : buildTemplateCodeByTemplateId(templateId);
        templateCode = ensureUniqueTemplateCode(templateCode, null);

        String businessType = resolveBusinessType(body, category, null);
        Map<String, Object> snapshot = buildSnapshot(templateId, body, null);
        overrideSnapshotMeta(snapshot, templateId, templateName, templateCode, category, clientStatus, 1);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(mapValue(snapshot.get("definition")));
        String layoutJson = toJson(mapValue(snapshot.get("layout")));
        String remark = stringValue(body.get("remark"));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        INSERT INTO hr_workflow_template
                            (template_id, template_name, template_code, category, business_type,
                             status, current_version, version_no,
                             latest_definition_json, latest_layout_json, latest_snapshot_json,
                             published_version, published_snapshot_json,
                             remark, created_by, updated_by, created_time, updated_time, deleted)
                        VALUES
                            (:templateId, :templateName, :templateCode, :category, :businessType,
                             :status, :currentVersion, :versionNo,
                             :definitionJson, :layoutJson, :snapshotJson,
                             :publishedVersion, :publishedSnapshotJson,
                             :remark, :createdBy, :updatedBy, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("templateName", templateName)
                        .addValue("templateCode", templateCode)
                        .addValue("category", category)
                        .addValue("businessType", businessType)
                        .addValue("status", dbStatus)
                        .addValue("currentVersion", 1)
                        .addValue("versionNo", 1)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("publishedVersion", "ENABLED".equalsIgnoreCase(dbStatus) ? 1 : null)
                        .addValue("publishedSnapshotJson", "ENABLED".equalsIgnoreCase(dbStatus) ? snapshotJson : null)
                        .addValue("remark", remark)
                        .addValue("createdBy", operatorId)
                        .addValue("updatedBy", operatorId)
        );

        Long dbId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (dbId != null) {
            syncRuntimeNodesBySnapshot(dbId, snapshot);
        }
        insertVersion(templateId, 1, "save", templateName, clientStatus, snapshotJson, definitionJson, layoutJson, null);

        Map<String, Object> row = queryTemplateRow(templateId, false);
        return Result.success(buildTemplateResponse(row));
    }

    @PutMapping("/{templateId}")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> save(@PathVariable String templateId, @RequestBody Map<String, Object> body) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("流程模板不存在");
        }

        String effectiveTemplateId = nonEmptyOrDefault(stringValue(row.get("template_id")), "tpl_" + row.get("id"));
        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = buildSnapshot(effectiveTemplateId, body, current);

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String templateName = nonEmptyOrDefault(stringValue(body.get("templateName")), stringValue(current.get("templateName")));
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(body.get("templateCode")), stringValue(current.get("templateCode"))));
        templateCode = ensureUniqueTemplateCode(templateCode, longValue(row.get("id")));
        String category = nonEmptyOrDefault(stringValue(body.get("category")), stringValue(current.get("category")));
        String businessType = resolveBusinessType(body, category, stringValue(row.get("business_type")));
        String clientStatus = normalizeClientStatus(nonEmptyOrDefault(stringValue(body.get("status")), stringValue(current.get("status"))));
        String dbStatus = toDbStatus(clientStatus);

        overrideSnapshotMeta(snapshot, effectiveTemplateId, templateName, templateCode, category, clientStatus, nextVersion);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(mapValue(snapshot.get("definition")));
        String layoutJson = toJson(mapValue(snapshot.get("layout")));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        UPDATE hr_workflow_template
                        SET template_name = :templateName,
                            template_code = :templateCode,
                            category = :category,
                            business_type = :businessType,
                            status = :status,
                            current_version = :currentVersion,
                            version_no = :versionNo,
                            latest_definition_json = :definitionJson,
                            latest_layout_json = :layoutJson,
                            latest_snapshot_json = :snapshotJson,
                            published_version = CASE WHEN UPPER(:status) = 'ENABLED' THEN :currentVersion ELSE published_version END,
                            published_snapshot_json = CASE WHEN UPPER(:status) = 'ENABLED' THEN :snapshotJson ELSE published_snapshot_json END,
                            updated_by = :updatedBy,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", row.get("id"))
                        .addValue("templateName", templateName)
                        .addValue("templateCode", templateCode)
                        .addValue("category", category)
                        .addValue("businessType", businessType)
                        .addValue("status", dbStatus)
                        .addValue("currentVersion", nextVersion)
                        .addValue("versionNo", nextVersion)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("updatedBy", operatorId)
        );

        Long dbId = longValue(row.get("id"));
        if (dbId != null) {
            syncRuntimeNodesBySnapshot(dbId, snapshot);
        }
        insertVersion(effectiveTemplateId, nextVersion, "save", templateName, clientStatus, snapshotJson, definitionJson, layoutJson, null);

        Map<String, Object> refreshed = queryTemplateRow(effectiveTemplateId, false);
        return Result.success(buildTemplateResponse(refreshed));
    }

    @PostMapping("/{templateId}/publish")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> publish(@PathVariable String templateId, @RequestBody(required = false) Map<String, Object> body) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("流程模板不存在");
        }

        Map<String, Object> payload = body == null ? Collections.emptyMap() : body;
        String effectiveTemplateId = nonEmptyOrDefault(stringValue(row.get("template_id")), "tpl_" + row.get("id"));
        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = buildSnapshot(effectiveTemplateId, payload, current);

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String templateName = nonEmptyOrDefault(stringValue(payload.get("templateName")), stringValue(current.get("templateName")));
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(payload.get("templateCode")), stringValue(current.get("templateCode"))));
        templateCode = ensureUniqueTemplateCode(templateCode, longValue(row.get("id")));
        String category = nonEmptyOrDefault(stringValue(payload.get("category")), stringValue(current.get("category")));
        String businessType = resolveBusinessType(payload, category, stringValue(row.get("business_type")));

        overrideSnapshotMeta(snapshot, effectiveTemplateId, templateName, templateCode, category, STATUS_PUBLISHED, nextVersion);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(mapValue(snapshot.get("definition")));
        String layoutJson = toJson(mapValue(snapshot.get("layout")));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        UPDATE hr_workflow_template
                        SET template_name = :templateName,
                            template_code = :templateCode,
                            category = :category,
                            business_type = :businessType,
                            status = 'ENABLED',
                            current_version = :currentVersion,
                            version_no = :versionNo,
                            latest_definition_json = :definitionJson,
                            latest_layout_json = :layoutJson,
                            latest_snapshot_json = :snapshotJson,
                            published_version = :currentVersion,
                            published_snapshot_json = :snapshotJson,
                            updated_by = :updatedBy,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", row.get("id"))
                        .addValue("templateName", templateName)
                        .addValue("templateCode", templateCode)
                        .addValue("category", category)
                        .addValue("businessType", businessType)
                        .addValue("currentVersion", nextVersion)
                        .addValue("versionNo", nextVersion)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("updatedBy", operatorId)
        );

        Long dbId = longValue(row.get("id"));
        if (dbId != null) {
            syncRuntimeNodesBySnapshot(dbId, snapshot);
        }
        insertVersion(effectiveTemplateId, nextVersion, "publish", templateName, STATUS_PUBLISHED, snapshotJson, definitionJson, layoutJson, null);

        Map<String, Object> detail = buildTemplateResponse(queryTemplateRow(effectiveTemplateId, false));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templateId", effectiveTemplateId);
        result.put("publishTime", detail.get("updatedAt"));
        result.put("version", detail.get("version"));
        result.put("data", detail);
        return Result.success(result);
    }

    @PostMapping("/{templateId}/duplicate")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> duplicate(@PathVariable String templateId) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("模板不存在，无法复制");
        }

        Map<String, Object> source = buildTemplateResponse(row);
        String sourceName = nonEmptyOrDefault(stringValue(source.get("templateName")), "流程模板");
        String sourceCode = nonEmptyOrDefault(stringValue(source.get("templateCode")), "TEMPLATE");
        String sourceCategory = nonEmptyOrDefault(stringValue(source.get("category")), "通用");
        String sourceBusinessType = nonEmptyOrDefault(stringValue(source.get("businessType")), resolveBusinessType(Collections.emptyMap(), sourceCategory, null));

        String newTemplateId = generateTemplateId();
        while (existsTemplateId(newTemplateId)) {
            newTemplateId = generateTemplateId();
        }
        String newTemplateCode = ensureUniqueTemplateCode(normalizeTemplateCode(sourceCode + "_COPY"), null);
        String newTemplateName = sourceName + "-副本";

        Map<String, Object> snapshot = mapValue(source.get("snapshot"));
        if (snapshot.isEmpty()) {
            snapshot = buildSnapshot(newTemplateId, Collections.emptyMap(), source);
        }
        overrideSnapshotMeta(snapshot, newTemplateId, newTemplateName, newTemplateCode, sourceCategory, STATUS_DRAFT, 1);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(mapValue(snapshot.get("definition")));
        String layoutJson = toJson(mapValue(snapshot.get("layout")));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        INSERT INTO hr_workflow_template
                            (template_id, template_name, template_code, category, business_type,
                             status, current_version, version_no,
                             latest_definition_json, latest_layout_json, latest_snapshot_json,
                             published_version, published_snapshot_json,
                             remark, created_by, updated_by, created_time, updated_time, deleted)
                        VALUES
                            (:templateId, :templateName, :templateCode, :category, :businessType,
                             'DRAFT', 1, 1,
                             :definitionJson, :layoutJson, :snapshotJson,
                             NULL, NULL,
                             :remark, :createdBy, :updatedBy, NOW(), NOW(), 0)
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", newTemplateId)
                        .addValue("templateName", newTemplateName)
                        .addValue("templateCode", newTemplateCode)
                        .addValue("category", sourceCategory)
                        .addValue("businessType", sourceBusinessType)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("remark", "复制自 " + nonEmptyOrDefault(stringValue(source.get("templateId")), templateId))
                        .addValue("createdBy", operatorId)
                        .addValue("updatedBy", operatorId)
        );

        Long dbId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (dbId != null) {
            syncRuntimeNodesBySnapshot(dbId, snapshot);
        }
        insertVersion(newTemplateId, 1, "save", newTemplateName, STATUS_DRAFT, snapshotJson, definitionJson, layoutJson, "复制模板");
        return Result.success(buildTemplateResponse(queryTemplateRow(newTemplateId, false)));
    }

    @DeleteMapping("/{templateId}")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> delete(@PathVariable String templateId) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("模板不存在");
        }
        Long dbId = longValue(row.get("id"));
        Long operatorId = currentOperatorId();

        int affected = jdbc.update("""
                        UPDATE hr_workflow_template
                        SET deleted = 1,
                            updated_by = :updatedBy,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", dbId)
                        .addValue("updatedBy", operatorId)
        );

        if (dbId != null) {
            softDeleteRuntimeNodes(dbId);
        }
        return Result.success(affected > 0);
    }

    @GetMapping("/{templateId}/versions")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<Map<String, Object>>> versions(@PathVariable String templateId) {
        ensureSchemaReady();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                        SELECT id,
                               template_id AS templateId,
                               version_no AS version,
                               action_type AS actionType,
                               template_name AS templateName,
                               status,
                               snapshot_json AS snapshotJson,
                               definition_json AS definitionJson,
                               layout_json AS layoutJson,
                               operator_id AS operatorId,
                               operator_name AS operator,
                               remark,
                               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS updatedAt
                        FROM hr_workflow_template_version
                        WHERE template_id = :templateId
                        ORDER BY version_no DESC, id DESC
                        """,
                new MapSqlParameterSource("templateId", templateId)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>(row);
            item.put("status", normalizeClientStatus(stringValue(row.get("status"))));
            Map<String, Object> payload = resolveVersionPayload(row);
            item.put("snapshot", payload);
            item.put("payload", payload);
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/{templateId}/versions/{versionNo}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> versionDetail(@PathVariable String templateId, @PathVariable Integer versionNo) {
        ensureSchemaReady();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                        SELECT id,
                               template_id AS templateId,
                               version_no AS version,
                               action_type AS actionType,
                               template_name AS templateName,
                               status,
                               snapshot_json AS snapshotJson,
                               definition_json AS definitionJson,
                               layout_json AS layoutJson,
                               operator_id AS operatorId,
                               operator_name AS operator,
                               remark,
                               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS updatedAt
                        FROM hr_workflow_template_version
                        WHERE template_id = :templateId
                          AND version_no = :versionNo
                        ORDER BY id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource().addValue("templateId", templateId).addValue("versionNo", versionNo)
        );
        if (rows.isEmpty()) {
            return Result.error("历史版本不存在");
        }

        Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
        row.put("status", normalizeClientStatus(stringValue(row.get("status"))));
        Map<String, Object> payload = resolveVersionPayload(row);
        row.put("snapshot", payload);
        row.put("payload", payload);
        return Result.success(row);
    }

    @PostMapping("/{templateId}/versions/{versionNo}/restore")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> restoreVersion(
            @PathVariable String templateId,
            @PathVariable Integer versionNo,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        ensureSchemaReady();
        List<Map<String, Object>> versionRows = jdbc.queryForList("""
                        SELECT version_no, template_name, status, snapshot_json, definition_json, layout_json
                        FROM hr_workflow_template_version
                        WHERE template_id = :templateId
                          AND version_no = :versionNo
                        ORDER BY id DESC
                        LIMIT 1
                        """,
                new MapSqlParameterSource().addValue("templateId", templateId).addValue("versionNo", versionNo)
        );
        if (versionRows.isEmpty()) {
            return Result.error("历史版本不存在，无法恢复");
        }

        Map<String, Object> templateRow = queryTemplateRow(templateId, true);
        if (templateRow == null) {
            return Result.error("流程模板不存在");
        }

        Map<String, Object> target = versionRows.get(0);
        Map<String, Object> payload = parseJson(stringValue(target.get("snapshot_json")));
        if (payload.isEmpty()) {
            payload = new LinkedHashMap<>();
            payload.put("definition", parseJson(stringValue(target.get("definition_json"))));
            payload.put("layout", parseJson(stringValue(target.get("layout_json"))));
        }
        Map<String, Object> current = buildTemplateResponse(templateRow);
        payload = buildSnapshot(nonEmptyOrDefault(stringValue(templateRow.get("template_id")), templateId), payload, current);

        int nextVersion = intValue(templateRow.get("current_version"), intValue(templateRow.get("version_no"), 1)) + 1;
        String restoredName = nonEmptyOrDefault(stringValue(target.get("template_name")), stringValue(current.get("templateName")));
        String restoredCode = normalizeTemplateCode(nonEmptyOrDefault(
                stringValue(mapValue(payload).get("templateCode")),
                nonEmptyOrDefault(stringValue(current.get("templateCode")), buildTemplateCodeByTemplateId(templateId))
        ));
        restoredCode = ensureUniqueTemplateCode(restoredCode, longValue(templateRow.get("id")));
        String restoredCategory = nonEmptyOrDefault(
                stringValue(mapValue(payload).get("category")),
                nonEmptyOrDefault(stringValue(current.get("category")), "通用")
        );
        String restoredClientStatus = normalizeClientStatus(nonEmptyOrDefault(stringValue(target.get("status")), stringValue(current.get("status"))));
        String restoredDbStatus = toDbStatus(restoredClientStatus);
        String restoredBusinessType = resolveBusinessType(Collections.emptyMap(), restoredCategory, stringValue(templateRow.get("business_type")));

        overrideSnapshotMeta(payload,
                nonEmptyOrDefault(stringValue(templateRow.get("template_id")), templateId),
                restoredName,
                restoredCode,
                restoredCategory,
                restoredClientStatus,
                nextVersion
        );
        String snapshotJson = toJson(payload);
        String definitionJson = toJson(mapValue(payload.get("definition")));
        String layoutJson = toJson(mapValue(payload.get("layout")));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        UPDATE hr_workflow_template
                        SET template_name = :templateName,
                            template_code = :templateCode,
                            category = :category,
                            business_type = :businessType,
                            status = :status,
                            current_version = :currentVersion,
                            version_no = :versionNo,
                            latest_definition_json = :definitionJson,
                            latest_layout_json = :layoutJson,
                            latest_snapshot_json = :snapshotJson,
                            published_version = CASE WHEN UPPER(:status) = 'ENABLED' THEN :currentVersion ELSE published_version END,
                            published_snapshot_json = CASE WHEN UPPER(:status) = 'ENABLED' THEN :snapshotJson ELSE published_snapshot_json END,
                            updated_by = :updatedBy,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", templateRow.get("id"))
                        .addValue("templateName", restoredName)
                        .addValue("templateCode", restoredCode)
                        .addValue("category", restoredCategory)
                        .addValue("businessType", restoredBusinessType)
                        .addValue("status", restoredDbStatus)
                        .addValue("currentVersion", nextVersion)
                        .addValue("versionNo", nextVersion)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("updatedBy", operatorId)
        );

        Long dbId = longValue(templateRow.get("id"));
        if (dbId != null) {
            syncRuntimeNodesBySnapshot(dbId, payload);
        }
        String remark = body == null ? null : stringValue(body.get("remark"));
        insertVersion(
                nonEmptyOrDefault(stringValue(templateRow.get("template_id")), templateId),
                nextVersion,
                "restore",
                restoredName,
                restoredClientStatus,
                snapshotJson,
                definitionJson,
                layoutJson,
                remark
        );

        return Result.success(buildTemplateResponse(queryTemplateRow(nonEmptyOrDefault(stringValue(templateRow.get("template_id")), templateId), false)));
    }

    @GetMapping("/{templateId}/nodes")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<Map<String, Object>>> nodes(@PathVariable String templateId) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, false);
        if (row == null) {
            return Result.error("流程模板不存在");
        }
        Long dbId = longValue(row.get("id"));
        if (dbId == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(queryRuntimeNodes(dbId));
    }

    @PostMapping("/{templateId}/nodes")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> saveNodes(@PathVariable String templateId, @RequestBody List<Map<String, Object>> nodes) {
        ensureSchemaReady();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("流程模板不存在");
        }

        Long dbId = longValue(row.get("id"));
        if (dbId == null) {
            return Result.error("流程模板主键不存在");
        }
        syncRuntimeNodesFromLegacy(dbId, nodes);

        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = mapValue(current.get("snapshot"));
        if (snapshot.isEmpty()) {
            snapshot = buildSnapshot(nonEmptyOrDefault(stringValue(row.get("template_id")), templateId), Collections.emptyMap(), current);
        }
        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        if (definition.isEmpty()) {
            definition = new LinkedHashMap<>();
        }
        Map<String, Object> rebuilt = buildLinearDefinitionFromRuntimeNodes(dbId);
        definition.put("nodes", listValue(rebuilt.get("nodes")));
        definition.put("edges", listValue(rebuilt.get("edges")));
        snapshot.put("definition", definition);

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String templateName = nonEmptyOrDefault(stringValue(row.get("template_name")), "未命名流程模板");
        String templateCode = nonEmptyOrDefault(stringValue(row.get("template_code")), buildTemplateCodeByTemplateId(nonEmptyOrDefault(stringValue(row.get("template_id")), templateId)));
        String category = nonEmptyOrDefault(stringValue(row.get("category")), "通用");
        String clientStatus = normalizeClientStatus(stringValue(row.get("status")));
        overrideSnapshotMeta(snapshot, nonEmptyOrDefault(stringValue(row.get("template_id")), templateId), templateName, templateCode, category, clientStatus, nextVersion);

        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(mapValue(snapshot.get("definition")));
        String layoutJson = toJson(mapValue(snapshot.get("layout")));
        Long operatorId = currentOperatorId();

        jdbc.update("""
                        UPDATE hr_workflow_template
                        SET current_version = :currentVersion,
                            version_no = :versionNo,
                            latest_definition_json = :definitionJson,
                            latest_layout_json = :layoutJson,
                            latest_snapshot_json = :snapshotJson,
                            updated_by = :updatedBy,
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", dbId)
                        .addValue("currentVersion", nextVersion)
                        .addValue("versionNo", nextVersion)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("updatedBy", operatorId)
        );

        insertVersion(nonEmptyOrDefault(stringValue(row.get("template_id")), templateId), nextVersion, "save", templateName, clientStatus, snapshotJson, definitionJson, layoutJson, "legacy nodes save");
        return Result.success(true);
    }

    private Map<String, Object> queryTemplateRow(String templateId, boolean forUpdate) {
        Long numericId = isNumeric(templateId) ? Long.valueOf(templateId) : null;
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       template_id,
                       template_name,
                       template_code,
                       category,
                       business_type,
                       status,
                       current_version,
                       version_no,
                       latest_definition_json,
                       latest_layout_json,
                       latest_snapshot_json,
                       published_version,
                       published_snapshot_json,
                       remark,
                       DATE_FORMAT(created_time, '%Y-%m-%d %H:%i:%s') AS created_time,
                       DATE_FORMAT(updated_time, '%Y-%m-%d %H:%i:%s') AS updated_time
                FROM hr_workflow_template
                WHERE deleted = 0
                  AND (
                        template_id = :templateId
                """);
        MapSqlParameterSource params = new MapSqlParameterSource("templateId", templateId);
        if (numericId != null) {
            sql.append(" OR id = :numericId ");
            params.addValue("numericId", numericId);
        }
        sql.append(") ORDER BY id DESC LIMIT 1");
        if (forUpdate) {
            sql.append(" FOR UPDATE");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> buildTemplateResponse(Map<String, Object> row) {
        if (row == null) {
            return Collections.emptyMap();
        }

        String templateId = nonEmptyOrDefault(stringValue(row.get("template_id")), "tpl_" + row.get("id"));
        String templateName = nonEmptyOrDefault(stringValue(row.get("template_name")), "未命名流程模板");
        String templateCode = nonEmptyOrDefault(stringValue(row.get("template_code")), buildTemplateCodeByTemplateId(templateId));
        String category = nonEmptyOrDefault(stringValue(row.get("category")), nonEmptyOrDefault(stringValue(row.get("business_type")), "通用"));
        String businessType = nonEmptyOrDefault(stringValue(row.get("business_type")), resolveBusinessType(Collections.emptyMap(), category, null));
        String status = normalizeClientStatus(stringValue(row.get("status")));
        int version = intValue(row.get("current_version"), intValue(row.get("version_no"), 1));
        int publishedVersion = intValue(row.get("published_version"), 0);
        String updatedAt = nonEmptyOrDefault(stringValue(row.get("updated_time")), TIME_FORMATTER.format(LocalDateTime.now()));

        Map<String, Object> snapshot = parseJson(stringValue(row.get("latest_snapshot_json")));
        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        Map<String, Object> layout = mapValue(snapshot.get("layout"));
        Map<String, Object> viewport = mapValue(snapshot.get("viewport"));

        if (definition.isEmpty()) {
            definition = parseJson(stringValue(row.get("latest_definition_json")));
        }
        if (layout.isEmpty()) {
            layout = parseJson(stringValue(row.get("latest_layout_json")));
        }
        if (definition.isEmpty()) {
            definition = buildLinearDefinitionFromRuntimeNodes(longValue(row.get("id")));
        }
        if (layout.isEmpty()) {
            layout = new LinkedHashMap<>();
            layout.put("manualPositions", true);
        }
        if (viewport.isEmpty()) {
            viewport = new LinkedHashMap<>();
            viewport.put("x", 0);
            viewport.put("y", 0);
            viewport.put("zoom", 1);
        }

        List<Map<String, Object>> nodes = listValue(definition.get("nodes"));
        List<Map<String, Object>> edges = listValue(definition.get("edges"));
        definition.put("nodes", nodes);
        definition.put("edges", edges);

        if (snapshot.isEmpty()) {
            snapshot = new LinkedHashMap<>();
        } else {
            snapshot = new LinkedHashMap<>(snapshot);
        }
        snapshot.put("templateId", templateId);
        snapshot.put("templateName", templateName);
        snapshot.put("templateCode", templateCode);
        snapshot.put("category", category);
        snapshot.put("businessType", businessType);
        snapshot.put("status", status);
        snapshot.put("version", version);
        snapshot.put("definition", definition);
        snapshot.put("layout", layout);
        snapshot.put("viewport", viewport);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", row.get("id"));
        response.put("templateId", templateId);
        response.put("templateName", templateName);
        response.put("templateCode", templateCode);
        response.put("category", category);
        response.put("businessType", businessType);
        response.put("status", status);
        response.put("version", version);
        response.put("currentVersion", version);
        response.put("publishedVersion", publishedVersion);
        response.put("updatedAt", updatedAt);
        response.put("definition", definition);
        response.put("nodes", nodes);
        response.put("edges", edges);
        response.put("layout", layout);
        response.put("viewport", viewport);
        response.put("snapshot", snapshot);
        return response;
    }

    private Map<String, Object> resolveVersionPayload(Map<String, Object> row) {
        Map<String, Object> snapshot = parseJson(stringValue(row.get("snapshotJson")));
        if (snapshot.isEmpty()) {
            snapshot = new LinkedHashMap<>();
        } else {
            snapshot = new LinkedHashMap<>(snapshot);
        }

        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        if (definition.isEmpty()) {
            definition = parseJson(stringValue(row.get("definitionJson")));
        }
        if (definition.isEmpty()) {
            definition = defaultLinearDefinition();
        }
        definition.put("nodes", listValue(definition.get("nodes")));
        definition.put("edges", listValue(definition.get("edges")));

        Map<String, Object> layout = mapValue(snapshot.get("layout"));
        if (layout.isEmpty()) {
            layout = parseJson(stringValue(row.get("layoutJson")));
        }
        if (layout.isEmpty()) {
            layout = new LinkedHashMap<>();
            layout.put("manualPositions", true);
        }

        Map<String, Object> viewport = mapValue(snapshot.get("viewport"));
        if (viewport.isEmpty()) {
            viewport = new LinkedHashMap<>();
            viewport.put("x", 0);
            viewport.put("y", 0);
            viewport.put("zoom", 1);
        }

        String templateId = nonEmptyOrDefault(stringValue(row.get("templateId")), stringValue(snapshot.get("templateId")));
        String templateName = nonEmptyOrDefault(stringValue(row.get("templateName")), stringValue(snapshot.get("templateName")));
        String templateCode = nonEmptyOrDefault(stringValue(snapshot.get("templateCode")), buildTemplateCodeByTemplateId(nonEmptyOrDefault(templateId, "tpl_unknown")));
        String category = nonEmptyOrDefault(stringValue(snapshot.get("category")), "通用");
        String businessType = nonEmptyOrDefault(stringValue(snapshot.get("businessType")), resolveBusinessType(Collections.emptyMap(), category, null));
        String status = normalizeClientStatus(stringValue(row.get("status")));
        int version = intValue(row.get("version"), 1);
        String updatedAt = nonEmptyOrDefault(stringValue(row.get("updatedAt")), TIME_FORMATTER.format(LocalDateTime.now()));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("templateId", templateId);
        payload.put("templateName", templateName);
        payload.put("templateCode", templateCode);
        payload.put("category", category);
        payload.put("businessType", businessType);
        payload.put("status", status);
        payload.put("version", version);
        payload.put("updatedAt", updatedAt);
        payload.put("definition", definition);
        payload.put("nodes", listValue(definition.get("nodes")));
        payload.put("edges", listValue(definition.get("edges")));
        payload.put("layout", layout);
        payload.put("viewport", viewport);
        return payload;
    }

    private void syncRuntimeNodesBySnapshot(Long templateDbId, Map<String, Object> snapshot) {
        if (templateDbId == null) {
            return;
        }
        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        List<Map<String, Object>> designerNodes = listValue(definition.get("nodes"));
        List<RuntimeTemplateNode> runtimeNodes = toRuntimeTemplateNodes(designerNodes);
        rewriteRuntimeNodes(templateDbId, runtimeNodes);
    }

    private void syncRuntimeNodesFromLegacy(Long templateDbId, List<Map<String, Object>> nodes) {
        List<RuntimeTemplateNode> runtimeNodes = toRuntimeTemplateNodesFromLegacy(nodes);
        rewriteRuntimeNodes(templateDbId, runtimeNodes);
    }

    private void rewriteRuntimeNodes(Long templateDbId, List<RuntimeTemplateNode> runtimeNodes) {
        List<Long> oldNodeIds = jdbc.queryForList("""
                        SELECT id
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId
                          AND deleted = 0
                        ORDER BY id ASC
                        """,
                new MapSqlParameterSource("templateId", templateDbId),
                Long.class
        );
        if (!oldNodeIds.isEmpty()) {
            jdbc.update("""
                            UPDATE hr_workflow_template_node_approver
                            SET deleted = 1, updated_time = NOW()
                            WHERE template_node_id IN (:nodeIds) AND deleted = 0
                            """,
                    new MapSqlParameterSource("nodeIds", oldNodeIds)
            );
            jdbc.update("""
                            UPDATE hr_workflow_template_node_cc
                            SET deleted = 1, updated_time = NOW()
                            WHERE template_node_id IN (:nodeIds) AND deleted = 0
                            """,
                    new MapSqlParameterSource("nodeIds", oldNodeIds)
            );
        }
        jdbc.update("""
                        UPDATE hr_workflow_template_node
                        SET deleted = 1, updated_time = NOW()
                        WHERE template_id = :templateId AND deleted = 0
                        """,
                new MapSqlParameterSource("templateId", templateDbId)
        );

        for (RuntimeTemplateNode node : runtimeNodes) {
            RuntimeApprover legacyApprover = node.approvers().isEmpty() ? null : node.approvers().get(0);
            jdbc.update("""
                            INSERT INTO hr_workflow_template_node
                                (template_id, node_order, node_name, node_type, approval_mode,
                                 approver_type, approver_role_code, approver_user_id,
                                 condition_expression, required_flag,
                                 created_time, updated_time, deleted)
                            VALUES
                                (:templateId, :nodeOrder, :nodeName, :nodeType, :approvalMode,
                                 :approverType, :approverRoleCode, :approverUserId,
                                 :conditionExpression, :requiredFlag,
                                 NOW(), NOW(), 0)
                            """,
                    new MapSqlParameterSource()
                            .addValue("templateId", templateDbId)
                            .addValue("nodeOrder", node.nodeOrder())
                            .addValue("nodeName", node.nodeName())
                            .addValue("nodeType", node.nodeType())
                            .addValue("approvalMode", node.approvalMode())
                            .addValue("approverType", legacyApprover == null ? "NONE" : legacyApprover.approverType())
                            .addValue("approverRoleCode", legacyApprover == null ? null : legacyApprover.roleCode())
                            .addValue("approverUserId", legacyApprover == null ? null : legacyApprover.userId())
                            .addValue("conditionExpression", node.conditionExpression())
                            .addValue("requiredFlag", node.requiredFlag())
            );
            Long templateNodeId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
            if (templateNodeId == null) {
                continue;
            }

            int order = 1;
            for (RuntimeApprover approver : node.approvers()) {
                jdbc.update("""
                                INSERT INTO hr_workflow_template_node_approver
                                    (template_node_id, approver_order, approver_type, approver_role_code, approver_user_id, created_time, updated_time, deleted)
                                VALUES
                                    (:templateNodeId, :approverOrder, :approverType, :approverRoleCode, :approverUserId, NOW(), NOW(), 0)
                                """,
                        new MapSqlParameterSource()
                                .addValue("templateNodeId", templateNodeId)
                                .addValue("approverOrder", order++)
                                .addValue("approverType", approver.approverType())
                                .addValue("approverRoleCode", approver.roleCode())
                                .addValue("approverUserId", approver.userId())
                );
            }

            int ccOrder = 1;
            for (RuntimeCc cc : node.ccs()) {
                jdbc.update("""
                                INSERT INTO hr_workflow_template_node_cc
                                    (template_node_id, cc_order, cc_type, cc_role_code, cc_user_id, cc_dept_id, cc_timing, created_time, updated_time, deleted)
                                VALUES
                                    (:templateNodeId, :ccOrder, :ccType, :ccRoleCode, :ccUserId, :ccDeptId, :ccTiming, NOW(), NOW(), 0)
                                """,
                        new MapSqlParameterSource()
                                .addValue("templateNodeId", templateNodeId)
                                .addValue("ccOrder", ccOrder++)
                                .addValue("ccType", cc.ccType())
                                .addValue("ccRoleCode", cc.roleCode())
                                .addValue("ccUserId", cc.userId())
                                .addValue("ccDeptId", cc.deptId())
                                .addValue("ccTiming", cc.ccTiming())
                );
            }
        }
    }

    private List<RuntimeTemplateNode> toRuntimeTemplateNodes(List<Map<String, Object>> designerNodes) {
        List<RuntimeTemplateNode> runtimeNodes = new ArrayList<>();
        int order = 1;
        for (Map<String, Object> designerNode : designerNodes) {
            String type = stringValue(designerNode.get("type"));
            if (!StringUtils.hasText(type)) {
                continue;
            }
            String lowerType = type.trim().toLowerCase(Locale.ROOT);
            if (!"approval".equals(lowerType) && !"cc".equals(lowerType)) {
                continue;
            }

            String nodeName = nonEmptyOrDefault(stringValue(designerNode.get("name")), "审批节点");
            Map<String, Object> config = mapValue(designerNode.get("config"));

            if ("approval".equals(lowerType)) {
                String approvalMode = toDbApprovalMode(stringValue(config.get("approvalMode")));
                Integer requiredFlag = booleanValue(config.get("required"), true) ? 1 : 0;
                List<RuntimeApprover> approvers = resolveApprovers(config);
                if (approvers.isEmpty()) {
                    approvers.add(new RuntimeApprover("DIRECT_LEADER", null, null));
                }
                runtimeNodes.add(new RuntimeTemplateNode(order++, nodeName, "APPROVAL", approvalMode, null, requiredFlag, approvers, List.of()));
            } else {
                List<RuntimeCc> ccs = resolveCcs(config);
                runtimeNodes.add(new RuntimeTemplateNode(order++, nodeName, "CC", "ANY", null, 1, List.of(), ccs));
            }
        }
        return runtimeNodes;
    }

    private List<RuntimeTemplateNode> toRuntimeTemplateNodesFromLegacy(List<Map<String, Object>> nodes) {
        List<RuntimeTemplateNode> runtimeNodes = new ArrayList<>();
        if (nodes == null) {
            return runtimeNodes;
        }
        int order = 1;
        for (Map<String, Object> node : nodes) {
            String nodeType = nonEmptyOrDefault(stringValue(node.get("nodeType")), "APPROVAL").toUpperCase(Locale.ROOT);
            String nodeName = nonEmptyOrDefault(stringValue(node.get("nodeName")), "审批节点");
            String approvalMode = toDbApprovalMode(stringValue(node.get("approvalMode")));
            String conditionExpression = stringValue(node.get("conditionExpression"));
            Integer requiredFlag = intValue(node.get("requiredFlag"), 1);

            List<RuntimeApprover> approvers = new ArrayList<>();
            for (Map<String, Object> approver : listValue(node.get("approvers"))) {
                approvers.add(new RuntimeApprover(
                        nonEmptyOrDefault(stringValue(approver.get("approverType")), "DIRECT_LEADER").toUpperCase(Locale.ROOT),
                        stringValue(approver.get("approverRoleCode")),
                        longValue(approver.get("approverUserId"))
                ));
            }

            List<RuntimeCc> ccs = new ArrayList<>();
            for (Map<String, Object> cc : listValue(node.get("ccs"))) {
                ccs.add(new RuntimeCc(
                        nonEmptyOrDefault(stringValue(cc.get("ccType")), "SPECIFIED_USER").toUpperCase(Locale.ROOT),
                        stringValue(cc.get("ccRoleCode")),
                        longValue(cc.get("ccUserId")),
                        longValue(cc.get("ccDeptId")),
                        nonEmptyOrDefault(stringValue(cc.get("ccTiming")), "AFTER_APPROVAL")
                ));
            }

            runtimeNodes.add(new RuntimeTemplateNode(order++, nodeName, nodeType, approvalMode, conditionExpression, requiredFlag, approvers, ccs));
        }
        return runtimeNodes;
    }

    private List<RuntimeApprover> resolveApprovers(Map<String, Object> config) {
        String assigneeType = nonEmptyOrDefault(stringValue(config.get("assigneeType")), "direct_leader").toLowerCase(Locale.ROOT);
        List<RuntimeApprover> approvers = new ArrayList<>();
        if ("role".equals(assigneeType)) {
            List<String> roleIds = stringList(config.get("roleIds"));
            for (String roleId : roleIds) {
                if (StringUtils.hasText(roleId)) {
                    approvers.add(new RuntimeApprover("SPECIFIED_ROLE", roleId.trim(), null));
                }
            }
            return approvers;
        }
        if ("user".equals(assigneeType)) {
            List<String> userIds = stringList(config.get("userIds"));
            for (String userId : userIds) {
                Long value = parseLongSafely(userId);
                if (value != null) {
                    approvers.add(new RuntimeApprover("SPECIFIED_USER", null, value));
                }
            }
            return approvers;
        }
        if ("self_select".equals(assigneeType)) {
            approvers.add(new RuntimeApprover("SELF", null, null));
            return approvers;
        }
        approvers.add(new RuntimeApprover("DIRECT_LEADER", null, null));
        return approvers;
    }

    private List<RuntimeCc> resolveCcs(Map<String, Object> config) {
        String targetType = nonEmptyOrDefault(stringValue(config.get("targetType")), "user").toLowerCase(Locale.ROOT);
        List<RuntimeCc> ccs = new ArrayList<>();
        if ("role".equals(targetType)) {
            for (String roleId : stringList(config.get("roleIds"))) {
                if (StringUtils.hasText(roleId)) {
                    ccs.add(new RuntimeCc("SPECIFIED_ROLE", roleId.trim(), null, null, "AFTER_APPROVAL"));
                }
            }
            return ccs;
        }
        if ("user".equals(targetType)) {
            for (String userId : stringList(config.get("userIds"))) {
                Long value = parseLongSafely(userId);
                if (value != null) {
                    ccs.add(new RuntimeCc("SPECIFIED_USER", null, value, null, "AFTER_APPROVAL"));
                }
            }
            return ccs;
        }
        if ("department".equals(targetType) || "dept".equals(targetType)) {
            for (String deptId : stringList(config.get("departmentIds"))) {
                Long value = parseLongSafely(deptId);
                if (value != null) {
                    ccs.add(new RuntimeCc("SPECIFIED_DEPT", null, null, value, "AFTER_APPROVAL"));
                }
            }
            return ccs;
        }
        return ccs;
    }

    private List<Map<String, Object>> queryRuntimeNodes(Long templateDbId) {
        List<Map<String, Object>> nodes = jdbc.queryForList("""
                        SELECT id,
                               template_id AS templateId,
                               node_order AS nodeOrder,
                               node_name AS nodeName,
                               IFNULL(node_type, 'APPROVAL') AS nodeType,
                               IFNULL(approval_mode, 'ANY') AS approvalMode,
                               approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId,
                               condition_expression AS conditionExpression,
                               required_flag AS requiredFlag
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId
                          AND deleted = 0
                        ORDER BY node_order ASC, id ASC
                        """,
                new MapSqlParameterSource("templateId", templateDbId)
        );
        for (Map<String, Object> node : nodes) {
            Long nodeId = longValue(node.get("id"));
            node.put("approvers", queryApprovers(nodeId));
            node.put("ccs", queryCcs(nodeId));
        }
        return nodes;
    }

    private List<Map<String, Object>> queryApprovers(Long templateNodeId) {
        if (templateNodeId == null) {
            return Collections.emptyList();
        }
        return jdbc.queryForList("""
                        SELECT id,
                               template_node_id AS templateNodeId,
                               approver_order AS approverOrder,
                               approver_type AS approverType,
                               approver_role_code AS approverRoleCode,
                               approver_user_id AS approverUserId
                        FROM hr_workflow_template_node_approver
                        WHERE template_node_id = :templateNodeId
                          AND deleted = 0
                        ORDER BY approver_order ASC, id ASC
                        """,
                new MapSqlParameterSource("templateNodeId", templateNodeId)
        );
    }

    private List<Map<String, Object>> queryCcs(Long templateNodeId) {
        if (templateNodeId == null) {
            return Collections.emptyList();
        }
        return jdbc.queryForList("""
                        SELECT id,
                               template_node_id AS templateNodeId,
                               cc_order AS ccOrder,
                               cc_type AS ccType,
                               cc_role_code AS ccRoleCode,
                               cc_user_id AS ccUserId,
                               cc_dept_id AS ccDeptId,
                               cc_timing AS ccTiming
                        FROM hr_workflow_template_node_cc
                        WHERE template_node_id = :templateNodeId
                          AND deleted = 0
                        ORDER BY cc_order ASC, id ASC
                        """,
                new MapSqlParameterSource("templateNodeId", templateNodeId)
        );
    }

    private void softDeleteRuntimeNodes(Long templateDbId) {
        List<Long> nodeIds = jdbc.queryForList("""
                        SELECT id
                        FROM hr_workflow_template_node
                        WHERE template_id = :templateId
                          AND deleted = 0
                        """,
                new MapSqlParameterSource("templateId", templateDbId),
                Long.class
        );
        jdbc.update("""
                        UPDATE hr_workflow_template_node
                        SET deleted = 1, updated_time = NOW()
                        WHERE template_id = :templateId AND deleted = 0
                        """,
                new MapSqlParameterSource("templateId", templateDbId)
        );
        if (!nodeIds.isEmpty()) {
            jdbc.update("""
                            UPDATE hr_workflow_template_node_approver
                            SET deleted = 1, updated_time = NOW()
                            WHERE template_node_id IN (:nodeIds) AND deleted = 0
                            """,
                    new MapSqlParameterSource("nodeIds", nodeIds)
            );
            jdbc.update("""
                            UPDATE hr_workflow_template_node_cc
                            SET deleted = 1, updated_time = NOW()
                            WHERE template_node_id IN (:nodeIds) AND deleted = 0
                            """,
                    new MapSqlParameterSource("nodeIds", nodeIds)
            );
        }
    }

    private Map<String, Object> buildLinearDefinitionFromRuntimeNodes(Long templateDbId) {
        if (templateDbId == null) {
            return defaultLinearDefinition();
        }
        List<Map<String, Object>> runtimeNodes = queryRuntimeNodes(templateDbId);
        if (runtimeNodes.isEmpty()) {
            return defaultLinearDefinition();
        }

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> starter = createDesignerNode("node_start", "starter", "发起人", 0, 40, defaultStarterConfig());
        nodes.add(starter);
        String previousNodeId = "node_start";
        int y = 210;
        int idx = 1;

        for (Map<String, Object> runtimeNode : runtimeNodes) {
            String runtimeType = nonEmptyOrDefault(stringValue(runtimeNode.get("nodeType")), "APPROVAL").toUpperCase(Locale.ROOT);
            String nodeId = "node_runtime_" + idx++;
            String nodeName = nonEmptyOrDefault(stringValue(runtimeNode.get("nodeName")), "审批节点");
            Map<String, Object> config;
            String designerType;
            if ("CC".equals(runtimeType)) {
                designerType = "cc";
                config = defaultCcConfig();
                List<Map<String, Object>> ccs = listValue(runtimeNode.get("ccs"));
                if (!ccs.isEmpty()) {
                    Map<String, Object> first = ccs.get(0);
                    String ccType = nonEmptyOrDefault(stringValue(first.get("ccType")), "SPECIFIED_USER").toUpperCase(Locale.ROOT);
                    if ("SPECIFIED_ROLE".equals(ccType) || "ROLE".equals(ccType)) {
                        config.put("targetType", "role");
                        config.put("roleIds", ccs.stream().map(item -> stringValue(item.get("ccRoleCode"))).filter(StringUtils::hasText).toList());
                    } else if ("SPECIFIED_USER".equals(ccType) || "USER".equals(ccType)) {
                        config.put("targetType", "user");
                        config.put("userIds", ccs.stream().map(item -> stringValue(item.get("ccUserId"))).filter(StringUtils::hasText).toList());
                    } else if ("SPECIFIED_DEPT".equals(ccType) || "DEPT".equals(ccType)) {
                        config.put("targetType", "department");
                    }
                }
            } else {
                designerType = "approval";
                config = defaultApprovalConfig();
                config.put("required", intValue(runtimeNode.get("requiredFlag"), 1) == 1);
                config.put("approvalMode", toDesignerApprovalMode(stringValue(runtimeNode.get("approvalMode"))));
                List<Map<String, Object>> approvers = listValue(runtimeNode.get("approvers"));
                if (!approvers.isEmpty()) {
                    Map<String, Object> first = approvers.get(0);
                    String approverType = nonEmptyOrDefault(stringValue(first.get("approverType")), "DIRECT_LEADER").toUpperCase(Locale.ROOT);
                    if ("SPECIFIED_ROLE".equals(approverType) || "ROLE".equals(approverType)) {
                        config.put("assigneeType", "role");
                        config.put("roleIds", approvers.stream().map(item -> stringValue(item.get("approverRoleCode"))).filter(StringUtils::hasText).toList());
                    } else if ("SPECIFIED_USER".equals(approverType) || "USER".equals(approverType)) {
                        config.put("assigneeType", "user");
                        config.put("userIds", approvers.stream().map(item -> stringValue(item.get("approverUserId"))).filter(StringUtils::hasText).toList());
                    } else if ("SELF".equals(approverType)) {
                        config.put("assigneeType", "self_select");
                    } else {
                        config.put("assigneeType", "direct_leader");
                    }
                }
            }

            Map<String, Object> designerNode = createDesignerNode(nodeId, designerType, nodeName, 0, y, config);
            nodes.add(designerNode);
            edges.add(createDesignerEdge("edge_" + previousNodeId + "_" + nodeId, previousNodeId, nodeId));
            previousNodeId = nodeId;
            y += 170;
        }

        Map<String, Object> endNode = createDesignerNode("node_end", "end", "流程结束", 0, y, defaultEndConfig());
        nodes.add(endNode);
        edges.add(createDesignerEdge("edge_" + previousNodeId + "_node_end", previousNodeId, "node_end"));

        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("nodes", nodes);
        definition.put("edges", edges);
        return definition;
    }

    private Map<String, Object> defaultLinearDefinition() {
        Map<String, Object> definition = new LinkedHashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(createDesignerNode("node_start", "starter", "发起人", 0, 40, defaultStarterConfig()));
        nodes.add(createDesignerNode("node_approval_1", "approval", "直属主管审批", 0, 210, defaultApprovalConfig()));
        nodes.add(createDesignerNode("node_end", "end", "流程结束", 0, 380, defaultEndConfig()));
        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(createDesignerEdge("edge_node_start_node_approval_1", "node_start", "node_approval_1"));
        edges.add(createDesignerEdge("edge_node_approval_1_node_end", "node_approval_1", "node_end"));
        definition.put("nodes", nodes);
        definition.put("edges", edges);
        return definition;
    }

    private Map<String, Object> buildSnapshot(String templateId, Map<String, Object> body, Map<String, Object> fallback) {
        Map<String, Object> source = body == null ? Collections.emptyMap() : body;
        Map<String, Object> snapshot = mapValue(source.get("snapshot"));
        if (snapshot.isEmpty() && fallback != null) {
            snapshot = mapValue(fallback.get("snapshot"));
        }
        if (snapshot.isEmpty()) {
            snapshot = new LinkedHashMap<>();
        } else {
            snapshot = new LinkedHashMap<>(snapshot);
        }

        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        if (definition.isEmpty()) {
            definition = mapValue(source.get("definition"));
        }
        if (definition.isEmpty()) {
            Object nodesObj = source.get("nodes");
            Object edgesObj = source.get("edges");
            if (nodesObj instanceof List<?> && edgesObj instanceof List<?>) {
                definition = new LinkedHashMap<>();
                definition.put("nodes", nodesObj);
                definition.put("edges", edgesObj);
            }
        }
        if (definition.isEmpty() && fallback != null) {
            definition = mapValue(fallback.get("definition"));
        }
        if (definition.isEmpty()) {
            definition = defaultLinearDefinition();
        }

        definition.put("nodes", listValue(definition.get("nodes")));
        definition.put("edges", listValue(definition.get("edges")));
        snapshot.put("definition", definition);

        Map<String, Object> layout = mapValue(snapshot.get("layout"));
        if (layout.isEmpty()) {
            layout = mapValue(source.get("layout"));
        }
        if (layout.isEmpty() && fallback != null) {
            layout = mapValue(fallback.get("layout"));
        }
        if (layout.isEmpty()) {
            layout = new LinkedHashMap<>();
            layout.put("manualPositions", true);
        }
        snapshot.put("layout", layout);

        Map<String, Object> viewport = mapValue(snapshot.get("viewport"));
        if (viewport.isEmpty()) {
            viewport = mapValue(source.get("viewport"));
        }
        if (viewport.isEmpty() && fallback != null) {
            viewport = mapValue(fallback.get("viewport"));
        }
        if (viewport.isEmpty()) {
            viewport = new LinkedHashMap<>();
            viewport.put("x", 0);
            viewport.put("y", 0);
            viewport.put("zoom", 1);
        }
        snapshot.put("viewport", viewport);

        String templateName = nonEmptyOrDefault(stringValue(source.get("templateName")), fallback == null ? "未命名流程模板" : stringValue(fallback.get("templateName")));
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(source.get("templateCode")), fallback == null ? buildTemplateCodeByTemplateId(templateId) : stringValue(fallback.get("templateCode"))));
        String category = nonEmptyOrDefault(stringValue(source.get("category")), fallback == null ? "通用" : stringValue(fallback.get("category")));
        String businessType = resolveBusinessType(source, category, fallback == null ? null : stringValue(fallback.get("businessType")));
        String status = normalizeClientStatus(nonEmptyOrDefault(stringValue(source.get("status")), fallback == null ? STATUS_DRAFT : stringValue(fallback.get("status"))));
        int version = intValue(source.get("version"), fallback == null ? 1 : intValue(fallback.get("version"), 1));

        overrideSnapshotMeta(snapshot, templateId, templateName, templateCode, category, status, version);
        snapshot.put("businessType", businessType);
        return snapshot;
    }

    private void overrideSnapshotMeta(
            Map<String, Object> snapshot,
            String templateId,
            String templateName,
            String templateCode,
            String category,
            String status,
            int version
    ) {
        snapshot.put("templateId", templateId);
        snapshot.put("templateName", templateName);
        snapshot.put("templateCode", templateCode);
        snapshot.put("category", category);
        snapshot.put("status", normalizeClientStatus(status));
        snapshot.put("version", version);
        Map<String, Object> meta = mapValue(snapshot.get("meta"));
        if (meta.isEmpty()) {
            meta = new LinkedHashMap<>();
        }
        meta.put("updatedAt", TIME_FORMATTER.format(LocalDateTime.now()));
        meta.put("updatedBy", currentOperatorName());
        snapshot.put("meta", meta);
    }

    private void insertVersion(
            String templateId,
            Integer versionNo,
            String actionType,
            String templateName,
            String clientStatus,
            String snapshotJson,
            String definitionJson,
            String layoutJson,
            String remark
    ) {
        jdbc.update("""
                        INSERT INTO hr_workflow_template_version
                            (template_id, version_no, action_type, template_name, status,
                             snapshot_json, definition_json, layout_json,
                             operator_id, operator_name, remark, created_at)
                        VALUES
                            (:templateId, :versionNo, :actionType, :templateName, :status,
                             :snapshotJson, :definitionJson, :layoutJson,
                             :operatorId, :operatorName, :remark, NOW())
                        """,
                new MapSqlParameterSource()
                        .addValue("templateId", templateId)
                        .addValue("versionNo", versionNo)
                        .addValue("actionType", actionType)
                        .addValue("templateName", templateName)
                        .addValue("status", normalizeClientStatus(clientStatus))
                        .addValue("snapshotJson", snapshotJson)
                        .addValue("definitionJson", definitionJson)
                        .addValue("layoutJson", layoutJson)
                        .addValue("operatorId", currentOperatorId())
                        .addValue("operatorName", currentOperatorName())
                        .addValue("remark", remark)
        );
    }

    private void ensureSchemaReady() {
        if (SCHEMA_READY.get()) {
            return;
        }
        synchronized (SCHEMA_READY) {
            if (SCHEMA_READY.get()) {
                return;
            }

            ensureTemplateVarcharColumn("template_id", 64);
            ensureTemplateVarcharColumn("template_code", 100);
            ensureTemplateVarcharColumn("category", 50);
            ensureTemplateColumn("current_version", "INT DEFAULT 1");
            ensureTemplateColumn("latest_definition_json", "LONGTEXT");
            ensureTemplateColumn("latest_layout_json", "LONGTEXT");
            ensureTemplateColumn("latest_snapshot_json", "LONGTEXT");
            ensureTemplateColumn("published_version", "INT DEFAULT NULL");
            ensureTemplateColumn("published_snapshot_json", "LONGTEXT");
            ensureTemplateColumn("created_by", "BIGINT DEFAULT NULL");
            ensureTemplateColumn("updated_by", "BIGINT DEFAULT NULL");

            jdbc.update("""
                            CREATE TABLE IF NOT EXISTS hr_workflow_template_version (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              template_id VARCHAR(64) NOT NULL,
                              version_no INT NOT NULL,
                              action_type VARCHAR(20) NOT NULL,
                              template_name VARCHAR(100) NOT NULL,
                              status VARCHAR(20) DEFAULT 'draft',
                              snapshot_json LONGTEXT,
                              definition_json LONGTEXT,
                              layout_json LONGTEXT,
                              operator_id BIGINT DEFAULT NULL,
                              operator_name VARCHAR(100) DEFAULT NULL,
                              remark VARCHAR(500) DEFAULT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_wf_tpl_ver_template_version (template_id, version_no),
                              KEY idx_wf_tpl_ver_template_id (template_id),
                              KEY idx_wf_tpl_ver_created_at (created_at)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                            """,
                    new MapSqlParameterSource()
            );

            jdbc.update("""
                            UPDATE hr_workflow_template
                            SET template_id = CONCAT('tpl_', id)
                            WHERE (template_id IS NULL OR template_id = '')
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource()
            );
            jdbc.update("""
                            UPDATE hr_workflow_template
                            SET template_code = UPPER(REPLACE(template_id, '-', '_'))
                            WHERE (template_code IS NULL OR template_code = '')
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource()
            );
            jdbc.update("""
                            UPDATE hr_workflow_template
                            SET category = COALESCE(NULLIF(category, ''), NULLIF(business_type, ''), '通用')
                            WHERE (category IS NULL OR category = '')
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource()
            );
            jdbc.update("""
                            UPDATE hr_workflow_template
                            SET category = COALESCE(NULLIF(business_type, ''), '通用')
                            WHERE deleted = 0
                              AND (category REGEXP '^\\\\?+$' OR category = '閫氱敤')
                            """,
                    new MapSqlParameterSource()
            );
            jdbc.update("""
                            UPDATE hr_workflow_template
                            SET current_version = IFNULL(NULLIF(current_version, 0), IFNULL(version_no, 1))
                            WHERE (current_version IS NULL OR current_version = 0)
                              AND deleted = 0
                            """,
                    new MapSqlParameterSource()
            );

            SCHEMA_READY.set(true);
        }
    }

    private void ensureTemplateVarcharColumn(String columnName, int length) {
        Long count = jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE table_schema = DATABASE()
                          AND table_name = 'hr_workflow_template'
                          AND column_name = :columnName
                        """,
                new MapSqlParameterSource("columnName", columnName),
                Long.class
        );
        if (count == null || count == 0) {
            jdbc.update(
                    "ALTER TABLE hr_workflow_template ADD COLUMN " + columnName + " VARCHAR(" + length + ") DEFAULT NULL",
                    new MapSqlParameterSource()
            );
            return;
        }

        String dataType = jdbc.queryForObject("""
                        SELECT LOWER(data_type)
                        FROM information_schema.COLUMNS
                        WHERE table_schema = DATABASE()
                          AND table_name = 'hr_workflow_template'
                          AND column_name = :columnName
                        LIMIT 1
                        """,
                new MapSqlParameterSource("columnName", columnName),
                String.class
        );

        if (!isTextDataType(dataType)) {
            jdbc.update(
                    "ALTER TABLE hr_workflow_template MODIFY COLUMN " + columnName + " VARCHAR(" + length + ") DEFAULT NULL",
                    new MapSqlParameterSource()
            );
            return;
        }

        Integer charLength = jdbc.queryForObject("""
                        SELECT character_maximum_length
                        FROM information_schema.COLUMNS
                        WHERE table_schema = DATABASE()
                          AND table_name = 'hr_workflow_template'
                          AND column_name = :columnName
                        LIMIT 1
                        """,
                new MapSqlParameterSource("columnName", columnName),
                Integer.class
        );
        if (charLength != null && charLength > 0 && charLength < length) {
            jdbc.update(
                    "ALTER TABLE hr_workflow_template MODIFY COLUMN " + columnName + " VARCHAR(" + length + ") DEFAULT NULL",
                    new MapSqlParameterSource()
            );
        }
    }

    private void ensureTemplateColumn(String columnName, String columnDefinition) {
        Long count = jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE table_schema = DATABASE()
                          AND table_name = 'hr_workflow_template'
                          AND column_name = :columnName
                        """,
                new MapSqlParameterSource("columnName", columnName),
                Long.class
        );
        if (count != null && count > 0) {
            return;
        }
        jdbc.update("ALTER TABLE hr_workflow_template ADD COLUMN " + columnName + " " + columnDefinition, new MapSqlParameterSource());
    }

    private boolean isTextDataType(String dataType) {
        if (!StringUtils.hasText(dataType)) {
            return false;
        }
        String type = dataType.trim().toLowerCase(Locale.ROOT);
        return type.equals("varchar")
                || type.equals("char")
                || type.equals("text")
                || type.equals("tinytext")
                || type.equals("mediumtext")
                || type.equals("longtext");
    }

    private boolean existsTemplateId(String templateId) {
        Long count = jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM hr_workflow_template
                        WHERE template_id = :templateId
                          AND deleted = 0
                        """,
                new MapSqlParameterSource("templateId", templateId),
                Long.class
        );
        return count != null && count > 0;
    }

    private String ensureUniqueTemplateCode(String templateCode, Long excludeId) {
        String candidate = templateCode;
        int seq = 1;
        while (existsTemplateCode(candidate, excludeId)) {
            candidate = templateCode + "_" + (++seq);
        }
        return candidate;
    }

    private boolean existsTemplateCode(String templateCode, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM hr_workflow_template
                WHERE template_code = :templateCode
                  AND deleted = 0
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("templateCode", templateCode);
        if (excludeId != null) {
            sql += " AND id <> :excludeId ";
            params.addValue("excludeId", excludeId);
        }
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    private String generateTemplateId() {
        return "tpl_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }

    private String buildTemplateCodeByTemplateId(String templateId) {
        return normalizeTemplateCode(templateId);
    }

    private String normalizeTemplateCode(String code) {
        if (!StringUtils.hasText(code)) {
            return "TEMPLATE_" + System.currentTimeMillis();
        }
        return code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
    }

    private String resolveBusinessType(Map<String, Object> payload, String category, String currentBusinessType) {
        String explicit = payload == null ? null : stringValue(payload.get("businessType"));
        if (StringUtils.hasText(explicit)) {
            return explicit.trim().toUpperCase(Locale.ROOT);
        }
        if (StringUtils.hasText(currentBusinessType)) {
            return currentBusinessType.trim().toUpperCase(Locale.ROOT);
        }

        String normalizedCategory = nonEmptyOrDefault(category, "通用").trim().toUpperCase(Locale.ROOT);
        return switch (normalizedCategory) {
            case "请假", "LEAVE" -> "LEAVE";
            case "补卡", "PATCH" -> "PATCH";
            case "加班", "OVERTIME" -> "OVERTIME";
            default -> "GENERAL";
        };
    }

    private String normalizeClientStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_DRAFT;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        if ("all".equals(value)) {
            return "all";
        }
        if ("enabled".equals(value) || "published".equals(value)) {
            return STATUS_PUBLISHED;
        }
        if ("disabled".equals(value)) {
            return STATUS_DISABLED;
        }
        return STATUS_DRAFT;
    }

    private String toDbStatus(String clientStatus) {
        String normalized = normalizeClientStatus(clientStatus);
        if (STATUS_PUBLISHED.equals(normalized)) {
            return "ENABLED";
        }
        if (STATUS_DISABLED.equals(normalized)) {
            return "DISABLED";
        }
        return "DRAFT";
    }

    private String toDbApprovalMode(String approvalMode) {
        if (!StringUtils.hasText(approvalMode)) {
            return "ANY";
        }
        String mode = approvalMode.trim().toLowerCase(Locale.ROOT);
        if ("all".equals(mode)) {
            return "ALL";
        }
        if ("sequence".equals(mode) || "sequential".equals(mode)) {
            return "SEQUENTIAL";
        }
        return "ANY";
    }

    private String toDesignerApprovalMode(String dbMode) {
        if (!StringUtils.hasText(dbMode)) {
            return "any_one";
        }
        String mode = dbMode.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(mode)) {
            return "all";
        }
        if ("SEQUENTIAL".equals(mode)) {
            return "sequence";
        }
        return "any_one";
    }

    private Map<String, Object> createDesignerNode(
            String id,
            String type,
            String name,
            int x,
            int y,
            Map<String, Object> config
    ) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", id);
        node.put("type", type);
        node.put("name", name);
        node.put("positionMode", "auto");
        node.put("position", Map.of("x", x, "y", y));
        node.put("config", config);
        return node;
    }

    private Map<String, Object> createDesignerEdge(String id, String source, String target) {
        Map<String, Object> edge = new LinkedHashMap<>();
        edge.put("id", id);
        edge.put("source", source);
        edge.put("target", target);
        return edge;
    }

    private Map<String, Object> defaultStarterConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("initiatorScopeType", "all");
        config.put("roleIds", List.of());
        config.put("departmentIds", List.of());
        config.put("userIds", List.of());
        config.put("remark", "");
        return config;
    }

    private Map<String, Object> defaultApprovalConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("approvalMode", "any_one");
        config.put("assigneeType", "direct_leader");
        config.put("leaderLevel", 1);
        config.put("roleIds", List.of());
        config.put("positionIds", List.of());
        config.put("userIds", List.of());
        config.put("required", true);
        config.put("timeoutHours", null);
        config.put("timeoutAction", "auto_pass");
        config.put("remark", "");
        return config;
    }

    private Map<String, Object> defaultCcConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("targetType", "user");
        config.put("roleIds", List.of());
        config.put("positionIds", List.of());
        config.put("userIds", List.of());
        config.put("canViewAllComments", false);
        config.put("remark", "");
        return config;
    }

    private Map<String, Object> defaultEndConfig() {
        return Map.of("displayText", "流程已结束");
    }

    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            log.warn("[workflow-template] parse json failed: {}", ex.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new RuntimeException("流程模板序列化失败", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        raw.forEach((k, v) -> map.put(String.valueOf(k), v));
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listValue(Object value) {
        if (!(value instanceof List<?> raw)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof Map<?, ?> mapItem) {
                Map<String, Object> mapped = new LinkedHashMap<>();
                mapItem.forEach((k, v) -> mapped.put(String.valueOf(k), v));
                list.add(mapped);
            }
        }
        return list;
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> raw)) {
            return List.of();
        }
        return raw.stream()
                .map(item -> item == null ? null : String.valueOf(item))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private Long currentOperatorId() {
        return SecurityUtils.getCurrentUserId();
    }

    private String currentOperatorName() {
        return nonEmptyOrDefault(SecurityUtils.getCurrentUsername(), "system");
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer intValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if ("true".equals(text) || "1".equals(text) || "yes".equals(text)) {
            return true;
        }
        if ("false".equals(text) || "0".equals(text) || "no".equals(text)) {
            return false;
        }
        return defaultValue;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String nonEmptyOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private boolean isNumeric(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (char ch : value.trim().toCharArray()) {
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    private Long parseLongSafely(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private record RuntimeTemplateNode(
            Integer nodeOrder,
            String nodeName,
            String nodeType,
            String approvalMode,
            String conditionExpression,
            Integer requiredFlag,
            List<RuntimeApprover> approvers,
            List<RuntimeCc> ccs
    ) {
    }

    private record RuntimeApprover(String approverType, String roleCode, Long userId) {
    }

    private record RuntimeCc(String ccType, String roleCode, Long userId, Long deptId, String ccTiming) {
    }
}
