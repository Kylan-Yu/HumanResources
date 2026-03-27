package com.hrms.system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.PageResult;
import com.hrms.common.Result;
import com.hrms.common.ResultCode;
import com.hrms.system.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowTemplateService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern PROCESS_KEY_PATTERN = Pattern.compile("^([a-zA-Z0-9]+)_process_(\\d+)$");
    private static final Pattern PROCESS_CODE_PATTERN = Pattern.compile("^([A-Z0-9]+)_PROCESS_\\d+$");
    private static final Pattern QUESTION_MARK_ONLY_PATTERN = Pattern.compile("^[?？]+$");
    private static final Set<String> KNOWN_GARBLED_NODE_NAMES = Set.of("鐩村睘涓荤瀹℃壒");

    private final WorkflowTemplateRepository jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object workflowSchemaLock = new Object();
    private volatile boolean workflowSchemaPrepared = false;

    public Result<PageResult<Map<String, Object>>> page(
            Integer pageNum,
            Integer pageSize,
            String keyword,
            String category,
            String status
    ) {
        ensureWorkflowSchemaPrepared();
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 200);
        Set<String> columns = getWorkflowTemplateColumns();
        String categoryExpr = hasColumn(columns, "category")
                ? "COALESCE(NULLIF(t.category,''), NULLIF(t.business_type,''), 'general')"
                : "COALESCE(NULLIF(t.business_type,''), 'general')";
        String templateCodeExpr = hasColumn(columns, "template_code")
                ? "t.template_code"
                : "UPPER(REPLACE(COALESCE(NULLIF(t.business_type,''), CONCAT('TEMPLATE_', t.id)), '-', '_'))";
        String versionExpr = hasColumn(columns, "current_version")
                ? "COALESCE(t.current_version, t.version_no, 1)"
                : "COALESCE(t.version_no, 1)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE t.deleted = 0 ");

        if (StringUtils.hasText(keyword)) {
            where.append(" AND (t.template_name LIKE :keyword OR t.business_type LIKE :keyword ");
            if (hasColumn(columns, "template_code")) {
                where.append(" OR t.template_code LIKE :keyword ");
            }
            if (hasColumn(columns, "template_id")) {
                where.append(" OR t.template_id LIKE :keyword ");
            }
            where.append(") ");
            params.addValue("keyword", "%" + keyword.trim() + "%");
        }
        if (StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)) {
            where.append(" AND ").append(categoryExpr).append(" = :category ");
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

        params.addValue("limit", safePageSize);
        params.addValue("offset", (safePageNum - 1) * safePageSize);

        String sql = "SELECT t.id, "
                + (hasColumn(columns, "template_id") ? "t.template_id" : "CAST(t.id AS CHAR)") + " AS templateId, "
                + "t.template_name AS templateName, "
                + templateCodeExpr + " AS templateCode, "
                + categoryExpr + " AS category, "
                + "t.status, "
                + versionExpr + " AS version, "
                + "DATE_FORMAT(t.updated_time, '%Y-%m-%d %H:%i:%s') AS updatedAt "
                + "FROM hr_workflow_template t"
                + where
                + " ORDER BY t.updated_time DESC, t.id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        list.forEach(item -> item.put("status", normalizeStatus(stringValue(item.get("status")))));
        return Result.success(PageResult.of(list, total, safePageNum, safePageSize));
    }

    public Result<List<String>> categories() {
        ensureWorkflowSchemaPrepared();
        Set<String> columns = getWorkflowTemplateColumns();
        String sql = hasColumn(columns, "category")
                ? """
                SELECT DISTINCT COALESCE(NULLIF(category,''), NULLIF(business_type,''), 'general') AS category
                FROM hr_workflow_template
                WHERE deleted = 0
                ORDER BY category ASC
                """
                : """
                SELECT DISTINCT COALESCE(NULLIF(business_type,''), 'general') AS category
                FROM hr_workflow_template
                WHERE deleted = 0
                ORDER BY category ASC
                """;
        List<String> categories = jdbc.queryForList(sql, new MapSqlParameterSource(), String.class);
        return Result.success(categories);
    }

    public Result<Map<String, Object>> detail(String templateId) {
        ensureWorkflowSchemaPrepared();
        Map<String, Object> row = queryTemplateRow(templateId, false);
        if (row == null) {
            return Result.error("Template not found");
        }
        log.info("[workflow-template] load raw fields templateId={}, templateName={}", templateId, row.get("template_name"));
        Map<String, Object> response = buildTemplateResponse(row);
        logLoad(templateId, response);
        return Result.success(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> create(Map<String, Object> body) {
        ensureWorkflowSchemaPrepared();
        Map<String, Object> payload = body == null ? Collections.emptyMap() : body;
        Set<String> columns = getWorkflowTemplateColumns();
        String templateName = sanitizeReadableText(stringValue(payload.get("templateName")), null, "Untitled Workflow Template");
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(payload.get("templateCode")), "TEMPLATE_" + System.currentTimeMillis()));
        String templateId = nonEmptyOrDefault(stringValue(payload.get("templateId")), templateCode.toLowerCase(Locale.ROOT));
        templateId = normalizeTemplateBusinessKey(templateId);
        String businessType = resolveBusinessTypeForWrite(payload, null, templateId, templateCode);
        String category = resolveCategoryForWrite(payload, null, businessType, "general");

        if (existsTemplateId(templateId)) {
            return Result.error(ResultCode.DATA_EXISTS.getCode(), "模板标识已存在，请更换模板编码");
        }
        if (existsTemplateCode(templateCode)) {
            return Result.error(ResultCode.DATA_EXISTS.getCode(), "模板编码已存在，请更换模板编码");
        }

        String status = normalizeStatus(stringValue(payload.get("status")));
        if (!StringUtils.hasText(status) || "all".equals(status)) {
            status = "draft";
        }

        Map<String, Object> snapshot = buildSnapshot(templateId, payload, null);
        snapshot.put("templateName", templateName);
        snapshot.put("templateCode", templateCode);
        snapshot.put("category", category);
        snapshot.put("businessType", businessType);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));
        String remark = stringValue(payload.get("remark"));

        Map<String, Object> insertValues = new LinkedHashMap<>();
        putIfColumn(columns, insertValues, "template_id", templateId);
        putIfColumn(columns, insertValues, "template_name", templateName);
        putIfColumn(columns, insertValues, "template_code", templateCode);
        putIfColumn(columns, insertValues, "category", category);
        putIfColumn(columns, insertValues, "business_type", businessType);
        putIfColumn(columns, insertValues, "status", status);
        putIfColumn(columns, insertValues, "current_version", 1);
        putIfColumn(columns, insertValues, "version_no", 1);
        putIfColumn(columns, insertValues, "latest_definition_json", definitionJson);
        putIfColumn(columns, insertValues, "latest_layout_json", layoutJson);
        putIfColumn(columns, insertValues, "latest_snapshot_json", snapshotJson);
        putIfColumn(columns, insertValues, "remark", remark);
        putIfColumn(columns, insertValues, "created_by", currentOperatorId());
        putIfColumn(columns, insertValues, "updated_by", currentOperatorId());
        putIfColumn(columns, insertValues, "deleted", 0);

        long insertedId = insertTemplate(insertValues);
        String versionTemplateKey = hasColumn(columns, "template_id") ? templateId : String.valueOf(insertedId);
        insertVersion(versionTemplateKey, 1, "save", templateName, status, snapshotJson, definitionJson, layoutJson, null);
        return Result.success(buildTemplateResponse(queryTemplateRow(String.valueOf(insertedId), false)));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> save(String templateId, Map<String, Object> body) {
        ensureWorkflowSchemaPrepared();
        Map<String, Object> payload = body == null ? Collections.emptyMap() : body;
        Set<String> columns = getWorkflowTemplateColumns();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("Template not found");
        }
        String identityError = validatePayloadIdentity(templateId, row, payload);
        if (identityError != null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), identityError);
        }

        String currentTemplateId = nonEmptyOrDefault(stringValue(row.get("template_id")), templateId);
        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = buildSnapshot(currentTemplateId, payload, current);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String nextStatus = normalizeStatus(nonEmptyOrDefault(stringValue(payload.get("status")), stringValue(row.get("status"))));
        String historyTemplateName = resolveReadableValueFromHistory(currentTemplateId, "templateName");
        String nextName = sanitizeReadableText(
                stringValue(payload.get("templateName")),
                firstReadableText(stringValue(row.get("template_name")), historyTemplateName),
                "Untitled Workflow Template"
        );
        String nextCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(payload.get("templateCode")), stringValue(row.get("template_code"))));
        String nextBusinessType = resolveBusinessTypeForWrite(payload, row, currentTemplateId, nextCode);
        String historyCategory = resolveReadableValueFromHistory(currentTemplateId, "category");
        String nextCategory = resolveCategoryForWrite(
                payload,
                row,
                nextBusinessType,
                firstReadableText(historyCategory, "general")
        );
        snapshot.put("templateName", nextName);
        snapshot.put("templateCode", nextCode);
        snapshot.put("category", nextCategory);
        snapshot.put("businessType", nextBusinessType);
        snapshot.put("status", nextStatus);
        snapshot.put("version", nextVersion);
        snapshotJson = toJson(snapshot);
        definitionJson = toJson(snapshot.get("definition"));
        layoutJson = toJson(snapshot.get("layout"));
        log.info("[workflow-template] save request templateId={}, actionType=save, templateName={}", templateId, nextName);

        Map<String, Object> updateValues = new LinkedHashMap<>();
        putIfColumn(columns, updateValues, "template_name", nextName);
        putIfColumn(columns, updateValues, "template_code", nextCode);
        putIfColumn(columns, updateValues, "category", nextCategory);
        putIfColumn(columns, updateValues, "business_type", nextBusinessType);
        putIfColumn(columns, updateValues, "status", nextStatus);
        putIfColumn(columns, updateValues, "current_version", nextVersion);
        putIfColumn(columns, updateValues, "version_no", nextVersion);
        putIfColumn(columns, updateValues, "latest_definition_json", definitionJson);
        putIfColumn(columns, updateValues, "latest_layout_json", layoutJson);
        putIfColumn(columns, updateValues, "latest_snapshot_json", snapshotJson);
        putIfColumn(columns, updateValues, "updated_by", currentOperatorId());

        int updatedRows = updateTemplateById(toLong(row.get("id")), updateValues);

        insertVersion(currentTemplateId, nextVersion, "save", nextName, nextStatus, snapshotJson, definitionJson, layoutJson, null);
        log.info("[workflow-template] save templateId={}, actionType=save, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        return Result.success(buildTemplateResponse(queryTemplateRow(String.valueOf(row.get("id")), false)));
    }
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> publish(String templateId, Map<String, Object> body) {
        ensureWorkflowSchemaPrepared();
        Set<String> columns = getWorkflowTemplateColumns();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("Template not found");
        }
        Map<String, Object> payload = body == null ? Collections.emptyMap() : body;
        String identityError = validatePayloadIdentity(templateId, row, payload);
        if (identityError != null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), identityError);
        }

        String currentTemplateId = nonEmptyOrDefault(stringValue(row.get("template_id")), templateId);
        Map<String, Object> current = buildTemplateResponse(row);
        Map<String, Object> snapshot = buildSnapshot(currentTemplateId, payload, current);
        String snapshotJson = toJson(snapshot);
        String definitionJson = toJson(snapshot.get("definition"));
        String layoutJson = toJson(snapshot.get("layout"));

        int nextVersion = intValue(row.get("current_version"), intValue(row.get("version_no"), 1)) + 1;
        String historyTemplateName = resolveReadableValueFromHistory(currentTemplateId, "templateName");
        String nextName = sanitizeReadableText(
                stringValue(payload.get("templateName")),
                firstReadableText(stringValue(row.get("template_name")), historyTemplateName),
                "Untitled Workflow Template"
        );
        String nextCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(payload.get("templateCode")), stringValue(row.get("template_code"))));
        String nextBusinessType = resolveBusinessTypeForWrite(payload, row, currentTemplateId, nextCode);
        String historyCategory = resolveReadableValueFromHistory(currentTemplateId, "category");
        String nextCategory = resolveCategoryForWrite(
                payload,
                row,
                nextBusinessType,
                firstReadableText(historyCategory, "general")
        );
        snapshot.put("templateName", nextName);
        snapshot.put("templateCode", nextCode);
        snapshot.put("category", nextCategory);
        snapshot.put("businessType", nextBusinessType);
        snapshot.put("status", "published");
        snapshot.put("version", nextVersion);
        snapshotJson = toJson(snapshot);
        definitionJson = toJson(snapshot.get("definition"));
        layoutJson = toJson(snapshot.get("layout"));
        log.info("[workflow-template] publish request templateId={}, actionType=publish, templateName={}", templateId, nextName);

        Map<String, Object> updateValues = new LinkedHashMap<>();
        putIfColumn(columns, updateValues, "template_name", nextName);
        putIfColumn(columns, updateValues, "template_code", nextCode);
        putIfColumn(columns, updateValues, "category", nextCategory);
        putIfColumn(columns, updateValues, "business_type", nextBusinessType);
        putIfColumn(columns, updateValues, "status", "published");
        putIfColumn(columns, updateValues, "current_version", nextVersion);
        putIfColumn(columns, updateValues, "version_no", nextVersion);
        putIfColumn(columns, updateValues, "published_version", nextVersion);
        putIfColumn(columns, updateValues, "latest_definition_json", definitionJson);
        putIfColumn(columns, updateValues, "latest_layout_json", layoutJson);
        putIfColumn(columns, updateValues, "latest_snapshot_json", snapshotJson);
        putIfColumn(columns, updateValues, "published_snapshot_json", snapshotJson);
        putIfColumn(columns, updateValues, "updated_by", currentOperatorId());

        int updatedRows = updateTemplateById(toLong(row.get("id")), updateValues);

        insertVersion(currentTemplateId, nextVersion, "publish", nextName, "published", snapshotJson, definitionJson, layoutJson, null);
        log.info("[workflow-template] publish templateId={}, actionType=publish, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        Map<String, Object> detail = buildTemplateResponse(queryTemplateRow(String.valueOf(row.get("id")), false));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templateId", detail.get("templateId"));
        result.put("publishTime", detail.get("updatedAt"));
        result.put("version", detail.get("version"));
        result.put("data", detail);
        return Result.success(result);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> duplicate(String templateId) {
        ensureWorkflowSchemaPrepared();
        Set<String> columns = getWorkflowTemplateColumns();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("Template not found");
        }

        String sourceCode = nonEmptyOrDefault(stringValue(row.get("template_code")), "TEMPLATE");
        String sourceName = nonEmptyOrDefault(stringValue(row.get("template_name")), "Workflow Template");
        String sourceBusinessType = resolveBusinessTypeForWrite(Collections.emptyMap(), row, templateId, sourceCode);
        String sourceCategory = resolveCategoryForWrite(Collections.emptyMap(), row, sourceBusinessType, "general");
        String sourceTemplateId = nonEmptyOrDefault(stringValue(row.get("template_id")), templateId);

        String newTemplateId = normalizeTemplateBusinessKey(sourceTemplateId + "_copy");
        while (existsTemplateId(newTemplateId)) {
            newTemplateId = "tpl_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        }

        String newTemplateCode = generateCopyTemplateCode(sourceCode);
        String snapshotJson = stringValue(row.get("latest_snapshot_json"));
        String definitionJson = stringValue(row.get("latest_definition_json"));
        String layoutJson = stringValue(row.get("latest_layout_json"));

        Map<String, Object> insertValues = new LinkedHashMap<>();
        putIfColumn(columns, insertValues, "template_id", newTemplateId);
        putIfColumn(columns, insertValues, "template_name", sourceName + "-COPY");
        putIfColumn(columns, insertValues, "template_code", newTemplateCode);
        putIfColumn(columns, insertValues, "category", sourceCategory);
        putIfColumn(columns, insertValues, "business_type", sourceBusinessType);
        putIfColumn(columns, insertValues, "status", "draft");
        putIfColumn(columns, insertValues, "current_version", 1);
        putIfColumn(columns, insertValues, "version_no", 1);
        putIfColumn(columns, insertValues, "latest_definition_json", definitionJson);
        putIfColumn(columns, insertValues, "latest_layout_json", layoutJson);
        putIfColumn(columns, insertValues, "latest_snapshot_json", snapshotJson);
        putIfColumn(columns, insertValues, "created_by", currentOperatorId());
        putIfColumn(columns, insertValues, "updated_by", currentOperatorId());
        putIfColumn(columns, insertValues, "deleted", 0);

        long insertedId = insertTemplate(insertValues);
        String versionTemplateId = hasColumn(columns, "template_id") ? newTemplateId : String.valueOf(insertedId);
        insertVersion(versionTemplateId, 1, "save", sourceName + "-COPY", "draft", snapshotJson, definitionJson, layoutJson, "duplicate template");
        return Result.success(buildTemplateResponse(queryTemplateRow(String.valueOf(insertedId), false)));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> delete(String templateId) {
        ensureWorkflowSchemaPrepared();
        Set<String> columns = getWorkflowTemplateColumns();
        Map<String, Object> row = queryTemplateRow(templateId, true);
        if (row == null) {
            return Result.error("Template not found");
        }

        Map<String, Object> updateValues = new LinkedHashMap<>();
        putIfColumn(columns, updateValues, "deleted", 1);
        putIfColumn(columns, updateValues, "updated_by", currentOperatorId());
        int rows = updateTemplateById(toLong(row.get("id")), updateValues);
        return Result.success(rows > 0);
    }

    public Result<List<Map<String, Object>>> versions(String templateId) {
        ensureWorkflowSchemaPrepared();
        String versionTemplateId = resolveVersionTemplateId(templateId);
        if (!StringUtils.hasText(versionTemplateId)) {
            return Result.success(Collections.emptyList());
        }
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
                """, new MapSqlParameterSource("templateId", versionTemplateId));

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

    public Result<Map<String, Object>> versionDetail(String templateId, Integer versionNo) {
        ensureWorkflowSchemaPrepared();
        String versionTemplateId = resolveVersionTemplateId(templateId);
        if (!StringUtils.hasText(versionTemplateId)) {
            return Result.error("Template not found");
        }
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
                """, new MapSqlParameterSource().addValue("templateId", versionTemplateId).addValue("versionNo", versionNo));

        if (rows.isEmpty()) {
            return Result.error("Template version not found");
        }

        Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
        row.put("status", normalizeStatus(stringValue(row.get("status"))));
        Map<String, Object> snapshot = parseJson(stringValue(row.get("snapshotJson")));
        row.put("snapshot", snapshot);
        row.put("payload", snapshot);
        return Result.success(row);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> restoreVersion(
            String templateId,
            Integer versionNo,
            Map<String, Object> body
    ) {
        ensureWorkflowSchemaPrepared();
        Set<String> columns = getWorkflowTemplateColumns();
        String versionTemplateId = resolveVersionTemplateId(templateId);
        if (!StringUtils.hasText(versionTemplateId)) {
            return Result.error("Template not found");
        }
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT version_no, template_name, status, snapshot_json, definition_json, layout_json
                FROM hr_workflow_template_version
                WHERE template_id = :templateId AND version_no = :versionNo
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource().addValue("templateId", versionTemplateId).addValue("versionNo", versionNo));

        if (rows.isEmpty()) {
            return Result.error("Template version not found");
        }

        Map<String, Object> targetVersion = rows.get(0);
        String snapshotJson = stringValue(targetVersion.get("snapshot_json"));
        String definitionJson = stringValue(targetVersion.get("definition_json"));
        String layoutJson = stringValue(targetVersion.get("layout_json"));
        if (!StringUtils.hasText(snapshotJson)) {
            return Result.error("Template snapshot is empty");
        }

        Map<String, Object> templateRow = queryTemplateRow(templateId, true);
        if (templateRow == null) {
            return Result.error("Template not found");
        }

        int nextVersion = intValue(templateRow.get("current_version"), intValue(templateRow.get("version_no"), 1)) + 1;
        String restoredStatus = normalizeStatus(nonEmptyOrDefault(stringValue(targetVersion.get("status")), stringValue(templateRow.get("status"))));
        Map<String, Object> restoredSnapshot = parseJson(snapshotJson);
        String restoredName = sanitizeReadableText(
                stringValue(targetVersion.get("template_name")),
                firstReadableText(stringValue(restoredSnapshot.get("templateName")), stringValue(templateRow.get("template_name"))),
                "Untitled Workflow Template"
        );
        String restoredCode = normalizeTemplateCode(nonEmptyOrDefault(
                stringValue(restoredSnapshot.get("templateCode")),
                stringValue(templateRow.get("template_code"))
        ));
        String restoredBusinessType = resolveBusinessTypeForWrite(restoredSnapshot, templateRow, versionTemplateId, restoredCode);
        String restoredCategory = resolveCategoryForWrite(restoredSnapshot, templateRow, restoredBusinessType, "general");
        restoredSnapshot.put("templateName", restoredName);
        restoredSnapshot.put("templateCode", restoredCode);
        restoredSnapshot.put("category", restoredCategory);
        restoredSnapshot.put("businessType", restoredBusinessType);
        restoredSnapshot.put("status", restoredStatus);
        restoredSnapshot.put("version", nextVersion);
        snapshotJson = toJson(restoredSnapshot);
        definitionJson = toJson(restoredSnapshot.get("definition"));
        layoutJson = toJson(restoredSnapshot.get("layout"));

        Map<String, Object> updateValues = new LinkedHashMap<>();
        putIfColumn(columns, updateValues, "status", restoredStatus);
        putIfColumn(columns, updateValues, "template_name", restoredName);
        putIfColumn(columns, updateValues, "template_code", restoredCode);
        putIfColumn(columns, updateValues, "category", restoredCategory);
        putIfColumn(columns, updateValues, "business_type", restoredBusinessType);
        putIfColumn(columns, updateValues, "current_version", nextVersion);
        putIfColumn(columns, updateValues, "version_no", nextVersion);
        putIfColumn(columns, updateValues, "latest_definition_json", definitionJson);
        putIfColumn(columns, updateValues, "latest_layout_json", layoutJson);
        putIfColumn(columns, updateValues, "latest_snapshot_json", snapshotJson);
        putIfColumn(columns, updateValues, "updated_by", currentOperatorId());

        int updatedRows = updateTemplateById(toLong(templateRow.get("id")), updateValues);

        String remark = body == null ? null : stringValue(body.get("remark"));
        insertVersion(versionTemplateId, nextVersion, "restore", restoredName, restoredStatus, snapshotJson, definitionJson, layoutJson, remark);

        Map<String, Object> snapshot = parseJson(snapshotJson);
        log.info("[workflow-template] restore templateId={}, actionType=restore, versionNo={}, snapshotEmpty={}, nodeCount={}, edgeCount={}, updateSuccess={}",
                templateId, nextVersion, !StringUtils.hasText(snapshotJson), countNodes(snapshot), countEdges(snapshot), updatedRows > 0);

        return Result.success(buildTemplateResponse(queryTemplateRow(String.valueOf(templateRow.get("id")), false)));
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

    private void ensureWorkflowSchemaPrepared() {
        if (workflowSchemaPrepared) {
            return;
        }
        synchronized (workflowSchemaLock) {
            if (workflowSchemaPrepared) {
                return;
            }

            Set<String> columns = getWorkflowTemplateColumns();
            List<String> requiredColumns = Arrays.asList(
                    "template_id",
                    "template_code",
                    "category",
                    "current_version",
                    "latest_definition_json",
                    "latest_layout_json",
                    "latest_snapshot_json",
                    "published_version",
                    "published_snapshot_json",
                    "created_by",
                    "updated_by"
            );

            List<String> missingColumns = new ArrayList<>();
            for (String requiredColumn : requiredColumns) {
                if (!hasColumn(columns, requiredColumn)) {
                    missingColumns.add(requiredColumn);
                }
            }

            if (!missingColumns.isEmpty() || !tableExists("hr_workflow_template_version")) {
                throw new IllegalStateException(
                        "workflow schema is not ready, please execute database/hrms_full_init.sql or database/04_migration_workflow.sql, missingColumns="
                                + missingColumns
                );
            }

            workflowSchemaPrepared = true;
            log.info("[workflow-template] schema check passed");
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbc.queryForObject("""
                        SELECT COUNT(1)
                        FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = :tableName
                        """,
                new MapSqlParameterSource("tableName", tableName),
                Integer.class
        );
        return count != null && count > 0;
    }


    private Map<String, Object> queryTemplateRow(String templateId, boolean forUpdate) {
        Set<String> columns = getWorkflowTemplateColumns();
        Long numericId = isNumeric(templateId) ? Long.valueOf(templateId) : null;

        boolean hasTemplateId = hasColumn(columns, "template_id");
        boolean hasTemplateCode = hasColumn(columns, "template_code");
        boolean hasCategory = hasColumn(columns, "category");
        boolean hasCurrentVersion = hasColumn(columns, "current_version");
        boolean hasLatestDefinition = hasColumn(columns, "latest_definition_json");
        boolean hasLatestLayout = hasColumn(columns, "latest_layout_json");
        boolean hasLatestSnapshot = hasColumn(columns, "latest_snapshot_json");
        boolean hasPublishedVersion = hasColumn(columns, "published_version");
        boolean hasPublishedSnapshot = hasColumn(columns, "published_snapshot_json");

        List<String> selectColumns = new ArrayList<>();
        selectColumns.add("id");
        selectColumns.add(hasTemplateId ? "template_id" : "CAST(id AS CHAR) AS template_id");
        selectColumns.add("template_name");
        selectColumns.add(hasTemplateCode
                ? "template_code"
                : (hasTemplateId ? "template_id AS template_code" : "CAST(id AS CHAR) AS template_code"));
        selectColumns.add(hasCategory ? "category" : "business_type AS category");
        selectColumns.add("business_type");
        selectColumns.add("status");
        selectColumns.add("version_no");
        selectColumns.add(hasCurrentVersion ? "current_version" : "version_no AS current_version");
        selectColumns.add("remark");
        if (hasLatestDefinition) {
            selectColumns.add("latest_definition_json");
        }
        if (hasLatestLayout) {
            selectColumns.add("latest_layout_json");
        }
        if (hasLatestSnapshot) {
            selectColumns.add("latest_snapshot_json");
        }
        if (hasPublishedVersion) {
            selectColumns.add("published_version");
        }
        if (hasPublishedSnapshot) {
            selectColumns.add("published_snapshot_json");
        }
        selectColumns.add("DATE_FORMAT(created_time, '%Y-%m-%d %H:%i:%s') AS created_time");
        selectColumns.add("DATE_FORMAT(updated_time, '%Y-%m-%d %H:%i:%s') AS updated_time");

        MapSqlParameterSource params = new MapSqlParameterSource("templateId", templateId);
        List<String> predicates = new ArrayList<>();
        if (hasTemplateId) {
            predicates.add("LOWER(template_id) = LOWER(:templateId)");
        }
        if (hasTemplateCode) {
            predicates.add("LOWER(template_code) = LOWER(:templateId)");
        }
        if (!hasTemplateId && !hasTemplateCode) {
            predicates.add("LOWER(business_type) = LOWER(:templateId)");
        }
        if (numericId != null) {
            predicates.add("id = :numericId");
            params.addValue("numericId", numericId);
        }
        if (predicates.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ")
                .append(String.join(", ", selectColumns))
                .append(" FROM hr_workflow_template WHERE deleted = 0 AND (")
                .append(String.join(" OR ", predicates))
                .append(") ORDER BY id DESC LIMIT 1");
        if (forUpdate) {
            sql.append(" FOR UPDATE");
        }

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Set<String> getWorkflowTemplateColumns() {
        try {
            List<String> columnList = jdbc.queryForList("""
                    SELECT LOWER(COLUMN_NAME)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'hr_workflow_template'
                    """, new MapSqlParameterSource(), String.class);

            if (columnList == null || columnList.isEmpty()) {
                return new HashSet<>(Arrays.asList(
                        "id", "template_name", "business_type", "status",
                        "version_no", "remark", "created_time", "updated_time", "deleted"
                ));
            }
            return new HashSet<>(columnList);
        } catch (DataAccessException ex) {
            log.warn("[workflow-template] failed to read table columns, fallback to baseline schema: {}", ex.getMessage());
            return new HashSet<>(Arrays.asList(
                    "id", "template_name", "business_type", "status",
                    "version_no", "remark", "created_time", "updated_time", "deleted"
            ));
        }
    }

    private boolean hasColumn(Set<String> columns, String columnName) {
        return columns.contains(columnName.toLowerCase(Locale.ROOT));
    }

    private void putIfColumn(Set<String> columns, Map<String, Object> target, String column, Object value) {
        if (!hasColumn(columns, column)) {
            return;
        }
        target.put(column, value);
    }

    private int updateTemplateById(Long id, Map<String, Object> values) {
        if (id == null || values.isEmpty()) {
            return 0;
        }
        List<String> updates = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            updates.add(entry.getKey() + " = :" + entry.getKey());
            params.addValue(entry.getKey(), entry.getValue());
        }
        updates.add("updated_time = NOW()");

        String sql = "UPDATE hr_workflow_template SET " + String.join(", ", updates) + " WHERE id = :id AND deleted = 0";
        return jdbc.update(sql, params);
    }

    private long insertTemplate(Map<String, Object> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("template values cannot be empty");
        }
        List<String> columns = new ArrayList<>(values.keySet());
        List<String> placeholders = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();
        for (String column : columns) {
            placeholders.add(":" + column);
            params.addValue(column, values.get(column));
        }

        String sql = "INSERT INTO hr_workflow_template (" + String.join(", ", columns) + ") VALUES (" + String.join(", ", placeholders) + ")";
        jdbc.update(sql, params);
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return id == null ? 0L : id;
    }

    private String resolveVersionTemplateId(String templateId) {
        Map<String, Object> row = queryTemplateRow(templateId, false);
        if (row == null) {
            return null;
        }
        return nonEmptyOrDefault(stringValue(row.get("template_id")), templateId);
    }

    private Long parseProcessNumericId(String templateId) {
        if (!StringUtils.hasText(templateId)) {
            return null;
        }
        Matcher matcher = PROCESS_KEY_PATTERN.matcher(templateId.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Long.valueOf(matcher.group(2));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String parseProcessBusinessType(String templateId) {
        if (!StringUtils.hasText(templateId)) {
            return null;
        }
        Matcher matcher = PROCESS_KEY_PATTERN.matcher(templateId.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }

    private String normalizeTemplateBusinessKey(String key) {
        if (!StringUtils.hasText(key)) {
            return "tpl_" + System.currentTimeMillis();
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        normalized = normalized.replaceAll("_{2,}", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        if (!StringUtils.hasText(normalized)) {
            return "tpl_" + System.currentTimeMillis();
        }
        return normalized;
    }

    private Map<String, Object> buildTemplateResponse(Map<String, Object> row) {
        if (row == null) {
            return Collections.emptyMap();
        }

        String templateId = nonEmptyOrDefault(stringValue(row.get("template_id")), "tpl_" + row.get("id"));
        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(row.get("template_code")), templateId));
        String status = normalizeStatus(stringValue(row.get("status")));
        Integer version = intValue(row.get("current_version"), intValue(row.get("version_no"), 1));

        Map<String, Object> snapshot = parseJson(stringValue(row.get("latest_snapshot_json")));
        if (snapshot.isEmpty()) {
            snapshot = new LinkedHashMap<>();
        }
        String businessType = resolveBusinessTypeForWrite(snapshot, row, templateId, templateCode);
        String templateName = sanitizeReadableText(
                stringValue(row.get("template_name")),
                firstReadableText(stringValue(snapshot.get("templateName")), resolveReadableValueFromHistory(templateId, "templateName")),
                "Untitled Workflow Template"
        );
        String category = resolveCategoryForWrite(
                snapshot,
                row,
                businessType,
                firstReadableText(resolveReadableValueFromHistory(templateId, "category"), "general")
        );

        Map<String, Object> definition = mapValue(snapshot.get("definition"));
        if (definition.isEmpty()) {
            definition = parseJson(stringValue(row.get("latest_definition_json")));
        }
        if (definition.isEmpty()) {
            definition = new LinkedHashMap<>();
        }
        definition.put("nodes", listValue(definition.get("nodes")));
        definition.put("edges", listValue(definition.get("edges")));
        sanitizeDefinition(definition);

        Map<String, Object> layout = mapValue(snapshot.get("layout"));
        if (layout.isEmpty()) {
            layout = parseJson(stringValue(row.get("latest_layout_json")));
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
        response.put("templateId", templateId);
        response.put("templateName", templateName);
        response.put("templateCode", templateCode);
        response.put("category", category);
        response.put("businessType", businessType);
        response.put("status", status);
        response.put("version", version);
        response.put("currentVersion", version);
        response.put("publishedVersion", intValue(row.get("published_version"), 0));
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

        String templateCode = normalizeTemplateCode(nonEmptyOrDefault(stringValue(source.get("templateCode")), fallback == null ? templateId : stringValue(fallback.get("templateCode"))));
        String status = normalizeStatus(nonEmptyOrDefault(stringValue(source.get("status")), fallback == null ? "draft" : stringValue(fallback.get("status"))));
        Integer version = intValue(source.get("version"), fallback == null ? 1 : intValue(fallback.get("version"), 1));
        String businessType = resolveBusinessTypeForWrite(source, fallback, templateId, templateCode);
        String templateName = sanitizeReadableText(
                stringValue(source.get("templateName")),
                fallback == null ? null : stringValue(fallback.get("templateName")),
                "Untitled Workflow Template"
        );
        String category = resolveCategoryForWrite(source, fallback, businessType, "general");

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
        sanitizeDefinition(definition);

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
        snapshot.put("businessType", businessType);
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
            throw new RuntimeException("failed to serialize workflow snapshot", ex);
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
        Set<String> columns = getWorkflowTemplateColumns();
        if (!hasColumn(columns, "template_id")) {
            return false;
        }
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hr_workflow_template WHERE template_id = :templateId AND deleted = 0",
                new MapSqlParameterSource("templateId", templateId),
                Long.class
        );
        return count != null && count > 0;
    }

    private boolean existsTemplateCode(String templateCode) {
        Set<String> columns = getWorkflowTemplateColumns();
        String codeColumn = hasColumn(columns, "template_code") ? "template_code" : "business_type";
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hr_workflow_template WHERE " + codeColumn + " = :templateCode AND deleted = 0",
                new MapSqlParameterSource("templateCode", templateCode),
                Long.class
        );
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

    private void sanitizeDefinition(Map<String, Object> definition) {
        List<Map<String, Object>> nodes = listValue(definition.get("nodes"));
        for (Map<String, Object> node : nodes) {
            String nodeType = stringValue(node.get("type"));
            String rawName = stringValue(node.get("name"));
            node.put("name", sanitizeNodeName(nodeType, rawName));
        }
        definition.put("nodes", nodes);
    }

    private String sanitizeNodeName(String nodeType, String value) {
        String fallback = defaultNodeName(nodeType);
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String trimmed = value.trim();
        if (isQuestionMarkOnly(trimmed) || containsPrivateUseChars(trimmed) || KNOWN_GARBLED_NODE_NAMES.contains(trimmed)) {
            return fallback;
        }
        return trimmed;
    }

    private String defaultNodeName(String nodeType) {
        if (!StringUtils.hasText(nodeType)) {
            return "审批节点";
        }
        return switch (nodeType) {
            case "starter" -> "发起人";
            case "approval" -> "审批节点";
            case "cc" -> "抄送节点";
            case "condition" -> "条件分支";
            case "condition_branch" -> "条件分支";
            case "condition_join" -> "条件汇合";
            case "parallel_fork" -> "并行分支";
            case "parallel_branch" -> "并行分支";
            case "parallel_join" -> "并行汇合";
            case "end" -> "流程结束";
            default -> "审批节点";
        };
    }

    private boolean containsPrivateUseChars(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '\uE000' && ch <= '\uF8FF') {
                return true;
            }
        }
        return false;
    }

    private String resolveBusinessTypeForWrite(
            Map<String, Object> source,
            Map<String, Object> fallback,
            String templateId,
            String templateCode
    ) {
        String businessType = normalizeBusinessType(stringValue(source.get("businessType")));
        if (!StringUtils.hasText(businessType)) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(source.get("templateCode"))));
        }
        if (!StringUtils.hasText(businessType)) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(source.get("templateId"))));
        }

        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(stringValue(fallback.get("businessType")));
        }
        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(stringValue(fallback.get("business_type")));
        }
        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(fallback.get("templateCode"))));
        }
        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(fallback.get("template_code"))));
        }
        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(fallback.get("templateId"))));
        }
        if (!StringUtils.hasText(businessType) && fallback != null) {
            businessType = normalizeBusinessType(extractBusinessType(stringValue(fallback.get("template_id"))));
        }

        if (!StringUtils.hasText(businessType)) {
            businessType = normalizeBusinessType(extractBusinessType(templateCode));
        }
        if (!StringUtils.hasText(businessType)) {
            businessType = normalizeBusinessType(extractBusinessType(templateId));
        }
        if (!StringUtils.hasText(businessType)) {
            businessType = normalizeBusinessType(parseProcessBusinessType(templateId));
        }

        return StringUtils.hasText(businessType) ? businessType : "GENERAL";
    }

    private String resolveCategoryForWrite(
            Map<String, Object> source,
            Map<String, Object> fallback,
            String businessType,
            String defaultValue
    ) {
        String category = firstReadableText(
                stringValue(source.get("category")),
                fallback == null ? null : stringValue(fallback.get("category")),
                fallback == null ? null : stringValue(fallback.get("category_name"))
        );
        if (!StringUtils.hasText(category)) {
            category = categoryFromBusinessType(businessType);
        }
        return nonEmptyOrDefault(category, defaultValue);
    }

    private String resolveReadableValueFromHistory(String templateId, String field) {
        if (!StringUtils.hasText(templateId) || !tableExists("hr_workflow_template_version")) {
            return null;
        }
        try {
            List<Map<String, Object>> rows = jdbc.queryForList("""
                    SELECT template_name, snapshot_json
                    FROM hr_workflow_template_version
                    WHERE template_id = :templateId
                    ORDER BY version_no DESC, id DESC
                    LIMIT 30
                    """, new MapSqlParameterSource("templateId", templateId));
            for (Map<String, Object> row : rows) {
                if ("templateName".equals(field)) {
                    String directName = firstReadableText(stringValue(row.get("template_name")));
                    if (StringUtils.hasText(directName)) {
                        return directName;
                    }
                }
                Map<String, Object> snapshot = parseJson(stringValue(row.get("snapshot_json")));
                String candidate = switch (field) {
                    case "templateName" -> firstReadableText(stringValue(snapshot.get("templateName")));
                    case "category" -> firstReadableText(stringValue(snapshot.get("category")));
                    default -> null;
                };
                if (StringUtils.hasText(candidate)) {
                    return candidate;
                }
            }
        } catch (Exception ex) {
            log.warn("[workflow-template] failed to resolve readable history field, templateId={}, field={}, message={}",
                    templateId, field, ex.getMessage());
        }
        return null;
    }

    private String validatePayloadIdentity(String pathTemplateId, Map<String, Object> row, Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        String rowTemplateId = stringValue(row.get("template_id"));
        String rowTemplateCode = stringValue(row.get("template_code"));
        List<String> accepted = new ArrayList<>();
        if (StringUtils.hasText(pathTemplateId)) {
            accepted.add(pathTemplateId.trim());
        }
        if (StringUtils.hasText(rowTemplateId)) {
            accepted.add(rowTemplateId.trim());
        }
        if (StringUtils.hasText(rowTemplateCode)) {
            accepted.add(rowTemplateCode.trim());
        }

        String bodyTemplateId = stringValue(payload.get("templateId"));
        if (StringUtils.hasText(bodyTemplateId) && !matchesAnyIdentity(bodyTemplateId, accepted)) {
            return "请求体 templateId 与路径模板不一致，请刷新后重试";
        }

        Map<String, Object> snapshot = mapValue(payload.get("snapshot"));
        String snapshotTemplateId = stringValue(snapshot.get("templateId"));
        if (StringUtils.hasText(snapshotTemplateId) && !matchesAnyIdentity(snapshotTemplateId, accepted)) {
            return "请求体 snapshot.templateId 与路径模板不一致，请刷新后重试";
        }
        return null;
    }

    private boolean matchesAnyIdentity(String candidate, List<String> acceptedIdentities) {
        if (!StringUtils.hasText(candidate) || acceptedIdentities == null || acceptedIdentities.isEmpty()) {
            return false;
        }
        String normalizedCandidate = candidate.trim();
        for (String accepted : acceptedIdentities) {
            if (!StringUtils.hasText(accepted)) {
                continue;
            }
            if (accepted.trim().equalsIgnoreCase(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeReadableText(String candidate, String fallback, String defaultValue) {
        String resolved = firstReadableText(candidate, fallback);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        return defaultValue;
    }

    private String firstReadableText(String... candidates) {
        if (candidates == null || candidates.length == 0) {
            return null;
        }
        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            String trimmed = candidate.trim();
            if (!isQuestionMarkOnly(trimmed)) {
                return trimmed;
            }
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return null;
    }

    private String extractBusinessType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        Matcher processKeyMatcher = PROCESS_KEY_PATTERN.matcher(normalized.toLowerCase(Locale.ROOT));
        if (processKeyMatcher.matches()) {
            return processKeyMatcher.group(1);
        }
        Matcher processCodeMatcher = PROCESS_CODE_PATTERN.matcher(normalized);
        if (processCodeMatcher.matches()) {
            return processCodeMatcher.group(1);
        }
        if (normalized.matches("^[A-Z0-9_]{2,32}$")) {
            return normalized;
        }
        return null;
    }

    private String normalizeBusinessType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replaceAll("[^A-Z0-9_]", "");
        if (!StringUtils.hasText(normalized) || isQuestionMarkOnly(normalized)) {
            return null;
        }
        return normalized;
    }

    private String categoryFromBusinessType(String businessType) {
        String normalized = normalizeBusinessType(businessType);
        if (!StringUtils.hasText(normalized)) {
            return "general";
        }
        return switch (normalized) {
            case "LEAVE" -> "请假";
            case "PATCH" -> "补卡";
            case "OVERTIME" -> "加班";
            case "REIMBURSE" -> "报销";
            case "GENERAL" -> "通用";
            default -> normalized;
        };
    }

    private boolean isQuestionMarkOnly(String value) {
        return StringUtils.hasText(value) && QUESTION_MARK_ONLY_PATTERN.matcher(value.trim()).matches();
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

