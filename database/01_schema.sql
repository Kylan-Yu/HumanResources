-- ============================================================================
-- 01 - HRMS Schema (MySQL 8)
-- Notes:
--   1) Schema only, no database-level statements (no DROP/CREATE/USE DATABASE).
--   2) UTF-8 (no BOM).
--   3) All COMMENT clauses removed to avoid encoding/quote corruption issues.
-- ============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `last_login_time` datetime DEFAULT NULL,
  `last_login_ip` varchar(50) DEFAULT NULL,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_status` (`status`),
  KEY `idx_sys_user_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_user_custom_field` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `field_key` varchar(64) NOT NULL,
  `field_name` varchar(100) NOT NULL,
  `field_type` varchar(20) NOT NULL DEFAULT 'TEXT',
  `required_flag` tinyint DEFAULT 0,
  `placeholder` varchar(200) DEFAULT NULL,
  `options_json` json DEFAULT NULL,
  `default_value` varchar(255) DEFAULT NULL,
  `industry_type` varchar(20) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_custom_field_key` (`field_key`),
  KEY `idx_sys_user_custom_field_status` (`status`),
  KEY `idx_sys_user_custom_field_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL,
  `role_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `sort_order` int DEFAULT 0,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`),
  KEY `idx_sys_role_status` (`status`),
  KEY `idx_sys_role_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT 0,
  `menu_name` varchar(50) NOT NULL,
  `menu_type` tinyint NOT NULL,
  `path` varchar(200) DEFAULT NULL,
  `component` varchar(200) DEFAULT NULL,
  `permission` varchar(100) DEFAULT NULL,
  `icon` varchar(100) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `visible` tinyint DEFAULT 1,
  `status` tinyint DEFAULT 1,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_parent_id` (`parent_id`),
  KEY `idx_sys_menu_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_name` varchar(100) NOT NULL,
  `dict_type` varchar(100) NOT NULL,
  `status` tinyint DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type` (`dict_type`),
  KEY `idx_sys_dict_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_sort` int DEFAULT 0,
  `dict_label` varchar(100) NOT NULL,
  `dict_value` varchar(100) NOT NULL,
  `dict_type` varchar(100) NOT NULL,
  `css_class` varchar(100) DEFAULT NULL,
  `list_class` varchar(100) DEFAULT NULL,
  `is_default` varchar(1) DEFAULT 'N',
  `status` tinyint DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_sys_dict_item_dict_type` (`dict_type`),
  KEY `idx_sys_dict_item_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_org` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_code` varchar(50) NOT NULL,
  `org_name` varchar(100) NOT NULL,
  `org_type` varchar(30) DEFAULT 'COMPANY',
  `parent_id` bigint DEFAULT 0,
  `industry_type` varchar(20) DEFAULT 'company',
  `status` tinyint DEFAULT 1,
  `sort_order` int DEFAULT 0,
  `ext_json` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_org_code` (`org_code`),
  KEY `idx_hr_org_parent_id` (`parent_id`),
  KEY `idx_hr_org_status` (`status`),
  KEY `idx_hr_org_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_code` varchar(50) NOT NULL,
  `dept_name` varchar(100) NOT NULL,
  `org_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT 0,
  `dept_type` varchar(30) DEFAULT 'FUNCTIONAL',
  `manager` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `sort_order` int DEFAULT 0,
  `ext_json` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_dept_code` (`dept_code`),
  KEY `idx_hr_dept_org_id` (`org_id`),
  KEY `idx_hr_dept_parent_id` (`parent_id`),
  KEY `idx_hr_dept_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rank_code` varchar(50) NOT NULL,
  `rank_name` varchar(100) NOT NULL,
  `rank_series` varchar(50) DEFAULT NULL,
  `rank_level` int DEFAULT 1,
  `description` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `sort_order` int DEFAULT 0,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_rank_code` (`rank_code`),
  KEY `idx_hr_rank_status` (`status`),
  KEY `idx_hr_rank_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_position` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `position_code` varchar(50) NOT NULL,
  `position_name` varchar(100) NOT NULL,
  `org_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `position_category` varchar(50) DEFAULT NULL,
  `rank_grade` varchar(30) DEFAULT NULL,
  `rank_series` varchar(50) DEFAULT NULL,
  `job_description` text,
  `requirements` text,
  `status` tinyint DEFAULT 1,
  `sort_order` int DEFAULT 0,
  `ext_json` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_position_code` (`position_code`),
  KEY `idx_hr_position_org_id` (`org_id`),
  KEY `idx_hr_position_dept_id` (`dept_id`),
  KEY `idx_hr_position_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_no` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `gender` tinyint DEFAULT 1,
  `birthday` date DEFAULT NULL,
  `id_card_no` varchar(30) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `marital_status` tinyint DEFAULT NULL,
  `nationality` varchar(50) DEFAULT NULL,
  `domicile_address` varchar(255) DEFAULT NULL,
  `current_address` varchar(255) DEFAULT NULL,
  `employee_status` tinyint DEFAULT 1,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_employee_no` (`employee_no`),
  UNIQUE KEY `uk_hr_employee_id_card` (`id_card_no`),
  KEY `idx_hr_employee_mobile` (`mobile`),
  KEY `idx_hr_employee_status` (`employee_status`),
  KEY `idx_hr_employee_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `org_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `position_id` bigint NOT NULL,
  `rank_id` bigint DEFAULT NULL,
  `leader_id` bigint DEFAULT NULL,
  `employee_type` varchar(30) DEFAULT 'formal',
  `employment_type` varchar(30) DEFAULT 'fulltime',
  `entry_date` date DEFAULT NULL,
  `regular_date` date DEFAULT NULL,
  `work_location` varchar(255) DEFAULT NULL,
  `is_main_job` tinyint DEFAULT 1,
  `status` tinyint DEFAULT 1,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_job_emp_id` (`employee_id`),
  KEY `idx_hr_employee_job_dept_id` (`dept_id`),
  KEY `idx_hr_employee_job_position_id` (`position_id`),
  KEY `idx_hr_employee_job_main` (`is_main_job`),
  KEY `idx_hr_employee_job_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_family` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `relationship` varchar(30) NOT NULL,
  `gender` tinyint DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `id_card_no` varchar(30) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `occupation` varchar(100) DEFAULT NULL,
  `work_unit` varchar(255) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_family_emp_id` (`employee_id`),
  KEY `idx_hr_employee_family_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_education` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `school_name` varchar(200) NOT NULL,
  `education_level` varchar(30) NOT NULL,
  `major` varchar(100) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `is_highest` tinyint DEFAULT 0,
  `degree_type` varchar(30) DEFAULT NULL,
  `graduation_certificate` varchar(255) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_education_emp_id` (`employee_id`),
  KEY `idx_hr_employee_education_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_work_experience` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `company_name` varchar(200) NOT NULL,
  `position` varchar(100) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `job_description` text,
  `resign_reason` varchar(500) DEFAULT NULL,
  `witness` varchar(50) DEFAULT NULL,
  `witness_mobile` varchar(20) DEFAULT NULL,
  `work_months` int DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_work_emp_id` (`employee_id`),
  KEY `idx_hr_employee_work_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `attachment_type` varchar(30) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_attachment_emp_id` (`employee_id`),
  KEY `idx_hr_employee_attachment_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_employee_change_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `change_type` varchar(30) NOT NULL,
  `change_date` date NOT NULL,
  `before_value` text,
  `after_value` text,
  `change_reason` varchar(500) DEFAULT NULL,
  `approver_id` bigint DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_change_emp_id` (`employee_id`),
  KEY `idx_hr_employee_change_date` (`change_date`),
  KEY `idx_hr_employee_change_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_recruit_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requirement_no` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `org_id` bigint DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `position_id` bigint DEFAULT NULL,
  `headcount` int DEFAULT 1,
  `urgency_level` varchar(20) DEFAULT 'MEDIUM',
  `requirement_status` varchar(20) DEFAULT 'DRAFT',
  `expected_entry_date` date DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_recruit_requirement_no` (`requirement_no`),
  KEY `idx_hr_recruit_requirement_status` (`requirement_status`),
  KEY `idx_hr_recruit_requirement_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_recruit_position` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requirement_id` bigint NOT NULL,
  `requirement_no` varchar(50) DEFAULT NULL,
  `position_name` varchar(100) NOT NULL,
  `job_description` text,
  `job_requirements` text,
  `salary_min` decimal(10,2) DEFAULT NULL,
  `salary_max` decimal(10,2) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `employment_type` varchar(30) DEFAULT 'fulltime',
  `publish_status` varchar(20) DEFAULT 'UNPUBLISHED',
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_recruit_position_req_id` (`requirement_id`),
  KEY `idx_hr_recruit_position_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_candidate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `candidate_no` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `gender` varchar(10) DEFAULT 'MALE',
  `mobile` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `resume_url` varchar(500) DEFAULT NULL,
  `source_channel` varchar(30) DEFAULT NULL,
  `apply_position_id` bigint DEFAULT NULL,
  `candidate_status` varchar(20) DEFAULT 'NEW',
  `current_company` varchar(200) DEFAULT NULL,
  `current_position` varchar(100) DEFAULT NULL,
  `expected_salary` decimal(10,2) DEFAULT NULL,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_candidate_no` (`candidate_no`),
  KEY `idx_hr_candidate_status` (`candidate_status`),
  KEY `idx_hr_candidate_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_candidate_interview` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `candidate_id` bigint NOT NULL,
  `interview_round` int DEFAULT 1,
  `interviewer_id` bigint DEFAULT NULL,
  `interview_time` datetime DEFAULT NULL,
  `interview_type` varchar(20) DEFAULT 'ONSITE',
  `score` decimal(5,2) DEFAULT NULL,
  `result` varchar(20) DEFAULT NULL,
  `feedback` text,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_candidate_interview_cid` (`candidate_id`),
  KEY `idx_hr_candidate_interview_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_candidate_offer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `candidate_id` bigint NOT NULL,
  `offer_no` varchar(50) NOT NULL,
  `position_id` bigint DEFAULT NULL,
  `salary_amount` decimal(10,2) DEFAULT NULL,
  `entry_date` date DEFAULT NULL,
  `offer_status` varchar(20) DEFAULT 'PENDING',
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_candidate_offer_no` (`offer_no`),
  KEY `idx_hr_candidate_offer_cid` (`candidate_id`),
  KEY `idx_hr_candidate_offer_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_payroll_standard` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `standard_name` varchar(100) NOT NULL,
  `org_id` bigint DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `position_id` bigint DEFAULT NULL,
  `grade_level` varchar(30) DEFAULT NULL,
  `base_salary` decimal(12,2) DEFAULT 0.00,
  `performance_salary` decimal(12,2) DEFAULT 0.00,
  `position_allowance` decimal(12,2) DEFAULT 0.00,
  `meal_allowance` decimal(12,2) DEFAULT 0.00,
  `transport_allowance` decimal(12,2) DEFAULT 0.00,
  `communication_allowance` decimal(12,2) DEFAULT 0.00,
  `housing_allowance` decimal(12,2) DEFAULT 0.00,
  `other_allowance` decimal(12,2) DEFAULT 0.00,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_payroll_standard_org` (`org_id`),
  KEY `idx_hr_payroll_standard_dept` (`dept_id`),
  KEY `idx_hr_payroll_standard_pos` (`position_id`),
  KEY `idx_hr_payroll_standard_status` (`status`),
  KEY `idx_hr_payroll_standard_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_payroll_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payroll_no` varchar(50) NOT NULL,
  `employee_id` bigint NOT NULL,
  `payroll_period` varchar(20) NOT NULL,
  `period_start_date` date DEFAULT NULL,
  `period_end_date` date DEFAULT NULL,
  `pay_date` date DEFAULT NULL,
  `base_salary` decimal(12,2) DEFAULT 0.00,
  `performance_salary` decimal(12,2) DEFAULT 0.00,
  `position_allowance` decimal(12,2) DEFAULT 0.00,
  `meal_allowance` decimal(12,2) DEFAULT 0.00,
  `transport_allowance` decimal(12,2) DEFAULT 0.00,
  `communication_allowance` decimal(12,2) DEFAULT 0.00,
  `housing_allowance` decimal(12,2) DEFAULT 0.00,
  `other_allowance` decimal(12,2) DEFAULT 0.00,
  `gross_salary` decimal(12,2) DEFAULT 0.00,
  `social_personal` decimal(12,2) DEFAULT 0.00,
  `fund_personal` decimal(12,2) DEFAULT 0.00,
  `income_tax` decimal(12,2) DEFAULT 0.00,
  `other_deduction` decimal(12,2) DEFAULT 0.00,
  `total_deduction` decimal(12,2) DEFAULT 0.00,
  `net_salary` decimal(12,2) DEFAULT 0.00,
  `status` varchar(20) DEFAULT 'DRAFT',
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_payroll_record_no` (`payroll_no`),
  KEY `idx_hr_payroll_record_employee` (`employee_id`),
  KEY `idx_hr_payroll_record_period` (`payroll_period`),
  KEY `idx_hr_payroll_record_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_performance_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_name` varchar(100) NOT NULL,
  `plan_year` int NOT NULL,
  `plan_period` varchar(20) NOT NULL,
  `org_id` bigint DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `status` varchar(20) DEFAULT 'DRAFT',
  `description` varchar(500) DEFAULT NULL,
  `industry_type` varchar(20) DEFAULT 'company',
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_performance_plan_year` (`plan_year`),
  KEY `idx_hr_performance_plan_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_performance_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `employee_no` varchar(50) DEFAULT NULL,
  `employee_name` varchar(50) DEFAULT NULL,
  `org_id` bigint DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `position_id` bigint DEFAULT NULL,
  `score` decimal(5,2) DEFAULT 0.00,
  `grade` varchar(20) DEFAULT NULL,
  `result_status` varchar(20) DEFAULT 'PENDING',
  `self_summary` text,
  `manager_comment` text,
  `hr_comment` text,
  `industry_type` varchar(20) DEFAULT 'company',
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_performance_record_plan` (`plan_id`),
  KEY `idx_hr_performance_record_emp` (`employee_id`),
  KEY `idx_hr_performance_record_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_training_course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(50) NOT NULL,
  `course_name` varchar(100) NOT NULL,
  `course_type` varchar(30) DEFAULT 'GENERAL',
  `lecturer` varchar(100) DEFAULT NULL,
  `duration_hours` decimal(6,2) DEFAULT NULL,
  `description` text,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `industry_type` varchar(20) DEFAULT 'company',
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_training_course_code` (`course_code`),
  KEY `idx_hr_training_course_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_training_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `session_name` varchar(100) NOT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `capacity` int DEFAULT NULL,
  `status` varchar(20) DEFAULT 'PLANNED',
  `industry_type` varchar(20) DEFAULT 'company',
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_training_session_course` (`course_id`),
  KEY `idx_hr_training_session_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_training_enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `attendance_status` varchar(20) DEFAULT 'REGISTERED',
  `score` decimal(5,2) DEFAULT NULL,
  `feedback` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_training_enrollment` (`session_id`,`employee_id`),
  KEY `idx_hr_training_enrollment_emp` (`employee_id`),
  KEY `idx_hr_training_enrollment_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_attendance_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_code` varchar(50) NOT NULL,
  `shift_name` varchar(100) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `work_days` varchar(30) DEFAULT '1,2,3,4,5',
  `work_hours` decimal(5,2) DEFAULT 8.00,
  `late_tolerance_minutes` int DEFAULT 5,
  `early_tolerance_minutes` int DEFAULT 5,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `industry_type` varchar(20) DEFAULT 'company',
  `remark` varchar(500) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_attendance_shift_code` (`shift_code`),
  KEY `idx_hr_attendance_shift_status` (`status`),
  KEY `idx_hr_attendance_shift_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_attendance_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_no` varchar(50) NOT NULL,
  `employee_id` bigint NOT NULL,
  `attendance_date` date NOT NULL,
  `shift_id` bigint DEFAULT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `check_out_time` datetime DEFAULT NULL,
  `attendance_status` varchar(30) DEFAULT 'NORMAL',
  `late_minutes` int DEFAULT 0,
  `early_leave_minutes` int DEFAULT 0,
  `overtime_minutes` int DEFAULT 0,
  `work_hours` decimal(6,2) DEFAULT 0.00,
  `source_type` varchar(20) DEFAULT 'MANUAL',
  `location` varchar(255) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_attendance_record_no` (`record_no`),
  KEY `idx_hr_attendance_record_employee` (`employee_id`),
  KEY `idx_hr_attendance_record_date` (`attendance_date`),
  KEY `idx_hr_attendance_record_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_attendance_appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appeal_no` varchar(50) NOT NULL,
  `record_id` bigint DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  `appeal_type` varchar(20) DEFAULT 'BOTH',
  `requested_check_in_time` datetime DEFAULT NULL,
  `requested_check_out_time` datetime DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `approver_id` bigint DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `approve_remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_attendance_appeal_no` (`appeal_no`),
  KEY `idx_hr_attendance_appeal_employee` (`employee_id`),
  KEY `idx_hr_attendance_appeal_status` (`status`),
  KEY `idx_hr_attendance_appeal_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `category` varchar(30) DEFAULT 'COMPANY',
  `top_flag` tinyint DEFAULT 0,
  `status` varchar(20) DEFAULT 'DRAFT',
  `publish_scope` varchar(20) DEFAULT 'ALL',
  `target_dept_ids` varchar(500) DEFAULT NULL,
  `attachment_json` json DEFAULT NULL,
  `published_by` bigint DEFAULT NULL,
  `published_time` datetime DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_notice_status` (`status`),
  KEY `idx_hr_notice_category` (`category`),
  KEY `idx_hr_notice_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_notice_read_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notice_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `read_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_notice_read` (`notice_id`,`user_id`),
  KEY `idx_hr_notice_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_leave_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_no` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `employee_id` bigint DEFAULT NULL,
  `leave_type` varchar(30) DEFAULT 'ANNUAL',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `leave_days` decimal(6,2) DEFAULT 1.00,
  `reason` varchar(500) DEFAULT NULL,
  `status` varchar(30) DEFAULT 'SUBMITTED',
  `current_instance_id` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_leave_apply_no` (`apply_no`),
  KEY `idx_hr_leave_apply_user` (`user_id`),
  KEY `idx_hr_leave_apply_status` (`status`),
  KEY `idx_hr_leave_apply_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_patch_apply` (
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

CREATE TABLE `hr_overtime_apply` (
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

CREATE TABLE `hr_workflow_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_name` varchar(100) NOT NULL,
  `business_type` varchar(30) NOT NULL,
  `status` varchar(20) DEFAULT 'ENABLED',
  `version_no` int DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_workflow_template_business` (`business_type`),
  KEY `idx_hr_workflow_template_status` (`status`),
  KEY `idx_hr_workflow_template_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_workflow_template_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `node_order` int NOT NULL,
  `node_name` varchar(100) NOT NULL,
  `node_type` varchar(20) DEFAULT 'APPROVAL',
  `approval_mode` varchar(20) DEFAULT 'ANY',
  `approver_type` varchar(30) NOT NULL,
  `approver_role_code` varchar(50) DEFAULT NULL,
  `approver_user_id` bigint DEFAULT NULL,
  `condition_expression` varchar(200) DEFAULT NULL,
  `required_flag` tinyint DEFAULT 1,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_workflow_template_node_tid` (`template_id`),
  KEY `idx_hr_workflow_template_node_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_workflow_template_node_approver` (
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

CREATE TABLE `hr_workflow_template_node_cc` (
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

CREATE TABLE `hr_workflow_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `business_type` varchar(30) NOT NULL,
  `business_id` bigint NOT NULL,
  `initiator_id` bigint NOT NULL,
  `status` varchar(30) DEFAULT 'IN_PROGRESS',
  `current_node_order` int DEFAULT NULL,
  `current_node_name` varchar(100) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finished_time` datetime DEFAULT NULL,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_workflow_instance_business` (`business_type`,`business_id`),
  KEY `idx_hr_workflow_instance_status` (`status`),
  KEY `idx_hr_workflow_instance_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_workflow_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL,
  `node_order` int NOT NULL,
  `node_name` varchar(100) NOT NULL,
  `assignee_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT 'WAITING',
  `result` varchar(20) DEFAULT NULL,
  `comment` varchar(500) DEFAULT NULL,
  `action_time` datetime DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_hr_workflow_task_instance` (`instance_id`),
  KEY `idx_hr_workflow_task_assignee` (`assignee_id`),
  KEY `idx_hr_workflow_task_status` (`status`),
  KEY `idx_hr_workflow_task_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_workflow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `node_order` int DEFAULT NULL,
  `node_name` varchar(100) DEFAULT NULL,
  `approver_id` bigint NOT NULL,
  `action` varchar(20) NOT NULL,
  `result` varchar(20) DEFAULT NULL,
  `comment` varchar(500) DEFAULT NULL,
  `action_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_hr_workflow_record_instance` (`instance_id`),
  KEY `idx_hr_workflow_record_approver` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `contract_no` varchar(50) NOT NULL,
  `contract_type` varchar(50) NOT NULL,
  `contract_subject` varchar(200) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `sign_date` date DEFAULT NULL,
  `contract_status` varchar(20) DEFAULT 'DRAFT',
  `renew_count` int DEFAULT 0,
  `industry_type` varchar(20) DEFAULT 'company',
  `ext_json` json DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_contract_no` (`contract_no`),
  KEY `idx_hr_contract_emp` (`employee_id`),
  KEY `idx_hr_contract_status` (`contract_status`),
  KEY `idx_hr_contract_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `hr_contract_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint NOT NULL,
  `record_type` varchar(30) NOT NULL,
  `old_value` text,
  `new_value` text,
  `change_reason` varchar(500) DEFAULT NULL,
  `operator_id` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_hr_contract_record_contract` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Compatibility Alterations
-- ============================================================================
ALTER TABLE `hr_employee` ADD COLUMN `org_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN `dept_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN `position_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN `grade_level` varchar(30) DEFAULT NULL;

SET FOREIGN_KEY_CHECKS = 1;