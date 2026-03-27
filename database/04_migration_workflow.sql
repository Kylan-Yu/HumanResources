-- ============================================================================
-- 04 - Workflow Migration (Old schema -> target schema)
-- ============================================================================
SET NAMES utf8mb4;
USE `hrms_db`;

-- Add missing columns for hr_workflow_template (idempotent)
SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'template_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN template_id VARCHAR(64) NULL COMMENT ''workflow template business key'' AFTER id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'template_code');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN template_code VARCHAR(100) NULL COMMENT ''workflow template code'' AFTER template_name', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'category');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN category VARCHAR(50) NULL COMMENT ''workflow category'' AFTER business_type', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'current_version');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN current_version INT DEFAULT 1 COMMENT ''current version'' AFTER status', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'latest_definition_json');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN latest_definition_json JSON NULL COMMENT ''latest definition json'' AFTER remark', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'latest_layout_json');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN latest_layout_json JSON NULL COMMENT ''latest layout json'' AFTER latest_definition_json', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'latest_snapshot_json');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN latest_snapshot_json JSON NULL COMMENT ''latest snapshot json'' AFTER latest_layout_json', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'published_version');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN published_version INT DEFAULT NULL COMMENT ''published version'' AFTER latest_snapshot_json', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'published_snapshot_json');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN published_snapshot_json JSON NULL COMMENT ''published snapshot json'' AFTER published_version', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'created_by');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN created_by BIGINT NULL COMMENT ''created by'' AFTER published_snapshot_json', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND COLUMN_NAME = 'updated_by');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE hr_workflow_template ADD COLUMN updated_by BIGINT NULL COMMENT ''updated by'' AFTER created_by', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill and normalize data
UPDATE hr_workflow_template
SET template_id = LOWER(CONCAT(COALESCE(NULLIF(business_type, ''), 'tpl'), '_process_', LPAD(id, 3, '0')))
WHERE (template_id IS NULL OR template_id = '')
  AND deleted = 0;

UPDATE hr_workflow_template
SET template_code = UPPER(REPLACE(COALESCE(NULLIF(template_id, ''), CONCAT('TPL_', id)), '-', '_'))
WHERE (template_code IS NULL OR template_code = '')
  AND deleted = 0;

UPDATE hr_workflow_template
SET category = COALESCE(NULLIF(category, ''), NULLIF(business_type, ''), 'general')
WHERE (category IS NULL OR category = '')
  AND deleted = 0;

UPDATE hr_workflow_template
SET current_version = COALESCE(current_version, version_no, 1)
WHERE current_version IS NULL OR current_version <= 0;

UPDATE hr_workflow_template
SET status = CASE
  WHEN LOWER(status) = 'enabled' THEN 'published'
  WHEN LOWER(status) = 'disabled' THEN 'disabled'
  WHEN LOWER(status) = 'published' THEN 'published'
  WHEN LOWER(status) = 'draft' THEN 'draft'
  ELSE 'draft'
END
WHERE deleted = 0;

-- Optional fallback names for blank legacy records
UPDATE hr_workflow_template
SET template_name = 'Leave Approval Flow V1'
WHERE business_type = 'LEAVE'
  AND (template_name IS NULL OR template_name = '');

UPDATE hr_workflow_template
SET template_name = 'Patch Approval Flow V1'
WHERE business_type = 'PATCH'
  AND (template_name IS NULL OR template_name = '');

UPDATE hr_workflow_template
SET template_name = 'Overtime Approval Flow V1'
WHERE business_type = 'OVERTIME'
  AND (template_name IS NULL OR template_name = '');

-- Unique indexes
SET @idx_exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND INDEX_NAME = 'uk_hr_workflow_template_template_id');
SET @ddl = IF(@idx_exists = 0, 'ALTER TABLE hr_workflow_template ADD UNIQUE KEY uk_hr_workflow_template_template_id (template_id)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_workflow_template' AND INDEX_NAME = 'uk_hr_workflow_template_template_code');
SET @ddl = IF(@idx_exists = 0, 'ALTER TABLE hr_workflow_template ADD UNIQUE KEY uk_hr_workflow_template_template_code (template_code)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Version table
CREATE TABLE IF NOT EXISTS hr_workflow_template_version (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  template_id VARCHAR(64) NOT NULL COMMENT 'workflow template business key',
  version_no INT NOT NULL COMMENT 'version number',
  action_type VARCHAR(20) NOT NULL COMMENT 'save/publish/restore',
  template_name VARCHAR(100) NOT NULL COMMENT 'template name',
  status VARCHAR(20) DEFAULT 'draft' COMMENT 'template status',
  snapshot_json JSON NULL COMMENT 'full snapshot',
  definition_json JSON NULL COMMENT 'definition data',
  layout_json JSON NULL COMMENT 'layout data',
  operator_id BIGINT DEFAULT NULL COMMENT 'operator id',
  operator_name VARCHAR(100) DEFAULT NULL COMMENT 'operator name',
  remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_wf_tpl_ver_template_version (template_id, version_no),
  KEY idx_wf_tpl_ver_template_id (template_id),
  KEY idx_wf_tpl_ver_action_type (action_type),
  KEY idx_wf_tpl_ver_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Charset normalize
ALTER TABLE hr_workflow_template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE hr_workflow_template_node CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE hr_workflow_template_node_approver CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE hr_workflow_template_node_cc CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE hr_workflow_template_version CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
