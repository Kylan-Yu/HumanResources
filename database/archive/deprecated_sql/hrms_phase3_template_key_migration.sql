-- ============================================================================
-- Workflow template key migration (template_id)
-- Purpose:
--   1) Add template_id business key for /workflow/templates/{templateKey}
--   2) Backfill historical records
--   3) Add unique index
-- Compatible with MySQL 8+
-- ============================================================================

USE `hrms_db`;

-- 1) Add template_id column if missing
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'hr_workflow_template'
      AND COLUMN_NAME = 'template_id'
);

SET @ddl_add_col = IF(
    @col_exists = 0,
    'ALTER TABLE hr_workflow_template ADD COLUMN template_id VARCHAR(64) NULL COMMENT ''workflow template business key'' AFTER id',
    'SELECT ''template_id already exists'' AS msg'
);
PREPARE stmt_add_col FROM @ddl_add_col;
EXECUTE stmt_add_col;
DEALLOCATE PREPARE stmt_add_col;

-- 2) Backfill template_id for historical rows
-- Preferred format: <business_type>_process_<id(3)>
-- Example: LEAVE + id=1 => leave_process_001
UPDATE hr_workflow_template
SET template_id = LOWER(
    CONCAT(
        COALESCE(NULLIF(business_type, ''), 'tpl'),
        '_process_',
        LPAD(id, 3, '0')
    )
)
WHERE (template_id IS NULL OR template_id = '')
  AND deleted = 0;

-- Fallback to tpl_<id> if business_type is still null/empty after normalization
UPDATE hr_workflow_template
SET template_id = CONCAT('tpl_', id)
WHERE (template_id IS NULL OR template_id = '')
  AND deleted = 0;

-- 3) Add unique index if missing
SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'hr_workflow_template'
      AND INDEX_NAME = 'uk_hr_workflow_template_template_id'
);

SET @ddl_add_idx = IF(
    @idx_exists = 0,
    'ALTER TABLE hr_workflow_template ADD UNIQUE KEY uk_hr_workflow_template_template_id (template_id)',
    'SELECT ''uk_hr_workflow_template_template_id already exists'' AS msg'
);
PREPARE stmt_add_idx FROM @ddl_add_idx;
EXECUTE stmt_add_idx;
DEALLOCATE PREPARE stmt_add_idx;

-- Optional verification
-- SELECT id, template_id, template_name, business_type FROM hr_workflow_template ORDER BY id;
