-- 组织架构相关表结构

-- 组织表（公司/医院主体）
CREATE TABLE `hr_org` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_code` varchar(50) NOT NULL COMMENT '组织编码',
  `org_name` varchar(100) NOT NULL COMMENT '组织名称',
  `org_type` varchar(20) NOT NULL COMMENT '组织类型：company-公司，hospital-医院，branch-分公司，campus-院区',
  `parent_id` bigint DEFAULT '0' COMMENT '父组织ID',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `unified_social_credit_code` varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `industry_type` varchar(20) DEFAULT 'company' COMMENT '行业类型：company-企业，hospital-医院',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_org_type` (`org_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

-- 部门/科室表
CREATE TABLE `hr_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_code` varchar(50) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `dept_type` varchar(20) NOT NULL COMMENT '部门类型：dept-部门，office-办公室，ward-病区，clinic-门诊，emergency-急诊，medical-医技，nursing-护理，admin-行政后勤',
  `dept_level` int DEFAULT '1' COMMENT '部门层级',
  `leader_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `employee_count` int DEFAULT '0' COMMENT '员工数量',
  `budget_headcount` int DEFAULT '0' COMMENT '编制人数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_dept_type` (`dept_type`)
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
  `headcount` int DEFAULT '1' COMMENT '编制人数',
  `current_count` int DEFAULT '0' COMMENT '现有人数',
  `vacancy_count` int DEFAULT '0' COMMENT '空缺人数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_code` (`position_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_position_category` (`position_category`)
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
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rank_code` (`rank_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_rank_grade` (`rank_grade`),
  KEY `idx_rank_series` (`rank_series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职级职等表';

-- 编制类型表
CREATE TABLE `hr_employment_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编制类型ID',
  `type_code` varchar(50) NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编制类型表';

-- 成本中心表
CREATE TABLE `hr_cost_center` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成本中心ID',
  `center_code` varchar(50) NOT NULL COMMENT '成本中心编码',
  `center_name` varchar(100) NOT NULL COMMENT '成本中心名称',
  `org_id` bigint NOT NULL COMMENT '所属组织ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父成本中心ID',
  `center_level` int DEFAULT '1' COMMENT '成本中心层级',
  `manager_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `manager_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `budget_amount` decimal(15,2) DEFAULT '0.00' COMMENT '预算金额',
  `actual_amount` decimal(15,2) DEFAULT '0.00' COMMENT '实际金额',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_center_code` (`center_code`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本中心表';
