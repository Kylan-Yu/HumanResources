-- ============================================================================
-- HRMS Phase 1 - Employee Self-Service & Team Management
-- Date: 2026-03-25
-- ============================================================================

USE `hrms_db`;

CREATE TABLE IF NOT EXISTS `hr_patch_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_no` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `employee_id` bigint DEFAULT NULL,
  `attendance_date` date NOT NULL,
  `patch_time` datetime NOT NULL,
  `patch_type` varchar(20) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `status` varchar(30) DEFAULT 'SUBMITTED',
  `current_instance_id` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_patch_apply_no` (`apply_no`),
  KEY `idx_hr_patch_apply_user` (`user_id`),
  KEY `idx_hr_patch_apply_date` (`attendance_date`),
  KEY `idx_hr_patch_apply_status` (`status`),
  KEY `idx_hr_patch_apply_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `hr_overtime_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_no` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `employee_id` bigint DEFAULT NULL,
  `overtime_date` date NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `hours` decimal(6,2) DEFAULT 0.00,
  `reason` varchar(500) DEFAULT NULL,
  `status` varchar(30) DEFAULT 'SUBMITTED',
  `current_instance_id` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_overtime_apply_no` (`apply_no`),
  KEY `idx_hr_overtime_apply_user` (`user_id`),
  KEY `idx_hr_overtime_apply_date` (`overtime_date`),
  KEY `idx_hr_overtime_apply_status` (`status`),
  KEY `idx_hr_overtime_apply_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `hr_workflow_template` (`template_name`, `business_type`, `status`, `version_no`, `remark`, `created_time`, `updated_time`, `deleted`)
SELECT '补卡审批流程V1', 'PATCH', 'ENABLED', 1, '默认补卡流程模板', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1
    FROM `hr_workflow_template`
    WHERE `business_type` = 'PATCH'
      AND `deleted` = 0
);

INSERT INTO `hr_workflow_template_node`
(`template_id`, `node_order`, `node_name`, `approver_type`, `approver_role_code`, `approver_user_id`, `condition_expression`, `required_flag`, `created_time`, `updated_time`, `deleted`)
SELECT x.id, 1, '直属主管审批', 'DIRECT_LEADER', NULL, NULL, NULL, 1, NOW(), NOW(), 0
FROM (
    SELECT id
    FROM `hr_workflow_template`
    WHERE `business_type` = 'PATCH'
      AND `deleted` = 0
    ORDER BY `version_no` DESC, id DESC
    LIMIT 1
) x
WHERE NOT EXISTS (
    SELECT 1
    FROM `hr_workflow_template_node` n
    WHERE n.`template_id` = x.id
      AND n.`deleted` = 0
);

INSERT INTO `hr_workflow_template` (`template_name`, `business_type`, `status`, `version_no`, `remark`, `created_time`, `updated_time`, `deleted`)
SELECT '加班审批流程V1', 'OVERTIME', 'ENABLED', 1, '默认加班流程模板', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1
    FROM `hr_workflow_template`
    WHERE `business_type` = 'OVERTIME'
      AND `deleted` = 0
);

INSERT INTO `hr_workflow_template_node`
(`template_id`, `node_order`, `node_name`, `approver_type`, `approver_role_code`, `approver_user_id`, `condition_expression`, `required_flag`, `created_time`, `updated_time`, `deleted`)
SELECT x.id, 1, '直属主管审批', 'DIRECT_LEADER', NULL, NULL, NULL, 1, NOW(), NOW(), 0
FROM (
    SELECT id
    FROM `hr_workflow_template`
    WHERE `business_type` = 'OVERTIME'
      AND `deleted` = 0
    ORDER BY `version_no` DESC, id DESC
    LIMIT 1
) x
WHERE NOT EXISTS (
    SELECT 1
    FROM `hr_workflow_template_node` n
    WHERE n.`template_id` = x.id
      AND n.`deleted` = 0
);
