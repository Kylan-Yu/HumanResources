-- 员工档案相关表结构

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
  `work_years` int DEFAULT '0' COMMENT '工作年限',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_code` (`employee_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_real_name` (`real_name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_employee_status` (`employee_status`)
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
  `is_main_job` tinyint DEFAULT '1' COMMENT '是否主岗位：0-否，1-是',
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
  KEY `idx_effective_date` (`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工任职信息表';

-- 员工联系方式表
CREATE TABLE `hr_employee_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系方式ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `contact_type` varchar(20) NOT NULL COMMENT '联系方式类型：phone-手机，email-邮箱，wechat-微信，qq-QQ，other-其他',
  `contact_value` varchar(200) NOT NULL COMMENT '联系方式值',
  `is_primary` tinyint DEFAULT '0' COMMENT '是否主要：0-否，1-是',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_contact_type` (`contact_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工联系方式表';

-- 员工家庭成员表
CREATE TABLE `hr_employee_family` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '家庭成员ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `relationship` varchar(20) NOT NULL COMMENT '关系：father-父亲，mother-母亲，spouse-配偶，child-子女，other-其他',
  `gender` tinyint DEFAULT NULL COMMENT '性别：1-男，2-女',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `id_card_number` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `work_unit` varchar(200) DEFAULT NULL COMMENT '工作单位',
  `occupation` varchar(100) DEFAULT NULL COMMENT '职业',
  `is_emergency_contact` tinyint DEFAULT '0' COMMENT '是否紧急联系人：0-否，1-是',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工家庭成员表';

-- 员工教育经历表
CREATE TABLE `hr_employee_education` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `school_name` varchar(200) NOT NULL COMMENT '学校名称',
  `major` varchar(200) NOT NULL COMMENT '专业',
  `education_level` varchar(20) NOT NULL COMMENT '学历层次：high_school-高中，junior_college-大专，bachelor-本科，master-硕士，doctor-博士',
  `degree` varchar(20) DEFAULT NULL COMMENT '学位：associate-专科，bachelor-学士，master-硕士，doctor-博士',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `is_full_time` tinyint DEFAULT '1' COMMENT '是否全日制：0-否，1-是',
  `graduation_certificate` varchar(255) DEFAULT NULL COMMENT '毕业证书',
  `degree_certificate` varchar(255) DEFAULT NULL COMMENT '学位证书',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工教育经历表';

-- 员工工作经历表
CREATE TABLE `hr_employee_work_experience` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作经历ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `company_name` varchar(200) NOT NULL COMMENT '公司名称',
  `industry` varchar(100) DEFAULT NULL COMMENT '所属行业',
  `position` varchar(100) NOT NULL COMMENT '职位',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `is_current` tinyint DEFAULT '0' COMMENT '是否当前工作：0-否，1-是',
  `work_description` text COMMENT '工作描述',
  `salary` decimal(10,2) DEFAULT NULL COMMENT '薪资',
  `leaving_reason` varchar(500) DEFAULT NULL COMMENT '离职原因',
  `reference_name` varchar(50) DEFAULT NULL COMMENT '证明人',
  `reference_phone` varchar(20) DEFAULT NULL COMMENT '证明人电话',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工工作经历表';

-- 员工银行卡信息表
CREATE TABLE `hr_employee_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '银行卡ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `bank_name` varchar(100) NOT NULL COMMENT '银行名称',
  `account_name` varchar(50) NOT NULL COMMENT '账户名称',
  `account_number` varchar(50) NOT NULL COMMENT '银行账号',
  `bank_branch` varchar(200) DEFAULT NULL COMMENT '开户支行',
  `is_default` tinyint DEFAULT '1' COMMENT '是否默认：0-否，1-是',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-有效，inactive-无效',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工银行卡信息表';

-- 员工附件表
CREATE TABLE `hr_employee_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `attachment_type` varchar(20) NOT NULL COMMENT '附件类型：id_card-身份证，education-学历，degree-学位，certificate-证书，contract-合同，resume-简历，photo-照片，other-其他',
  `attachment_name` varchar(200) NOT NULL COMMENT '附件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传人ID',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_attachment_type` (`attachment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工附件表';

-- 员工异动记录表
CREATE TABLE `hr_employee_change_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异动记录ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `change_type` varchar(20) NOT NULL COMMENT '异动类型：hire-入职，transfer-调岗，promotion-晋升，demotion-降职，salary_adjust-调薪，resign-离职，retire-退休',
  `change_date` date NOT NULL COMMENT '异动日期',
  `before_dept_id` bigint DEFAULT NULL COMMENT '异动前部门ID',
  `before_dept_name` varchar(100) DEFAULT NULL COMMENT '异动前部门名称',
  `before_position_id` bigint DEFAULT NULL COMMENT '异动前岗位ID',
  `before_position_name` varchar(100) DEFAULT NULL COMMENT '异动前岗位名称',
  `before_rank_id` bigint DEFAULT NULL COMMENT '异动前职级ID',
  `before_rank_name` varchar(100) DEFAULT NULL COMMENT '异动前职级名称',
  `before_salary` decimal(10,2) DEFAULT NULL COMMENT '异动前薪资',
  `after_dept_id` bigint DEFAULT NULL COMMENT '异动后部门ID',
  `after_dept_name` varchar(100) DEFAULT NULL COMMENT '异动后部门名称',
  `after_position_id` bigint DEFAULT NULL COMMENT '异动后岗位ID',
  `after_position_name` varchar(100) DEFAULT NULL COMMENT '异动后岗位名称',
  `after_rank_id` bigint DEFAULT NULL COMMENT '异动后职级ID',
  `after_rank_name` varchar(100) DEFAULT NULL COMMENT '异动后职级名称',
  `after_salary` decimal(10,2) DEFAULT NULL COMMENT '异动后薪资',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '异动原因',
  `approval_status` varchar(20) DEFAULT 'pending' COMMENT '审批状态：pending-待审批，approved-已审批，rejected-已拒绝',
  `approval_user_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_remark` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_change_date` (`change_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工异动记录表';
