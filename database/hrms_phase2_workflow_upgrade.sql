-- ============================================================================
-- HRMS Phase 2 - Workflow Template Upgrade
-- Date: 2026-03-25
-- ============================================================================

USE `hrms_db`;

ALTER TABLE `hr_workflow_template_node`
    ADD COLUMN IF NOT EXISTS `node_type` varchar(20) DEFAULT 'APPROVAL' AFTER `node_name`;

ALTER TABLE `hr_workflow_template_node`
    ADD COLUMN IF NOT EXISTS `approval_mode` varchar(20) DEFAULT 'ANY' AFTER `node_type`;

UPDATE `hr_workflow_template_node`
SET `node_type` = 'APPROVAL'
WHERE `node_type` IS NULL OR `node_type` = '';

UPDATE `hr_workflow_template_node`
SET `approval_mode` = 'ANY'
WHERE `approval_mode` IS NULL OR `approval_mode` = '';

CREATE TABLE IF NOT EXISTS `hr_workflow_template_node_approver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_node_id` bigint NOT NULL,
  `approver_order` int DEFAULT 1,
  `approver_type` varchar(30) NOT NULL,
  `approver_role_code` varchar(50) DEFAULT NULL,
  `approver_user_id` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_wf_node_approver_node` (`template_node_id`),
  KEY `idx_hr_wf_node_approver_type` (`approver_type`),
  KEY `idx_hr_wf_node_approver_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `hr_workflow_template_node_cc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_node_id` bigint NOT NULL,
  `cc_order` int DEFAULT 1,
  `cc_type` varchar(30) NOT NULL,
  `cc_role_code` varchar(50) DEFAULT NULL,
  `cc_user_id` bigint DEFAULT NULL,
  `cc_dept_id` bigint DEFAULT NULL,
  `cc_timing` varchar(30) DEFAULT 'AFTER_APPROVAL',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_wf_node_cc_node` (`template_node_id`),
  KEY `idx_hr_wf_node_cc_type` (`cc_type`),
  KEY `idx_hr_wf_node_cc_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `hr_workflow_template_node_approver`
(`template_node_id`, `approver_order`, `approver_type`, `approver_role_code`, `approver_user_id`, `created_time`, `updated_time`, `deleted`)
SELECT n.id, 1, n.approver_type, n.approver_role_code, n.approver_user_id, NOW(), NOW(), 0
FROM `hr_workflow_template_node` n
WHERE n.deleted = 0
  AND IFNULL(n.node_type, 'APPROVAL') = 'APPROVAL'
  AND n.approver_type IS NOT NULL
  AND n.approver_type <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM `hr_workflow_template_node_approver` a
      WHERE a.template_node_id = n.id
        AND a.deleted = 0
  );
