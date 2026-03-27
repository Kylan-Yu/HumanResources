-- ============================================================================
-- HRMS Phase 3 - Workflow Snapshot / Versioning Upgrade
-- Date: 2026-03-27
-- ============================================================================

USE `hrms_db`;

DROP PROCEDURE IF EXISTS `upgrade_workflow_template_phase3`;
DELIMITER $$
CREATE PROCEDURE `upgrade_workflow_template_phase3`()
BEGIN
    DECLARE v_template_id_type VARCHAR(32);
    DECLARE v_template_code_type VARCHAR(32);
    DECLARE v_category_type VARCHAR(32);

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'template_id'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `template_id` VARCHAR(64) DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'template_code'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `template_code` VARCHAR(100) DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'category'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `category` VARCHAR(50) DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'current_version'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `current_version` INT DEFAULT 1;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'latest_definition_json'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `latest_definition_json` LONGTEXT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'latest_layout_json'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `latest_layout_json` LONGTEXT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'latest_snapshot_json'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `latest_snapshot_json` LONGTEXT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'published_version'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `published_version` INT DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'published_snapshot_json'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `published_snapshot_json` LONGTEXT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'created_by'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `created_by` BIGINT DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE table_schema = DATABASE()
          AND table_name = 'hr_workflow_template'
          AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE `hr_workflow_template` ADD COLUMN `updated_by` BIGINT DEFAULT NULL;
    END IF;

    SELECT LOWER(data_type) INTO v_template_id_type
    FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE()
      AND table_name = 'hr_workflow_template'
      AND column_name = 'template_id'
    LIMIT 1;

    IF v_template_id_type IS NOT NULL
       AND v_template_id_type NOT IN ('varchar', 'char', 'text', 'tinytext', 'mediumtext', 'longtext') THEN
        ALTER TABLE `hr_workflow_template` MODIFY COLUMN `template_id` VARCHAR(64) DEFAULT NULL;
    END IF;

    SELECT LOWER(data_type) INTO v_template_code_type
    FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE()
      AND table_name = 'hr_workflow_template'
      AND column_name = 'template_code'
    LIMIT 1;

    IF v_template_code_type IS NOT NULL
       AND v_template_code_type NOT IN ('varchar', 'char', 'text', 'tinytext', 'mediumtext', 'longtext') THEN
        ALTER TABLE `hr_workflow_template` MODIFY COLUMN `template_code` VARCHAR(100) DEFAULT NULL;
    END IF;

    SELECT LOWER(data_type) INTO v_category_type
    FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE()
      AND table_name = 'hr_workflow_template'
      AND column_name = 'category'
    LIMIT 1;

    IF v_category_type IS NOT NULL
       AND v_category_type NOT IN ('varchar', 'char', 'text', 'tinytext', 'mediumtext', 'longtext') THEN
        ALTER TABLE `hr_workflow_template` MODIFY COLUMN `category` VARCHAR(50) DEFAULT NULL;
    END IF;
END$$
DELIMITER ;

CALL `upgrade_workflow_template_phase3`();
DROP PROCEDURE IF EXISTS `upgrade_workflow_template_phase3`;

CREATE TABLE IF NOT EXISTS `hr_workflow_template_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `template_id` VARCHAR(64) NOT NULL,
  `version_no` INT NOT NULL,
  `action_type` VARCHAR(20) NOT NULL,
  `template_name` VARCHAR(100) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'draft',
  `snapshot_json` LONGTEXT,
  `definition_json` LONGTEXT,
  `layout_json` LONGTEXT,
  `operator_id` BIGINT DEFAULT NULL,
  `operator_name` VARCHAR(100) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_tpl_ver_template_version` (`template_id`, `version_no`),
  KEY `idx_wf_tpl_ver_template_id` (`template_id`),
  KEY `idx_wf_tpl_ver_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE `hr_workflow_template`
SET `template_id` = CONCAT('tpl_', `id`)
WHERE (`template_id` IS NULL OR CAST(`template_id` AS CHAR) = '')
  AND `deleted` = 0;

UPDATE `hr_workflow_template`
SET `template_code` = UPPER(REPLACE(CAST(`template_id` AS CHAR), '-', '_'))
WHERE (`template_code` IS NULL OR CAST(`template_code` AS CHAR) = '')
  AND `deleted` = 0;

UPDATE `hr_workflow_template`
SET `category` = COALESCE(NULLIF(`category`, ''), NULLIF(`business_type`, ''), '通用')
WHERE (`category` IS NULL OR `category` = '')
  AND `deleted` = 0;

UPDATE `hr_workflow_template`
SET `current_version` = IFNULL(NULLIF(`current_version`, 0), IFNULL(`version_no`, 1))
WHERE (`current_version` IS NULL OR `current_version` = 0)
  AND `deleted` = 0;

UPDATE `hr_workflow_template`
SET `status` = CASE
  WHEN UPPER(IFNULL(`status`, '')) IN ('ENABLED', 'PUBLISHED') THEN 'ENABLED'
  WHEN UPPER(IFNULL(`status`, '')) = 'DISABLED' THEN 'DISABLED'
  ELSE 'DRAFT'
END
WHERE `deleted` = 0;
