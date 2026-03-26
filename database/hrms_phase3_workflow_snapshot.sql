-- ============================================================================
-- HRMS Phase 3 - Workflow Snapshot / Versioning / UTF8MB4
-- Date: 2026-03-26
-- ============================================================================

USE `hrms_db`;

-- 1) 主模板表增强：当前定义、布局、快照、发布快照
ALTER TABLE `hr_workflow_template`
    ADD COLUMN IF NOT EXISTS `template_id` varchar(64) NULL COMMENT '模板业务ID' AFTER `id`,
    ADD COLUMN IF NOT EXISTS `template_code` varchar(100) NULL COMMENT '模板编码' AFTER `template_name`,
    ADD COLUMN IF NOT EXISTS `category` varchar(50) NULL COMMENT '流程分类' AFTER `business_type`,
    ADD COLUMN IF NOT EXISTS `current_version` int DEFAULT 1 COMMENT '当前版本号' AFTER `status`,
    ADD COLUMN IF NOT EXISTS `latest_definition_json` JSON NULL COMMENT '最新流程定义JSON' AFTER `remark`,
    ADD COLUMN IF NOT EXISTS `latest_layout_json` JSON NULL COMMENT '最新布局JSON' AFTER `latest_definition_json`,
    ADD COLUMN IF NOT EXISTS `latest_snapshot_json` JSON NULL COMMENT '最新完整快照JSON' AFTER `latest_layout_json`,
    ADD COLUMN IF NOT EXISTS `published_version` int DEFAULT NULL COMMENT '已发布版本号' AFTER `latest_snapshot_json`,
    ADD COLUMN IF NOT EXISTS `published_snapshot_json` JSON NULL COMMENT '已发布快照JSON' AFTER `published_version`,
    ADD COLUMN IF NOT EXISTS `created_by` bigint NULL COMMENT '创建人ID' AFTER `published_snapshot_json`,
    ADD COLUMN IF NOT EXISTS `updated_by` bigint NULL COMMENT '更新人ID' AFTER `created_by`;

-- 2) 历史版本表
CREATE TABLE IF NOT EXISTS `hr_workflow_template_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` varchar(64) NOT NULL COMMENT '模板业务ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `action_type` varchar(20) NOT NULL COMMENT 'save/publish/restore',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `status` varchar(20) DEFAULT 'draft' COMMENT '模板状态',
  `snapshot_json` JSON NULL COMMENT '完整快照',
  `definition_json` JSON NULL COMMENT '流程定义',
  `layout_json` JSON NULL COMMENT '布局信息',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_tpl_ver_template_version` (`template_id`, `version_no`),
  KEY `idx_wf_tpl_ver_template_id` (`template_id`),
  KEY `idx_wf_tpl_ver_action_type` (`action_type`),
  KEY `idx_wf_tpl_ver_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程模板历史版本表';

-- 3) 数据修复与字段回填
UPDATE `hr_workflow_template`
SET `template_id` = CONCAT('tpl_', `id`)
WHERE (`template_id` IS NULL OR `template_id` = '');

UPDATE `hr_workflow_template`
SET `template_code` = UPPER(REPLACE(`template_id`, '-', '_'))
WHERE (`template_code` IS NULL OR `template_code` = '');

UPDATE `hr_workflow_template`
SET `category` = COALESCE(NULLIF(`category`, ''), NULLIF(`business_type`, ''), '通用')
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

-- 4) 唯一约束
ALTER TABLE `hr_workflow_template`
    ADD UNIQUE KEY `uk_hr_workflow_template_template_id` (`template_id`),
    ADD UNIQUE KEY `uk_hr_workflow_template_template_code` (`template_code`);

-- 5) 字符集统一为 utf8mb4
ALTER TABLE `hr_workflow_template`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node_approver`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `hr_workflow_template_node_cc`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 如有权限，建议同步执行：
-- ALTER DATABASE `hrms_db` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
