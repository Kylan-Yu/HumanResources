
-- ============================================================================
-- HRMS Full Database Script
-- Version: 4.0.0
-- Date: 2026-03-24
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '+08:00';

DROP DATABASE IF EXISTS `hrms_db`;
CREATE DATABASE `hrms_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE `hrms_db`;

-- ============================================================================
-- 1. System
-- ============================================================================

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

-- ============================================================================
-- 2. Organization
-- ============================================================================

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

-- ============================================================================
-- 3. Employee
-- ============================================================================

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

-- ============================================================================
-- 4. Recruit
-- ============================================================================

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

-- ============================================================================
-- 5. Payroll
-- ============================================================================

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

-- ============================================================================
-- 6. Performance
-- ============================================================================

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

-- ============================================================================
-- 7. Training
-- ============================================================================

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

-- ============================================================================
-- 8. Contract
-- ============================================================================

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
-- 9. Compatibility Adjustments
-- ============================================================================

ALTER TABLE `hr_employee` ADD COLUMN IF NOT EXISTS `org_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN IF NOT EXISTS `dept_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN IF NOT EXISTS `position_id` bigint DEFAULT NULL;
ALTER TABLE `hr_employee` ADD COLUMN IF NOT EXISTS `grade_level` varchar(30) DEFAULT NULL;

DROP VIEW IF EXISTS `hr_organization`;
CREATE VIEW `hr_organization` AS
SELECT
  `id`,
  `org_code`,
  `org_name`,
  `org_name` AS `name`,
  `org_type`,
  `parent_id`,
  `industry_type`,
  `status`,
  `sort_order`,
  `ext_json`,
  `created_by`,
  `created_time`,
  `updated_by`,
  `updated_time`,
  `deleted`
FROM `hr_org`;

DROP VIEW IF EXISTS `hr_department`;
CREATE VIEW `hr_department` AS
SELECT
  `id`,
  `dept_code`,
  `dept_name`,
  `dept_name` AS `name`,
  `org_id`,
  `parent_id`,
  `dept_type`,
  `manager`,
  `phone`,
  `email`,
  `address`,
  `description`,
  `status`,
  `sort_order`,
  `ext_json`,
  `created_by`,
  `created_time`,
  `updated_by`,
  `updated_time`,
  `deleted`
FROM `hr_dept`;

-- ============================================================================
-- 10. Seed Data
-- ============================================================================

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`, `industry_type`, `deleted`)
VALUES
  (1, 'admin', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXf8v8fQwMCKjRZ3eLaxhUJQ6Qcgf2W', '系统管理员', 'admin@hrms.com', '13800000000', 1, 'company', 0),
  (2, 'hr_admin', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXf8v8fQwMCKjRZ3eLaxhUJQ6Qcgf2W', 'HR管理员', 'hr@hrms.com', '13800000001', 1, 'company', 0);

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `sort_order`, `deleted`)
VALUES
  (1, 'ADMIN', '管理员', '系统管理员', 1, 1, 0),
  (2, 'HR', 'HR专员', '人力资源岗位', 1, 2, 0),
  (3, 'MANAGER', '部门经理', '部门负责人', 1, 3, 0);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`, `deleted`)
VALUES
  (1, 0, '系统管理', 1, '/system', NULL, NULL, 'SettingOutlined', 1, 1, 1, 0),
  (2, 0, '组织管理', 1, '/org', NULL, NULL, 'TeamOutlined', 2, 1, 1, 0),
  (3, 0, '员工管理', 1, '/employee', NULL, NULL, 'UsergroupAddOutlined', 3, 1, 1, 0),
  (4, 0, '招聘管理', 1, '/recruit', NULL, NULL, 'UserOutlined', 4, 1, 1, 0),
  (5, 0, '薪酬管理', 1, '/payroll', NULL, NULL, 'PayCircleOutlined', 5, 1, 1, 0),
  (6, 0, '绩效管理', 1, '/performance', NULL, NULL, 'TrophyOutlined', 6, 1, 1, 0),
  (7, 0, '培训管理', 1, '/training', NULL, NULL, 'BookOutlined', 7, 1, 1, 0);

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
  (1, 1, 1),
  (2, 2, 2);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
  (1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7),
  (8, 2, 2), (9, 2, 3), (10, 2, 4), (11, 2, 5), (12, 2, 6), (13, 2, 7);

INSERT INTO `hr_org` (`id`, `org_code`, `org_name`, `org_type`, `parent_id`, `industry_type`, `status`, `sort_order`, `deleted`)
VALUES
  (1, 'ORG001', '总部', 'COMPANY', 0, 'company', 1, 1, 0),
  (2, 'ORG002', '华东分部', 'BRANCH', 1, 'company', 1, 2, 0),
  (3, 'ORG003', '医疗事业部', 'DIVISION', 1, 'hospital', 1, 3, 0);

INSERT INTO `hr_dept` (`id`, `dept_code`, `dept_name`, `org_id`, `parent_id`, `dept_type`, `manager`, `phone`, `status`, `sort_order`, `deleted`)
VALUES
  (1, 'DEPT001', '人力资源部', 1, 0, 'FUNCTIONAL', '张敏', '021-10001', 1, 1, 0),
  (2, 'DEPT002', '技术部', 1, 0, 'FUNCTIONAL', '李强', '021-10002', 1, 2, 0),
  (3, 'DEPT003', '招聘组', 1, 1, 'FUNCTIONAL', '王欣', '021-10003', 1, 3, 0),
  (4, 'DEPT004', '薪酬绩效组', 1, 1, 'FUNCTIONAL', '周玲', '021-10004', 1, 4, 0),
  (5, 'DEPT005', '培训发展组', 1, 1, 'FUNCTIONAL', '陈晨', '021-10005', 1, 5, 0);

INSERT INTO `hr_rank` (`id`, `rank_code`, `rank_name`, `rank_series`, `rank_level`, `description`, `status`, `sort_order`, `industry_type`, `deleted`)
VALUES
  (1, 'P1', '专员', '专业序列', 1, '初级岗位', 1, 1, 'company', 0),
  (2, 'P2', '高级专员', '专业序列', 2, '中级岗位', 1, 2, 'company', 0),
  (3, 'M1', '主管', '管理序列', 3, '基层管理', 1, 3, 'company', 0),
  (4, 'M2', '经理', '管理序列', 4, '中层管理', 1, 4, 'company', 0);

INSERT INTO `hr_position` (`id`, `position_code`, `position_name`, `org_id`, `dept_id`, `position_category`, `rank_grade`, `rank_series`, `job_description`, `status`, `sort_order`, `deleted`)
VALUES
  (1, 'POS001', 'HR专员', 1, 1, 'HR', 'P1', '专业序列', '负责员工档案维护、招聘执行', 1, 1, 0),
  (2, 'POS002', '招聘专员', 1, 3, 'HR', 'P2', '专业序列', '负责招聘需求管理与候选人流程推进', 1, 2, 0),
  (3, 'POS003', '薪酬绩效专员', 1, 4, 'HR', 'P2', '专业序列', '负责薪资标准、绩效核算', 1, 3, 0),
  (4, 'POS004', '培训专员', 1, 5, 'HR', 'P2', '专业序列', '负责培训课程与计划执行', 1, 4, 0),
  (5, 'POS005', '技术经理', 1, 2, 'TECH', 'M2', '管理序列', '负责研发团队管理', 1, 5, 0);

INSERT INTO `hr_employee` (`id`, `employee_no`, `name`, `gender`, `birthday`, `id_card_no`, `mobile`, `email`, `marital_status`, `nationality`, `employee_status`, `industry_type`, `remark`, `org_id`, `dept_id`, `position_id`, `grade_level`, `deleted`)
VALUES
  (1, 'EMP202603240001', '赵明', 1, '1992-06-10', '310101199206103311', '13900000001', 'zhaoming@hrms.com', 2, '中国', 1, 'company', '核心骨干', 1, 2, 5, 'M2', 0),
  (2, 'EMP202603240002', '孙婷', 2, '1995-09-21', '310101199509214526', '13900000002', 'sunting@hrms.com', 1, '中国', 1, 'company', '招聘负责人', 1, 3, 2, 'P2', 0),
  (3, 'EMP202603240003', '何佳', 2, '1997-02-14', '310101199702144829', '13900000003', 'hejia@hrms.com', 1, '中国', 1, 'company', '培训运营', 1, 5, 4, 'P2', 0),
  (4, 'EMP202603240004', '吴鹏', 1, '1990-11-05', '310101199011053319', '13900000004', 'wupeng@hrms.com', 2, '中国', 2, 'company', '离职样例', 1, 1, 1, 'P1', 0);

INSERT INTO `hr_employee_job` (`id`, `employee_id`, `org_id`, `dept_id`, `position_id`, `rank_id`, `leader_id`, `employee_type`, `employment_type`, `entry_date`, `regular_date`, `work_location`, `is_main_job`, `status`, `deleted`)
VALUES
  (1, 1, 1, 2, 5, 4, NULL, 'formal', 'fulltime', '2020-05-01', '2020-08-01', '上海', 1, 1, 0),
  (2, 2, 1, 3, 2, 2, 1, 'formal', 'fulltime', '2021-03-15', '2021-06-15', '上海', 1, 1, 0),
  (3, 3, 1, 5, 4, 2, 1, 'formal', 'fulltime', '2022-04-01', '2022-07-01', '上海', 1, 1, 0),
  (4, 4, 1, 1, 1, 1, 2, 'formal', 'fulltime', '2019-02-01', '2019-05-01', '上海', 1, 2, 0);

INSERT INTO `hr_employee_change_record` (`id`, `employee_id`, `change_type`, `change_date`, `before_value`, `after_value`, `change_reason`, `approver_id`, `approve_time`, `remark`, `deleted`)
VALUES
  (1, 2, 'promotion', '2024-12-01', '招聘专员(P1)', '招聘专员(P2)', '年度晋升', 1, '2024-11-28 09:00:00', '表现优秀', 0),
  (2, 4, 'resign', '2025-10-31', '在职', '离职', '个人发展', 1, '2025-10-25 14:00:00', '完成交接', 0);

INSERT INTO `hr_recruit_requirement` (`id`, `requirement_no`, `title`, `org_id`, `dept_id`, `position_id`, `headcount`, `urgency_level`, `requirement_status`, `expected_entry_date`, `reason`, `industry_type`, `remark`, `deleted`)
VALUES
  (1, 'REQ20260324001', '招聘Java开发工程师', 1, 2, 5, 3, 'HIGH', 'OPEN', '2026-05-15', '团队扩编', 'company', '重点岗位', 0),
  (2, 'REQ20260324002', '招聘培训专员', 1, 5, 4, 1, 'MEDIUM', 'DRAFT', '2026-06-01', '替补岗位', 'company', NULL, 0);

INSERT INTO `hr_payroll_standard` (`id`, `standard_name`, `org_id`, `dept_id`, `position_id`, `grade_level`, `base_salary`, `performance_salary`, `position_allowance`, `meal_allowance`, `transport_allowance`, `communication_allowance`, `housing_allowance`, `other_allowance`, `status`, `industry_type`, `remark`, `deleted`)
VALUES
  (1, '技术经理薪资标准', 1, 2, 5, 'M2', 26000, 8000, 3000, 600, 600, 400, 2500, 500, 'ACTIVE', 'company', '核心岗位标准', 0),
  (2, '招聘专员薪资标准', 1, 3, 2, 'P2', 12000, 3000, 1000, 400, 300, 200, 800, 300, 'ACTIVE', 'company', NULL, 0),
  (3, '培训专员薪资标准', 1, 5, 4, 'P2', 11000, 2500, 800, 400, 300, 200, 700, 200, 'ACTIVE', 'company', NULL, 0);

INSERT INTO `hr_performance_plan` (`id`, `plan_name`, `plan_year`, `plan_period`, `org_id`, `dept_id`, `status`, `description`, `industry_type`, `deleted`)
VALUES
  (1, '2026年Q1绩效考核', 2026, 'Q1', 1, NULL, 'RUNNING', '季度绩效考核', 'company', 0),
  (2, '2026年Q2绩效考核', 2026, 'Q2', 1, NULL, 'DRAFT', '季度绩效考核', 'company', 0);

INSERT INTO `hr_performance_record` (`id`, `plan_id`, `employee_id`, `employee_no`, `employee_name`, `org_id`, `dept_id`, `position_id`, `score`, `grade`, `result_status`, `self_summary`, `manager_comment`, `hr_comment`, `industry_type`, `deleted`)
VALUES
  (1, 1, 1, 'EMP202603240001', '赵明', 1, 2, 5, 92.5, 'A', 'COMPLETED', '完成核心项目交付', '结果超预期', '建议纳入晋升观察', 'company', 0),
  (2, 1, 2, 'EMP202603240002', '孙婷', 1, 3, 2, 88.0, 'B+', 'COMPLETED', '按计划完成招聘需求', '执行稳定', '可加强数据分析能力', 'company', 0);

INSERT INTO `hr_training_course` (`id`, `course_code`, `course_name`, `course_type`, `lecturer`, `duration_hours`, `description`, `status`, `industry_type`, `deleted`)
VALUES
  (1, 'COURSE001', '新员工入职培训', 'ONBOARDING', '人力资源部', 8.0, '公司制度与流程培训', 'ACTIVE', 'company', 0),
  (2, 'COURSE002', '绩效管理实务', 'PROFESSIONAL', '外部讲师', 6.0, '绩效目标制定与反馈技巧', 'ACTIVE', 'company', 0);

INSERT INTO `hr_training_session` (`id`, `course_id`, `session_name`, `start_time`, `end_time`, `location`, `capacity`, `status`, `industry_type`, `deleted`)
VALUES
  (1, 1, '2026年3月入职班', '2026-03-28 09:00:00', '2026-03-28 18:00:00', '总部4楼培训室', 40, 'PLANNED', 'company', 0),
  (2, 2, '2026年Q2绩效培训班', '2026-04-15 14:00:00', '2026-04-15 18:00:00', '总部2楼会议室', 60, 'PLANNED', 'company', 0);

INSERT INTO `hr_training_enrollment` (`id`, `session_id`, `employee_id`, `attendance_status`, `score`, `feedback`, `deleted`)
VALUES
  (1, 1, 2, 'REGISTERED', NULL, NULL, 0),
  (2, 1, 3, 'REGISTERED', NULL, NULL, 0),
  (3, 2, 1, 'REGISTERED', NULL, NULL, 0);

INSERT INTO `hr_contract` (`id`, `employee_id`, `contract_no`, `contract_type`, `contract_subject`, `start_date`, `end_date`, `sign_date`, `contract_status`, `renew_count`, `industry_type`, `remark`, `deleted`)
VALUES
  (1, 1, 'CT20260324001', 'LABOR_CONTRACT', '劳动合同', '2024-01-01', '2027-12-31', '2023-12-15', 'ACTIVE', 1, 'company', '三年期', 0),
  (2, 2, 'CT20260324002', 'LABOR_CONTRACT', '劳动合同', '2025-03-01', '2027-02-28', '2025-02-20', 'ACTIVE', 0, 'company', '两年期', 0),
  (3, 4, 'CT20260324003', 'LABOR_CONTRACT', '劳动合同', '2022-01-01', '2025-12-31', '2021-12-20', 'TERMINATED', 0, 'company', '离职归档', 0);

INSERT INTO `hr_contract_record` (`id`, `contract_id`, `record_type`, `old_value`, `new_value`, `change_reason`, `operator_id`)
VALUES
  (1, 1, 'RENEW', '2026-12-31', '2027-12-31', '续签一年', 1),
  (2, 3, 'STATUS', 'ACTIVE', 'TERMINATED', '员工离职', 1);

SET FOREIGN_KEY_CHECKS = 1;
