-- ====================================================================
-- 人力资源管理系统数据库完整初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2024-03-18
-- 说明: 包含建表、索引、初始化数据的完整SQL脚本
-- ====================================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '+08:00';

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hrms_db 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE hrms_db;

-- ====================================================================
-- 1. 系统管理相关表
-- ====================================================================

-- 用户表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `industry_type` varchar(20) DEFAULT 'company' COMMENT '行业类型：company-企业，hospital-医院',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 菜单表
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `visible` tinyint DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 字典类型表
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` tinyint DEFAULT 1 COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建者',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认（Y是 N否）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建者',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_dict_sort` (`dict_sort`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 操作日志表
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` tinyint DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` tinyint DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `operator_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` text COMMENT '请求参数',
  `json_result` text COMMENT '返回参数',
  `status` tinyint DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_operator_name` (`operator_name`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志记录';

-- 登录日志表
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` tinyint DEFAULT 0 COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统访问记录';

-- ====================================================================
-- 2. 组织架构相关表
-- ====================================================================

-- 组织表（公司/医院主体）
CREATE TABLE `hr_org` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_code` varchar(50) NOT NULL COMMENT '组织编码',
  `org_name` varchar(100) NOT NULL COMMENT '组织名称',
  `org_type` varchar(20) NOT NULL COMMENT '组织类型：company-公司，hospital-医院，branch-分公司，campus-院区',
  `parent_id` bigint DEFAULT 0 COMMENT '父组织ID',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `unified_social_credit_code` varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `industry_type` varchar(20) DEFAULT 'company' COMMENT '行业类型：company-企业，hospital-医院',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_org_type` (`org_type`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

-- 部门/科室表
CREATE TABLE `hr_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_code` varchar(50) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父部门ID',
  `dept_type` varchar(20) NOT NULL COMMENT '部门类型：dept-部门，office-办公室，ward-病区，clinic-门诊，emergency-急诊，medical-医技，nursing-护理，admin-行政后勤',
  `dept_level` int DEFAULT 1 COMMENT '部门层级',
  `leader_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `employee_count` int DEFAULT 0 COMMENT '员工数量',
  `budget_headcount` int DEFAULT 0 COMMENT '编制人数',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_dept_type` (`dept_type`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门/科室表';

-- 岗位表
CREATE TABLE `hr_position` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `position_code` varchar(50) NOT NULL COMMENT '岗位编码',
  `position_name` varchar(100) NOT NULL COMMENT '岗位名称',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `dept_id` bigint NOT NULL COMMENT '所属部门ID',
  `position_category` varchar(20) NOT NULL COMMENT '岗位类别：management-管理，technical-技术，medical-医疗，nursing-护理，pharmacy-药剂，laboratory-检验，admin-行政，finance-财务，hr-人事，other-其他',
  `position_level` varchar(20) DEFAULT NULL COMMENT '岗位级别',
  `job_description` text COMMENT '岗位职责',
  `job_requirements` text COMMENT '任职要求',
  `headcount` int DEFAULT 1 COMMENT '编制人数',
  `current_count` int DEFAULT 0 COMMENT '现有人数',
  `vacancy_count` int DEFAULT 0 COMMENT '空缺人数',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_code` (`position_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_position_category` (`position_category`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位表';

-- 职级职等表
CREATE TABLE `hr_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职级ID',
  `rank_code` varchar(50) NOT NULL COMMENT '职级编码',
  `rank_name` varchar(100) NOT NULL COMMENT '职级名称',
  `rank_level` int NOT NULL COMMENT '职级等级',
  `rank_grade` varchar(20) NOT NULL COMMENT '职等：P-专业序列，M-管理序列，T-技术序列',
  `rank_series` varchar(20) NOT NULL COMMENT '职级序列：management-管理，professional-专业，technical-技术，medical-医疗，nursing-护理',
  `min_salary` decimal(10,2) DEFAULT NULL COMMENT '最低薪资',
  `max_salary` decimal(10,2) DEFAULT NULL COMMENT '最高薪资',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rank_code` (`rank_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_rank_grade` (`rank_grade`),
  KEY `idx_rank_series` (`rank_series`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职级职等表';

-- 编制类型表
CREATE TABLE `hr_employment_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编制类型ID',
  `type_code` varchar(50) NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编制类型表';

-- 成本中心表
CREATE TABLE `hr_cost_center` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成本中心ID',
  `center_code` varchar(50) NOT NULL COMMENT '成本中心编码',
  `center_name` varchar(100) NOT NULL COMMENT '成本中心名称',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父成本中心ID',
  `center_level` int DEFAULT 1 COMMENT '成本中心层级',
  `manager_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `manager_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `budget_amount` decimal(15,2) DEFAULT 0.00 COMMENT '预算金额',
  `actual_amount` decimal(15,2) DEFAULT 0.00 COMMENT '实际金额',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_center_code` (`center_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本中心表';

-- ====================================================================
-- 3. 员工档案相关表
-- ====================================================================

-- 员工基础信息表
CREATE TABLE `hr_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `employee_code` varchar(50) NOT NULL COMMENT '员工编号',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别：1-男，2-女',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `nation` varchar(20) DEFAULT NULL COMMENT '民族',
  `id_card_type` varchar(20) DEFAULT 'id_card' COMMENT '证件类型：id_card-身份证，passport-护照，other-其他',
  `id_card_number` varchar(50) DEFAULT NULL COMMENT '证件号码',
  `marital_status` varchar(20) DEFAULT NULL COMMENT '婚姻状况：single-未婚，married-已婚，divorced-离异，widowed-丧偶',
  `political_status` varchar(20) DEFAULT NULL COMMENT '政治面貌：member-党员，league-团员，mass-群众',
  `native_place` varchar(100) DEFAULT NULL COMMENT '籍贯',
  `household_register` varchar(200) DEFAULT NULL COMMENT '户籍地址',
  `current_address` varchar(200) DEFAULT NULL COMMENT '现住址',
  `phone` varchar(20) NOT NULL COMMENT '手机号码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系电话',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `employee_status` varchar(20) DEFAULT 'pending' COMMENT '员工状态：pending-待入职，probation-试用期，regular-正式，transfer-调动，leave_absence-停薪留职，resign-离职，retire-退休',
  `industry_type` varchar(20) DEFAULT 'company' COMMENT '行业类型：company-企业，hospital-医院',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `probation_end_date` date DEFAULT NULL COMMENT '试用期结束日期',
  `resign_date` date DEFAULT NULL COMMENT '离职日期',
  `work_years` int DEFAULT 0 COMMENT '工作年限',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_code` (`employee_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_real_name` (`real_name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_employee_status` (`employee_status`),
  KEY `idx_industry_type` (`industry_type`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工基础信息表';

-- 员工任职信息表
CREATE TABLE `hr_employee_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任职信息ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `dept_id` bigint NOT NULL COMMENT '所属部门ID',
  `position_id` bigint NOT NULL COMMENT '岗位ID',
  `rank_id` bigint DEFAULT NULL COMMENT '职级ID',
  `employment_type_id` bigint DEFAULT NULL COMMENT '编制类型ID',
  `cost_center_id` bigint DEFAULT NULL COMMENT '成本中心ID',
  `job_title` varchar(100) DEFAULT NULL COMMENT '职务',
  `direct_supervisor_id` bigint DEFAULT NULL COMMENT '直属上级ID',
  `direct_supervisor_name` varchar(50) DEFAULT NULL COMMENT '直属上级姓名',
  `work_location` varchar(200) DEFAULT NULL COMMENT '工作地点',
  `is_main_job` tinyint DEFAULT 1 COMMENT '是否主岗位：0-否，1-是',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-生效，inactive-失效',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工任职信息表';

-- ====================================================================
-- 4. 初始化基础数据
-- ====================================================================

-- 插入系统组织
INSERT INTO `hr_org` (`org_code`, `org_name`, `org_type`, `parent_id`, `industry_type`, `status`, `sort_order`, `created_by`, `created_time`) VALUES
('HRMS_GROUP', 'HRMS集团', 'company', 0, 'company', 1, 1, 1, NOW()),
('HRMS_HOSPITAL', 'HRMS医院', 'hospital', 1, 'hospital', 1, 2, 1, NOW());

-- 插入字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`, `created_by`, `created_time`) VALUES
('系统状态', 'sys_status', 1, '系统通用状态', 1, NOW()),
('性别', 'gender', 1, '性别选择', 1, NOW()),
('婚姻状况', 'marital_status', 1, '婚姻状况', 1, NOW()),
('政治面貌', 'political_status', 1, '政治面貌', 1, NOW()),
('证件类型', 'id_card_type', 1, '证件类型', 1, NOW()),
('员工状态', 'employee_status', 1, '员工状态', 1, NOW()),
('学历层次', 'education_level', 1, '学历层次', 1, NOW()),
('学位', 'degree', 1, '学位', 1, NOW()),
('组织类型', 'org_type', 1, '组织类型', 1, NOW()),
('部门类型', 'dept_type', 1, '部门类型', 1, NOW()),
('岗位类别', 'position_category', 1, '岗位类别', 1, NOW()),
('职等', 'rank_grade', 1, '职等', 1, NOW()),
('职级序列', 'rank_series', 1, '职级序列', 1, NOW()),
('异动类型', 'change_type', 1, '员工异动类型', 1, NOW()),
('审批状态', 'approval_status', 1, '审批状态', 1, NOW());

-- 插入字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `created_by`, `created_time`) VALUES
-- 系统状态
(1, '启用', '1', 'sys_status', '', 'primary', 1, 1, '启用状态', 1, NOW()),
(2, '禁用', '0', 'sys_status', '', 'danger', 0, 1, '禁用状态', 1, NOW()),
-- 性别
(1, '男', '1', 'gender', '', 'primary', 0, 1, '男性', 1, NOW()),
(2, '女', '2', 'gender', '', 'success', 0, 1, '女性', 1, NOW()),
-- 婚姻状况
(1, '未婚', 'single', 'marital_status', '', 'info', 0, 1, '未婚', 1, NOW()),
(2, '已婚', 'married', 'marital_status', '', 'success', 0, 1, '已婚', 1, NOW()),
(3, '离异', 'divorced', 'marital_status', '', 'warning', 0, 1, '离异', 1, NOW()),
(4, '丧偶', 'widowed', 'marital_status', '', 'danger', 0, 1, '丧偶', 1, NOW()),
-- 政治面貌
(1, '党员', 'member', 'political_status', '', 'danger', 0, 1, '中共党员', 1, NOW()),
(2, '团员', 'league', 'political_status', '', 'primary', 0, 1, '共青团员', 1, NOW()),
(3, '群众', 'mass', 'political_status', '', 'default', 1, 1, '群众', 1, NOW()),
-- 证件类型
(1, '身份证', 'id_card', 'id_card_type', '', 'primary', 1, 1, '身份证', 1, NOW()),
(2, '护照', 'passport', 'id_card_type', '', 'info', 0, 1, '护照', 1, NOW()),
(3, '其他', 'other', 'id_card_type', '', 'default', 0, 1, '其他证件', 1, NOW()),
-- 员工状态
(1, '待入职', 'pending', 'employee_status', '', 'warning', 0, 1, '待入职', 1, NOW()),
(2, '试用期', 'probation', 'employee_status', '', 'processing', 0, 1, '试用期', 1, NOW()),
(3, '正式', 'regular', 'employee_status', '', 'success', 1, 1, '正式员工', 1, NOW()),
(4, '调动', 'transfer', 'employee_status', '', 'primary', 0, 1, '调动中', 1, NOW()),
(5, '停薪留职', 'leave_absence', 'employee_status', '', 'default', 0, 1, '停薪留职', 1, NOW()),
(6, '离职', 'resign', 'employee_status', '', 'danger', 0, 1, '已离职', 1, NOW()),
(7, '退休', 'retire', 'employee_status', '', 'info', 0, 1, '已退休', 1, NOW()),
-- 学历层次
(1, '高中', 'high_school', 'education_level', '', 'default', 0, 1, '高中', 1, NOW()),
(2, '大专', 'junior_college', 'education_level', '', 'info', 0, 1, '大专', 1, NOW()),
(3, '本科', 'bachelor', 'education_level', '', 'primary', 1, 1, '本科', 1, NOW()),
(4, '硕士', 'master', 'education_level', '', 'success', 0, 1, '硕士', 1, NOW()),
(5, '博士', 'doctor', 'education_level', '', 'danger', 0, 1, '博士', 1, NOW()),
-- 学位
(1, '专科', 'associate', 'degree', '', 'info', 0, 1, '专科', 1, NOW()),
(2, '学士', 'bachelor', 'degree', '', 'primary', 1, 1, '学士', 1, NOW()),
(3, '硕士', 'master', 'degree', '', 'success', 0, 1, '硕士', 1, NOW()),
(4, '博士', 'doctor', 'degree', '', 'danger', 0, 1, '博士', 1, NOW()),
-- 组织类型
(1, '公司', 'company', 'org_type', '', 'primary', 1, 1, '公司', 1, NOW()),
(2, '医院', 'hospital', 'org_type', '', 'success', 0, 1, '医院', 1, NOW()),
(3, '分公司', 'branch', 'org_type', '', 'info', 0, 1, '分公司', 1, NOW()),
(4, '院区', 'campus', 'org_type', '', 'warning', 0, 1, '院区', 1, NOW()),
-- 部门类型
(1, '部门', 'dept', 'dept_type', '', 'primary', 1, 1, '普通部门', 1, NOW()),
(2, '办公室', 'office', 'dept_type', '', 'info', 0, 1, '办公室', 1, NOW()),
(3, '病区', 'ward', 'dept_type', '', 'success', 0, 1, '病区', 1, NOW()),
(4, '门诊', 'clinic', 'dept_type', '', 'warning', 0, 1, '门诊', 1, NOW()),
(5, '急诊', 'emergency', 'dept_type', '', 'danger', 0, 1, '急诊', 1, NOW()),
(6, '医技', 'medical', 'dept_type', '', 'processing', 0, 1, '医技科室', 1, NOW()),
(7, '护理', 'nursing', 'dept_type', '', 'cyan', 0, 1, '护理部门', 1, NOW()),
(8, '行政后勤', 'admin', 'dept_type', '', 'default', 0, 1, '行政后勤', 1, NOW()),
-- 岗位类别
(1, '管理', 'management', 'position_category', '', 'danger', 0, 1, '管理岗位', 1, NOW()),
(2, '技术', 'technical', 'position_category', '', 'purple', 0, 1, '技术岗位', 1, NOW()),
(3, '医疗', 'medical', 'position_category', '', 'success', 0, 1, '医疗岗位', 1, NOW()),
(4, '护理', 'nursing', 'position_category', '', 'cyan', 0, 1, '护理岗位', 1, NOW()),
(5, '药剂', 'pharmacy', 'position_category', '', 'warning', 0, 1, '药剂岗位', 1, NOW()),
(6, '检验', 'laboratory', 'position_category', '', 'orange', 0, 1, '检验岗位', 1, NOW()),
(7, '行政', 'admin', 'position_category', '', 'default', 1, 1, '行政岗位', 1, NOW()),
(8, '财务', 'finance', 'position_category', '', 'gold', 0, 1, '财务岗位', 1, NOW()),
(9, '人事', 'hr', 'position_category', '', 'lime', 0, 1, '人事岗位', 1, NOW()),
(10, '其他', 'other', 'position_category', '', 'default', 0, 1, '其他岗位', 1, NOW()),
-- 职等
(1, '专业序列', 'P', 'rank_grade', '', 'primary', 0, 1, '专业序列', 1, NOW()),
(2, '管理序列', 'M', 'rank_grade', '', 'danger', 0, 1, '管理序列', 1, NOW()),
(3, '技术序列', 'T', 'rank_grade', '', 'purple', 0, 1, '技术序列', 1, NOW()),
-- 职级序列
(1, '管理', 'management', 'rank_series', '', 'danger', 0, 1, '管理序列', 1, NOW()),
(2, '专业', 'professional', 'rank_series', '', 'primary', 0, 1, '专业序列', 1, NOW()),
(3, '技术', 'technical', 'rank_series', '', 'purple', 0, 1, '技术序列', 1, NOW()),
(4, '医疗', 'medical', 'rank_series', '', 'success', 0, 1, '医疗序列', 1, NOW()),
(5, '护理', 'nursing', 'rank_series', '', 'cyan', 0, 1, '护理序列', 1, NOW()),
-- 异动类型
(1, '入职', 'hire', 'change_type', '', 'success', 0, 1, '入职', 1, NOW()),
(2, '调岗', 'transfer', 'change_type', '', 'primary', 0, 1, '调岗', 1, NOW()),
(3, '晋升', 'promotion', 'change_type', '', 'success', 0, 1, '晋升', 1, NOW()),
(4, '降职', 'demotion', 'change_type', '', 'warning', 0, 1, '降职', 1, NOW()),
(5, '调薪', 'salary_adjust', 'change_type', '', 'gold', 0, 1, '调薪', 1, NOW()),
(6, '离职', 'resign', 'change_type', '', 'danger', 0, 1, '离职', 1, NOW()),
(7, '退休', 'retire', 'change_type', '', 'info', 0, 1, '退休', 1, NOW()),
-- 审批状态
(1, '待审批', 'pending', 'approval_status', '', 'warning', 0, 1, '待审批', 1, NOW()),
(2, '已审批', 'approved', 'approval_status', '', 'success', 1, 1, '已审批', 1, NOW()),
(3, '已拒绝', 'rejected', 'approval_status', '', 'danger', 0, 1, '已拒绝', 1, NOW());

-- 插入系统用户
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `phone`, `status`, `industry_type`, `created_by`, `created_time`) VALUES
('admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', '系统管理员', 'admin@hrms.com', '13800138000', 1, 'company', 1, NOW()),
('hr_admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', 'HR管理员', 'hr@hrms.com', '13800138001', 1, 'company', 1, NOW());

-- 插入系统角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `sort_order`, `created_by`, `created_time`) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1, 1, NOW()),
('HR_ADMIN', 'HR管理员', 'HR管理员，拥有人力资源管理权限', 1, 2, 1, NOW()),
('DEPT_MANAGER', '部门负责人', '部门负责人，拥有本部门管理权限', 1, 3, 1, NOW()),
('EMPLOYEE', '普通员工', '普通员工，只能查看自己的信息', 1, 4, 1, NOW());

-- 插入用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `created_by`, `created_time`) VALUES
(1, 1, 1, NOW()),
(2, 2, 1, NOW());

-- 插入系统菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`, `created_by`, `created_time`) VALUES
(0, '系统管理', 1, '/system', NULL, NULL, 'Setting', 99, 1, 1, 1, NOW()),
(100, '用户管理', 2, '/system/user', 'system/User', 'system:user:list', 'User', 1, 1, 1, 1, NOW()),
(100, '角色管理', 2, '/system/role', 'system/Role', 'system:role:list', 'UserGroup', 2, 1, 1, 1, NOW()),
(100, '菜单管理', 2, '/system/menu', 'system/Menu', 'system:menu:list', 'Menu', 3, 1, 1, 1, NOW()),
(100, '字典管理', 2, '/system/dict', 'system/Dict', 'system:dict:list', 'FileText', 4, 1, 1, 1, NOW()),
(0, '组织管理', 1, '/org', NULL, NULL, NULL, 'Apartment', 2, 1, 1, 1, NOW()),
(200, '组织架构', 2, '/org/tree', 'org/Tree', 'org:tree:list', 'Apartment', 1, 1, 1, 1, NOW()),
(200, '部门管理', 2, '/org/dept', 'org/Dept', 'org:dept:list', 'Team', 2, 1, 1, 1, NOW()),
(200, '岗位管理', 2, '/org/position', 'org/Position', 'org:position:list', 'Idcard', 3, 1, 1, 1, NOW()),
(200, '职级管理', 2, '/org/rank', 'org/Rank', 'org:rank:list', 'Trophy', 4, 1, 1, 1, NOW()),
(0, '员工管理', 1, '/employee', NULL, NULL, NULL, 'User', 3, 1, 1, 1, NOW()),
(300, '员工档案', 2, '/employee/list', 'employee/List', 'employee:list', 'User', 1, 1, 1, 1, NOW()),
(300, '员工异动', 2, '/employee/change', 'employee/Change', 'employee:change:list', 'Swap', 2, 1, 1, 1, NOW()),
(0, '工作台', 1, '/dashboard', NULL, NULL, NULL, 'Dashboard', 1, 1, 1, 1, NOW()),
(400, '首页', 2, '/dashboard/index', 'dashboard/Index', 'dashboard:view', 'Dashboard', 1, 1, 1, 1, NOW());

-- 插入角色菜单关联
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `created_by`, `created_time`) VALUES
-- 超级管理员拥有所有菜单权限
(1, 100, 1, NOW()),
(1, 101, 1, NOW()),
(1, 102, 1, NOW()),
(1, 103, 1, NOW()),
(1, 104, 1, NOW()),
(1, 200, 1, NOW()),
(1, 201, 1, NOW()),
(1, 202, 1, NOW()),
(1, 203, 1, NOW()),
(1, 204, 1, NOW()),
(1, 300, 1, NOW()),
(1, 301, 1, NOW()),
(1, 302, 1, NOW()),
(1, 400, 1, NOW()),
(1, 401, 1, NOW()),
-- HR管理员拥有HR相关权限
(2, 200, 1, NOW()),
(2, 201, 1, NOW()),
(2, 202, 1, NOW()),
(2, 203, 1, NOW()),
(2, 204, 1, NOW()),
(2, 300, 1, NOW()),
(2, 301, 1, NOW()),
(2, 302, 1, NOW()),
(2, 400, 1, NOW()),
(2, 401, 1, NOW());

-- ====================================================================
-- 初始化完成
-- ====================================================================

SET FOREIGN_KEY_CHECKS = 1;

-- 输出初始化完成信息
SELECT 'HRMS数据库初始化完成！' AS message;
SELECT '测试账号：admin/123456' AS account;
SELECT '数据库版本：1.0.0' AS version;
