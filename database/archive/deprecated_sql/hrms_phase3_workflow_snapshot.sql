-- ============================================================================
-- HRMS Phase 3 - Workflow Snapshot / Versioning / UTF8MB4
-- Date: 2026-03-26
-- ============================================================================

USE `hrms_db`;

-- 1) 涓绘ā鏉胯〃澧炲己锛氬綋鍓嶅畾涔夈€佸竷灞€銆佸揩鐓с€佸彂甯冨揩鐓?ALTER TABLE `hr_workflow_template`
    ADD COLUMN IF NOT EXISTS `template_id` varchar(64) NULL COMMENT '妯℃澘涓氬姟ID' AFTER `id`,
    ADD COLUMN IF NOT EXISTS `template_code` varchar(100) NULL COMMENT '妯℃澘缂栫爜' AFTER `template_name`,
    ADD COLUMN IF NOT EXISTS `category` varchar(50) NULL COMMENT '娴佺▼鍒嗙被' AFTER `business_type`,
    ADD COLUMN IF NOT EXISTS `current_version` int DEFAULT 1 COMMENT '褰撳墠鐗堟湰鍙? AFTER `status`,
    ADD COLUMN IF NOT EXISTS `latest_definition_json` JSON NULL COMMENT '鏈€鏂版祦绋嬪畾涔塉SON' AFTER `remark`,
    ADD COLUMN IF NOT EXISTS `latest_layout_json` JSON NULL COMMENT '鏈€鏂板竷灞€JSON' AFTER `latest_definition_json`,
    ADD COLUMN IF NOT EXISTS `latest_snapshot_json` JSON NULL COMMENT '鏈€鏂板畬鏁村揩鐓SON' AFTER `latest_layout_json`,
    ADD COLUMN IF NOT EXISTS `published_version` int DEFAULT NULL COMMENT '宸插彂甯冪増鏈彿' AFTER `latest_snapshot_json`,
    ADD COLUMN IF NOT EXISTS `published_snapshot_json` JSON NULL COMMENT '宸插彂甯冨揩鐓SON' AFTER `published_version`,
    ADD COLUMN IF NOT EXISTS `created_by` bigint NULL COMMENT '鍒涘缓浜篒D' AFTER `published_snapshot_json`,
    ADD COLUMN IF NOT EXISTS `updated_by` bigint NULL COMMENT '鏇存柊浜篒D' AFTER `created_by`;

-- 2) 鍘嗗彶鐗堟湰琛?CREATE TABLE IF NOT EXISTS `hr_workflow_template_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `template_id` varchar(64) NOT NULL COMMENT '妯℃澘涓氬姟ID',
  `version_no` int NOT NULL COMMENT '鐗堟湰鍙?,
  `action_type` varchar(20) NOT NULL COMMENT 'save/publish/restore',
  `template_name` varchar(100) NOT NULL COMMENT '妯℃澘鍚嶇О',
  `status` varchar(20) DEFAULT 'draft' COMMENT '妯℃澘鐘舵€?,
  `snapshot_json` JSON NULL COMMENT '瀹屾暣蹇収',
  `definition_json` JSON NULL COMMENT '娴佺▼瀹氫箟',
  `layout_json` JSON NULL COMMENT '甯冨眬淇℃伅',
  `operator_id` bigint DEFAULT NULL COMMENT '鎿嶄綔浜篒D',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '鎿嶄綔浜?,
  `remark` varchar(500) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_tpl_ver_template_version` (`template_id`, `version_no`),
  KEY `idx_wf_tpl_ver_template_id` (`template_id`),
  KEY `idx_wf_tpl_ver_action_type` (`action_type`),
  KEY `idx_wf_tpl_ver_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='娴佺▼妯℃澘鍘嗗彶鐗堟湰琛?;

-- 3) 鏁版嵁淇涓庡瓧娈靛洖濉?UPDATE `hr_workflow_template`
SET `template_id` = CONCAT('tpl_', `id`)
WHERE (`template_id` IS NULL OR `template_id` = '');

UPDATE `hr_workflow_template`
SET `template_code` = UPPER(REPLACE(`template_id`, '-', '_'))
WHERE (`template_code` IS NULL OR `template_code` = '');

UPDATE `hr_workflow_template`
SET `category` = COALESCE(NULLIF(`category`, ''), NULLIF(`business_type`, ''), '閫氱敤')
WHERE (`category` IS NULL OR `category` = '');

UPDATE `hr_workflow_template`
SET `current_version` = COALESCE(`current_version`, `version_no`, 1)
WHERE `current_version` IS NULL OR `current_version` <= 0;

UPDATE `hr_workflow_template`
SET `status` = CASE
    WHEN LOWER(`status`) = 'enabled' THEN 'published'
    WHEN LOWER(`status`) = 'disabled' THEN 'disabled'
    WHEN LOWER(`status`) = 'published' THEN 'published'
    WHEN LOWER(`status`) = 'draft' THEN 'draft'
    ELSE 'draft'
END;

-- 4) 鍞竴绾︽潫
ALTER TABLE `hr_workflow_template`
    ADD UNIQUE KEY `uk_hr_workflow_template_template_id` (`template_id`),
    ADD UNIQUE KEY `uk_hr_workflow_template_template_code` (`template_code`);

-- 5) 瀛楃闆嗙粺涓€涓?utf8mb4
ALTER TABLE `hr_workflow_template`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node_approver`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node_cc`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 濡傛湁鏉冮檺锛屽缓璁悓姝ユ墽琛岋細
-- ALTER DATABASE `hrms_db` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
