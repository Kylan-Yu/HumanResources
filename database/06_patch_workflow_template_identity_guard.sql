-- ============================================================================
-- 06 - Workflow template identity guard
-- ============================================================================
SET NAMES utf8mb4;
USE `hrms_db`;

-- 1) Check duplicates (must be empty)
SELECT template_id, COUNT(*) AS cnt
FROM hr_workflow_template
WHERE deleted = 0
GROUP BY template_id
HAVING COUNT(*) > 1;

SELECT template_code, COUNT(*) AS cnt
FROM hr_workflow_template
WHERE deleted = 0
GROUP BY template_code
HAVING COUNT(*) > 1;

-- 2) Optional cleanup for duplicates (MySQL 8 window function)
-- Keep newest row (largest id), mark older ones deleted=1.
-- Execute only when duplicate check above returns records.
-- START TRANSACTION;
-- UPDATE hr_workflow_template t
-- JOIN (
--   SELECT id
--   FROM (
--     SELECT id,
--            ROW_NUMBER() OVER (PARTITION BY template_id ORDER BY id DESC) AS rn
--     FROM hr_workflow_template
--     WHERE deleted = 0
--   ) x
--   WHERE x.rn > 1
-- ) d ON d.id = t.id
-- SET t.deleted = 1,
--     t.updated_time = NOW();
--
-- UPDATE hr_workflow_template t
-- JOIN (
--   SELECT id
--   FROM (
--     SELECT id,
--            ROW_NUMBER() OVER (PARTITION BY template_code ORDER BY id DESC) AS rn
--     FROM hr_workflow_template
--     WHERE deleted = 0
--   ) x
--   WHERE x.rn > 1
-- ) d ON d.id = t.id
-- SET t.deleted = 1,
--     t.updated_time = NOW();
-- COMMIT;

-- 3) Ensure unique indexes exist
SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'hr_workflow_template'
    AND INDEX_NAME = 'uk_hr_workflow_template_template_id'
);
SET @ddl = IF(
  @idx_exists = 0,
  'ALTER TABLE hr_workflow_template ADD UNIQUE KEY uk_hr_workflow_template_template_id (template_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'hr_workflow_template'
    AND INDEX_NAME = 'uk_hr_workflow_template_template_code'
);
SET @ddl = IF(
  @idx_exists = 0,
  'ALTER TABLE hr_workflow_template ADD UNIQUE KEY uk_hr_workflow_template_template_code (template_code)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) Verify indexes
SELECT INDEX_NAME, NON_UNIQUE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns_in_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'hr_workflow_template'
GROUP BY INDEX_NAME, NON_UNIQUE
ORDER BY INDEX_NAME;
