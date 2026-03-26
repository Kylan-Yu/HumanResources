package com.hrms.system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        System.out.println("[workflow-auth-fix] controller permission annotation: hasAnyAuthority('*:*:*','workflow:template:manage')");
        System.out.println("[workflow-auth-fix] current authentication: " + SecurityContextHolder.getContext().getAuthentication());
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE t.deleted = 0 ");

        if (StringUtils.hasText(keyword)) {
            where.append(" AND (t.template_name LIKE :keyword OR t.template_code LIKE :keyword OR t.template_id LIKE :keyword) ");
            params.addValue("keyword", "%" + keyword.trim() + "%");
        }
        if (StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)) {
            where.append(" AND COALESCE(NULLIF(t.category,''), NULLIF(t.business_type,''), '通用') = :category ");
            params.addValue("category", category.trim());
        }

        String normalizedStatus = normalizeStatus(status);
        if (StringUtils.hasText(normalizedStatus) && !"all".equals(normalizedStatus)) {
            where.append(" AND (LOWER(t.status) = :status OR (:status = 'published' AND UPPER(t.status) = 'ENABLED') OR (:status = 'disabled' AND UPPER(t.status) = 'DISABLED')) ");
            params.addValue("status", normalizedStatus);
        }

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM hr_workflow_template t " + where, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);

        String sql = """
                SELECT t.id,
                       t.id AS templateId,
                       t.template_name AS templateName,
                       t.template_name AS templateCode,
                       t.business_type AS category,
                       t.status,
                       t.version_no AS version,
                       DATE_FORMAT(t.updated_time, '%Y-%m-%d %H:%i:%s') AS updatedAt
                FROM hr_workflow_template t
                """ + where + " ORDER BY t.updated_time DESC, t.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        list.forEach(item -> item.put("status", normalizeStatus(stringValue(item.get("status")))));
        return Result.success(PageResult.of(list, total, pageNum, pageSize));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<String>> categories() {
        List<String> categories = jdbc.queryForList("""
                SELECT DISTINCT business_type AS category
                FROM hr_workflow_template
                WHERE deleted = 0
                ORDER BY business_type ASC
                """, new MapSqlParameterSource(), String.class);
        return Result.success(categories);
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> detail(@PathVariable String templateId) {
        Map<String, Object> row = queryTemplateRow(templateId, false);
        if (row == null) {
            return Result.error("流程模板不存在");
        }
        log.info("[workflow-template] load raw fields templateId={}, templateName={}", templateId, row.get("template_name"));
        Map<String, Object> response = buildTemplateResponse(row);
        logLoad(templateId, response);
        return Result.success(response);
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String templateName = nonEmptyOrDefault(stringValue(body.get("templateName")), "未命名流程模板");
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(body.get("templateCode")), "TEMPLATE_" + System.currentTimeMillis()));
        String category = nonEmptyOrDefault(stringValue(body.get("category")), "通用");
        String templateId = nonEmptyOrDefault(stringValue(body.get("templateId")), "tpl_" + System.currentTimeMillis());

        while (existsTemplateId(templateId)) {
            templateId = "tpl_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        }

        String status = normalizeStatus(stringValue(body.get("status")));
        if (!StringUtils.hasText(status) || "all".equals(status)) {
            status = "draft";
        }

        Map<String, Object> snapshot = buildSnapshot(templateId, body, null);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));

        jdbc.update("""
                INSERT INTO hr_workflow_template
                    (template_id, template_name, business_type,
                     status, version_no,
                     remark,
                     created_by, updated_by, created_time, updated_time, deleted)
                VALUES
                    (:templateId, :templateName, :businessType,
                     :status, :versionNo,
                     :remark,
                     :createdBy, :updatedBy, NOW(), NOW(), 0)
                """, new MapSqlParameterSource()
                .addValue("templateId", templateId)
                .addValue("templateName", templateName)
                .addValue("businessType", category)
                .addValue("status", status)
                .addValue("versionNo", 1)
                .addValue("remark", remark)
                .addValue("createdBy", currentOperatorId())
                .addValue("updatedBy", currentOperatorId()));

        insertVersion(templateId, 1, "save", templateName, status, snapshotJson, definitionJson, layoutJson, null);
        return Result.success(buildTemplateResponse(queryTemplateRow(templateId, false)));
    }

    @PutMapping("/{templateId}")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> save(@PathVariable String templateId, @RequestBody Map<String, Object> body) {
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("流程模板不存在");
        }

        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = buildSnapshot(templateId, body, current);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String nextStatus = normalizeStatus(nonEmptyOrDefault(stringValue(body.get("status")), stringValue(row.get("status"))));
        String nextName = nonEmptyOrDefault(stringValue(body.get("templateName")), stringValue(row.get("template_name")));
        String nextCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(body.get("templateCode")), stringValue(row.get("template_code"))));
        String nextCategory = nonEmptyOrDefault(stringValue(body.get("category")), nonEmptyOrDefault(stringValue(row.get("category")), "通用"));
        log.info("[workflow-template] save request templateId={}, actionType=save, templateName={}", templateId, nextName);

        int updatedRows = jdbc.update("""
                UPDATE hr_workflow_template
                SET template_name = :templateName,
                    template_code = :templateCode,
                    category = :category,
                    business_type = :businessType,
                    status = :status,
                    current_version = :currentVersion,
                    version_no = :currentVersion,
                    latest_definition_json = :definitionJson,
                    latest_layout_json = :layoutJson,
                    latest_snapshot_json = :snapshotJson,
                    updated_by = :updatedBy,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """, new MapSqlParameterSource()
                .addValue("id", row.get("id"))
                .addValue("templateName", nextName)
                .addValue("templateCode", nextCode)
                .addValue("category", nextCategory)
                .addValue("businessType", nextCategory)
                .addValue("status", nextStatus)
                .addValue("currentVersion", nextVersion)
                .addValue("definitionJson", definitionJson)
                .addValue("layoutJson", layoutJson)
                .addValue("snapshotJson", snapshotJson)
                .addValue("updatedBy", currentOperatorId()));

        insertVersion(templateId, nextVersion, "save", nextName, nextStatus, snapshotJson, definitionJson, layoutJson, null);
        log.info("[workflow-template] save templateId={}, actionType=save, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        return Result.success(buildTemplateResponse(queryTemplateRow(templateId, false)));
    }
    @PostMapping("/{templateId}/publish")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> publish(@PathVariable String templateId, @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("流程模板不存在");
        }

        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> payload = body == null ? Collections.emptyMap() : body;
        Map<String, Object> snapshot = buildSnapshot(templateId, payload, current);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String nextName = nonEmptyOrDefault(stringValue(payload.get("templateName")), stringValue(row.get("template_name")));
        log.info("[workflow-template] publish request templateId={}, actionType=publish, templateName={}", templateId, nextName);

        int updatedRows = jdbc.update("""
                UPDATE hr_workflow_template
                SET status = 'published',
                    template_name = :templateName,
                    current_version = :currentVersion,
                    version_no = :currentVersion,
                    published_version = :currentVersion,
                    latest_definition_json = :definitionJson,
                    latest_layout_json = :layoutJson,
                    latest_snapshot_json = :snapshotJson,
                    published_snapshot_json = :snapshotJson,
                    updated_by = :updatedBy,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """, new MapSqlParameterSource()
                .addValue("id", row.get("id"))
                .addValue("templateName", nextName)
                .addValue("currentVersion", nextVersion)
                .addValue("definitionJson", definitionJson)
                .addValue("layoutJson", layoutJson)
                .addValue("snapshotJson", snapshotJson)
                .addValue("updatedBy", currentOperatorId()));

        insertVersion(templateId, nextVersion, "publish", nextName, "published", snapshotJson, definitionJson, layoutJson, null);
        log.info("[workflow-template] publish templateId={}, actionType=publish, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        Map<String, Object> detail = buildTemplateResponse(queryTemplateRow(templateId, false));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templateId", templateId);
        result.put("publishTime", detail.get("updatedAt"));
        result.put("version", detail.get("version"));
        result.put("data", detail);
        return Result.success(result);
    }

    @PostMapping("/{templateId}/duplicate")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> duplicate(@PathVariable String templateId) {
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("模板不存在，无法复制");
        }

        String sourceCode = nonEmptyOrDefault(stringValue(row.get("template_code")), "TEMPLATE");
        String sourceName = nonEmptyOrDefault(stringValue(row.get("template_name")), "流程模板");
        String sourceCategory = nonEmptyOrDefault(stringValue(row.get("category")), "通用");

        String newTemplateId = "tpl_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        while (existsTemplateId(newTemplateId)) {
            newTemplateId = "tpl_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        }

        String newTemplateCode = generateCopyTemplateCode(sourceCode);
        String snapshotJson = stringValue(row.get("latest_snapshot_json"));
        String definitionJson = stringValue(row.get("latest_definition_json"));
        String layoutJson = stringValue(row.get("latest_layout_json"));

        jdbc.update("""
                INSERT INTO hr_workflow_template
                    (template_id, template_name, template_code, category, business_type,
                     status, current_version, version_no,
                     latest_definition_json, latest_layout_json, latest_snapshot_json,
                     created_by, updated_by, created_time, updated_time, deleted)
                VALUES
                    (:templateId, :templateName, :templateCode, :category, :businessType,
                     'draft', 1, 1,
                     :definitionJson, :layoutJson, :snapshotJson,
                     :createdBy, :updatedBy, NOW(), NOW(), 0)
                """, new MapSqlParameterSource()
                .addValue("templateId", newTemplateId)
                .addValue("templateName", sourceName + "-副本")
                .addValue("templateCode", newTemplateCode)
                .addValue("category", sourceCategory)
                .addValue("businessType", sourceCategory)
                .addValue("definitionJson", definitionJson)
                .addValue("layoutJson", layoutJson)
                .addValue("snapshotJson", snapshotJson)
                .addValue("createdBy", currentOperatorId())
                .addValue("updatedBy", currentOperatorId()));

        insertVersion(newTemplateId, 1, "save", sourceName + "-副本", "draft", snapshotJson, definitionJson, layoutJson, "复制模板");
        return Result.success(buildTemplateResponse(queryTemplateRow(newTemplateId, false)));
    }

    @DeleteMapping("/{templateId}")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Boolean> delete(@PathVariable String templateId) {
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("模板不存在");
        }

        int rows = jdbc.update("UPDATE hr_workflow_template SET deleted = 1, updated_time = NOW(), updated_by = :updatedBy WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", row.get("id")).addValue("updatedBy", currentOperatorId()));
        return Result.success(rows > 0);
    }

    @GetMapping("/{templateId}/versions")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<List<Map<String, Object>>> versions(@PathVariable String templateId) {
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
                """, new MapSqlParameterSource("templateId", templateId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>(row);
            item.put("status", normalizeStatus(stringValue(row.get("status"))));
            Map<String, Object> snapshot = parseJson(stringValue(row.get("snapshotJson")));
            item.put("snapshot", snapshot);
            item.put("payload", snapshot);
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/{templateId}/versions/{versionNo}")
    @PreAuthorize("hasAnyAuthority('*:*:*','workflow:template:manage')")
    public Result<Map<String, Object>> versionDetail(@PathVariable String templateId, @PathVariable Integer versionNo) {
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
                """, new MapSqlParameterSource().addValue("templateId", templateId).addValue("versionNo", versionNo));

        if (rows.isEmpty()) {
            return Result.error("历史版本不存在");
        }

        Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
        row.put("status", normalizeStatus(stringValue(row.get("status"))));
        Map<String, Object> snapshot = parseJson(stringValue(row.get("snapshotJson")));
        row.put("snapshot", snapshot);
        row.put("payload", snapshot);
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
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT version_no, template_name, status, snapshot_json, definition_json, layout_json
                FROM hr_workflow_template_version
                WHERE template_id = :templateId AND version_no = :versionNo
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource().addValue("templateId", templateId).addValue("versionNo", versionNo));

        if (rows.isEmpty()) {
            return Result.error("历史版本不存在，无法恢复");
        }

        Map<String, Object> targetVersion = rows.get(0);
        String snapshotJson = stringValue(targetVersion.get("snapshot_json"));
        String definitionJson = stringValue(targetVersion.get("definition_json"));
        String layoutJson = stringValue(targetVersion.get("layout_json"));
        if (!StringUtils.hasText(snapshotJson)) {
            return Result.error("历史版本快照为空，无法恢复");
        }

        Map<String, Object> templateRow = queryTemplateRow(templateId, true);
        if (templateRow == null) {
            return Result.error("流程模板不存在");
        }

        int nextVersion = intValue(templateRow.get("current_version"), intValue(templateRow.get("version_no"), 1)) + 1;
        String restoredStatus = normalizeStatus(nonEmptyOrDefault(stringValue(targetVersion.get("status")), stringValue(templateRow.get("status"))));
        String restoredName = nonEmptyOrDefault(stringValue(targetVersion.get("template_name")), stringValue(templateRow.get("template_name")));

        int updatedRows = jdbc.update("""
                UPDATE hr_workflow_template
                SET status = :status,
                    template_name = :templateName,
                    current_version = :currentVersion,
                    version_no = :currentVersion,
                    latest_definition_json = :definitionJson,
                    latest_layout_json = :layoutJson,
                    latest_snapshot_json = :snapshotJson,
                    updated_by = :updatedBy,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """, new MapSqlParameterSource()
                .addValue("id", templateRow.get("id"))
                .addValue("status", restoredStatus)
                .addValue("templateName", restoredName)
                .addValue("currentVersion", nextVersion)
                .addValue("definitionJson", definitionJson)
                .addValue("layoutJson", layoutJson)
                .addValue("snapshotJson", snapshotJson)
                .addValue("updatedBy", currentOperatorId()));

        String remark = body == null ? null : stringValue(body.get("remark"));
        insertVersion(templateId, nextVersion, "restore", restoredName, restoredStatus, snapshotJson, definitionJson, layoutJson, remark);

        Map<String, Object> snapshot = parseJson(snapshotJson);
        log.info("[workflow-template] restore templateId={}, actionType=restore, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        return Result.success(buildTemplateResponse(queryTemplateRow(templateId, false)));
    }
    private void insertVersion(String templateId, Integer versionNo, String actionType, String templateName, String status,
                               String snapshotJson, String definitionJson, String layoutJson, String remark) {
        jdbc.update("""
                INSERT INTO hr_workflow_template_version
                    (template_id, version_no, action_type, template_name, status,
                     snapshot_json, definition_json, layout_json,
                     operator_id, operator_name, remark, created_at)
                VALUES
                    (:templateId, :versionNo, :actionType, :templateName, :status,
                     :snapshotJson, :definitionJson, :layoutJson,
                     :operatorId, :operatorName, :remark, NOW())
                """, new MapSqlParameterSource()
                .addValue("templateId", templateId)
                .addValue("versionNo", versionNo)
                .addValue("actionType", actionType)
                .addValue("templateName", templateName)
                .addValue("status", status)
                .addValue("snapshotJson", snapshotJson)
                .addValue("definitionJson", definitionJson)
                .addValue("layoutJson", layoutJson)
                .addValue("operatorId", currentOperatorId())
                .addValue("operatorName", currentOperatorName())
                .addValue("remark", remark));
    }

    private Map<String, Object> queryTemplateRow(String templateId, boolean forUpdate) {
        Long numericId = isNumeric(templateId) ? Long.valueOf(templateId) : null;

        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       template_id,
                       template_name,
                       business_type,
                       status,
                       version_no,
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
        String templateCode = normalizeTemplateCode(templateId);
        String category = nonEmptyOrDefault(stringValue(row.get("business_type")), "通用");
        String status = normalizeStatus(stringValue(row.get("status")));
        Integer version = intValue(row.get("version_no"), 1);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("nodes", new ArrayList<>());
        definition.put("edges", new ArrayList<>());
        
        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("manualPositions", true);
        
        Map<String, Object> viewport = new LinkedHashMap<>();
        viewport.put("x", 0);
        viewport.put("y", 0);
        viewport.put("zoom", 1);

        snapshot.put("templateId", templateId);
        snapshot.put("templateName", templateName);
        snapshot.put("templateCode", templateCode);
        snapshot.put("category", category);
        snapshot.put("status", status);
        snapshot.put("version", version);
        snapshot.put("definition", definition);
        snapshot.put("layout", layout);
        snapshot.put("viewport", viewport);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("templateId", templateId);
        response.put("templateName", templateName);
        response.put("templateCode", templateCode);
        response.put("category", category);
        response.put("status", status);
        response.put("version", version);
        response.put("currentVersion", version);
        response.put("publishedVersion", 0);
        response.put("updatedAt", nonEmptyOrDefault(stringValue(row.get("updated_time")), TIME_FORMATTER.format(LocalDateTime.now())));
        response.put("definition", definition);
        response.put("nodes", definition.get("nodes"));
        response.put("edges", definition.get("edges"));
        response.put("layout", layout);
        response.put("viewport", viewport);
        response.put("snapshot", snapshot);
        return response;
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

        String templateName = nonEmptyOrDefault(stringValue(source.get("templateName")), fallback == null ? "未命名流程模板" : stringValue(fallback.get("templateName")));
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(source.get("templateCode")), fallback == null ? templateId : stringValue(fallback.get("templateCode"))));
        String category = nonEmptyOrDefault(stringValue(source.get("category")), fallback == null ? "通用" : stringValue(fallback.get("category")));
        String status = normalizeStatus(nonEmptyOrDefault(stringValue(source.get("status")), fallback == null ? "draft" : stringValue(fallback.get("status"))));
        Integer version = intValue(source.get("version"), fallback == null ? 1 : intValue(fallback.get("version"), 1));

        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        if (definition.isEmpty()) {
            definition = mapValue(source.get("definition"));
        }
        if (definition.isEmpty() && source.get("nodes") instanceof List<?> nodes && source.get("edges") instanceof List<?> edges) {
            definition = new LinkedHashMap<>();
            definition.put("nodes", nodes);
            definition.put("edges", edges);
        }
        if (definition.isEmpty() && fallback != null) {
            definition = mapValue(fallback.get("definition"));
        }
        if (definition.isEmpty()) {
            definition = new LinkedHashMap<>();
        }
        definition.put("nodes", listValue(definition.get("nodes")));
        definition.put("edges", listValue(definition.get("edges")));

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

        snapshot.put("templateId", templateId);
        snapshot.put("templateName", templateName);
        snapshot.put("templateCode", templateCode);
        snapshot.put("category", category);
        snapshot.put("status", status);
        snapshot.put("version", version);
        snapshot.put("definition", definition);
        snapshot.put("layout", layout);
        snapshot.put("viewport", viewport);

        Map<String, Object> meta = mapValue(snapshot.get("meta"));
        if (meta.isEmpty()) {
            meta = new LinkedHashMap<>();
        }
        meta.put("updatedAt", TIME_FORMATTER.format(LocalDateTime.now()));
        meta.put("updatedBy", currentOperatorName());
        snapshot.put("meta", meta);
        return snapshot;
    }

    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
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
            throw new RuntimeException("序列化流程快照失败", ex);
        }
    }

    private void logLoad(String templateId, Map<String, Object> response) {
        int nodeCount = listValue(response.get("nodes")).size();
        int edgeCount = listValue(response.get("edges")).size();
        log.info("[workflow-template] load templateId={}, latestSnapshotEmpty={}, nodeCount={}, edgeCount={}",
                templateId,
                response.get("snapshot") == null,
                nodeCount,
                edgeCount);
    }

    private int countNodes(Map<String, Object> snapshot) {
        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        return listValue(definition.get("nodes")).size();
    }

    private int countEdges(Map<String, Object> snapshot) {
        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        return listValue(definition.get("edges")).size();
    }

    private boolean existsTemplateId(String templateId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM hr_workflow_template WHERE template_id = :templateId AND deleted = 0",
                new MapSqlParameterSource("templateId", templateId), Long.class);
        return count != null && count > 0;
    }

    private boolean existsTemplateCode(String templateCode) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM hr_workflow_template WHERE business_type = :templateCode AND deleted = 0",
                new MapSqlParameterSource("templateCode", templateCode), Long.class);
        return count != null && count > 0;
    }

    private String generateCopyTemplateCode(String sourceCode) {
        String base = normalizeTemplateCode(sourceCode + "_COPY");
        String candidate = base;
        int seq = 1;
        while (existsTemplateCode(candidate)) {
            seq += 1;
            candidate = base + "_" + seq;
        }
        return candidate;
    }

    private String normalizeTemplateCode(String code) {
        if (!StringUtils.hasText(code)) {
            return "TEMPLATE_" + System.currentTimeMillis();
        }
        return code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "draft";
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        if ("enabled".equals(value)) return "published";
        if ("disabled".equals(value)) return "disabled";
        if ("published".equals(value)) return "published";
        if ("draft".equals(value)) return "draft";
        if ("all".equals(value)) return "all";
        return "draft";
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
            } else {
                Map<String, Object> mapped = new LinkedHashMap<>();
                mapped.put("value", item);
                list.add(mapped);
            }
        }
        return list;
    }

    private Long currentOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Map<?, ?> principalMap) {
            Object id = principalMap.get("id");
            if (id != null && isNumeric(String.valueOf(id))) {
                return Long.valueOf(String.valueOf(id));
            }
        }
        return null;
    }

    private String currentOperatorName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "system";
        }
        return authentication.getName();
    }

    private String nonEmptyOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int intValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
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
}
