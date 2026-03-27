-- ============================================================================
-- 05 - Workflow text repair patch (for historical '?' dirty data)
-- ============================================================================
SET NAMES utf8mb4;
USE `hrms_db`;

-- This patch restores leave_process_001 from the latest readable history snapshot.
-- It also repairs business_type/template_code/category and records a restore version.

START TRANSACTION;

SET @template_id := CAST('leave_process_001' AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci;

SET @restore_source_version := (
    SELECT v.version_no
    FROM hr_workflow_template_version v
    WHERE v.template_id COLLATE utf8mb4_0900_ai_ci = @template_id
      AND v.template_name IS NOT NULL
      AND v.template_name <> ''
      AND v.template_name NOT REGEXP '^[?？]+$'
      AND (
          JSON_UNQUOTE(JSON_EXTRACT(v.snapshot_json, '$.templateName')) IS NULL
          OR JSON_UNQUOTE(JSON_EXTRACT(v.snapshot_json, '$.templateName')) NOT REGEXP '^[?？]+$'
      )
    ORDER BY v.version_no DESC, v.id DESC
    LIMIT 1
);

SET @next_version := (
    SELECT COALESCE(current_version, version_no, 1) + 1
    FROM hr_workflow_template
    WHERE template_id = @template_id
      AND deleted = 0
    LIMIT 1
);

UPDATE hr_workflow_template t
JOIN hr_workflow_template_version v
  ON v.template_id COLLATE utf8mb4_0900_ai_ci = t.template_id
 AND v.version_no = @restore_source_version
SET t.template_name = COALESCE(NULLIF(v.template_name, ''), t.template_name),
    t.template_code = UPPER(REPLACE(
        COALESCE(
            NULLIF(JSON_UNQUOTE(JSON_EXTRACT(v.snapshot_json, '$.templateCode')), ''),
            NULLIF(t.template_code, ''),
            t.template_id
        ),
        '-',
        '_'
    )),
    t.category = COALESCE(
        NULLIF(JSON_UNQUOTE(JSON_EXTRACT(v.snapshot_json, '$.category')), ''),
        NULLIF(t.category, ''),
        '通用'
    ),
    t.business_type = UPPER(
        COALESCE(
            NULLIF(SUBSTRING_INDEX(t.template_id, '_process_', 1), ''),
            NULLIF(t.business_type, ''),
            'GENERAL'
        )
    ),
    t.latest_snapshot_json = v.snapshot_json,
    t.latest_definition_json = v.definition_json,
    t.latest_layout_json = v.layout_json,
    t.current_version = @next_version,
    t.version_no = @next_version,
    t.updated_time = NOW()
WHERE t.template_id = @template_id
  AND t.deleted = 0
  AND @restore_source_version IS NOT NULL;

INSERT INTO hr_workflow_template_version
(
    template_id,
    version_no,
    action_type,
    template_name,
    status,
    snapshot_json,
    definition_json,
    layout_json,
    operator_id,
    operator_name,
    remark,
    created_at
)
SELECT
    t.template_id,
    @next_version,
    'restore',
    t.template_name,
    t.status,
    t.latest_snapshot_json,
    t.latest_definition_json,
    t.latest_layout_json,
    NULL,
    'system_patch',
    CONCAT('repair from history version ', @restore_source_version),
    NOW()
FROM hr_workflow_template t
WHERE t.template_id = @template_id
  AND t.deleted = 0
  AND @restore_source_version IS NOT NULL;

COMMIT;

SELECT
    template_id,
    template_name,
    category,
    business_type,
    (template_name REGEXP '^[?？]+$') AS name_is_question_marks,
    (category REGEXP '^[?？]+$') AS category_is_question_marks,
    (business_type REGEXP '^[?？]+$') AS business_type_is_question_marks,
    (CAST(latest_snapshot_json AS CHAR) LIKE '%?%') AS snapshot_contains_question_mark
FROM hr_workflow_template
WHERE template_id = @template_id;
