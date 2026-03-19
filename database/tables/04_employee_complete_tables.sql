-- 员工相关表结构
-- 包含员工基础信息、任职信息、家庭成员、教育经历、工作经历、附件、异动记录

-- 员工基础信息表
CREATE TABLE `hr_employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_no` VARCHAR(32) NOT NULL COMMENT '员工编号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT NOT NULL COMMENT '性别：1-男，2-女',
    `birthday` DATE COMMENT '出生日期',
    `id_card_no` VARCHAR(18) COMMENT '身份证号',
    `mobile` VARCHAR(11) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `marital_status` TINYINT COMMENT '婚姻状况：1-未婚，2-已婚，3-离异，4-丧偶',
    `nationality` VARCHAR(50) COMMENT '国籍',
    `domicile_address` VARCHAR(255) COMMENT '户籍地址',
    `current_address` VARCHAR(255) COMMENT '现住址',
    `employee_status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态：1-在职，2-离职，3-退休',
    `industry_type` VARCHAR(20) NOT NULL COMMENT '行业类型：company-企业，hospital-医院',
    `ext_json` JSON COMMENT '扩展字段（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_by` BIGINT COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_name` (`name`),
    KEY `idx_mobile` (`mobile`),
    KEY `idx_employee_status` (`employee_status`),
    KEY `idx_industry_type` (`industry_type`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工基础信息表';

-- 员工任职信息表
CREATE TABLE `hr_employee_job` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `org_id` BIGINT NOT NULL COMMENT '所属组织ID',
    `dept_id` BIGINT NOT NULL COMMENT '所属部门ID',
    `position_id` BIGINT NOT NULL COMMENT '岗位ID',
    `rank_id` BIGINT COMMENT '职级ID',
    `leader_id` BIGINT COMMENT '直属领导ID',
    `employee_type` VARCHAR(20) NOT NULL COMMENT '员工类型：formal-正式工，contract-合同工，intern-实习生',
    `employment_type` VARCHAR(20) NOT NULL COMMENT '用工类型：fulltime-全职，parttime-兼职',
    `entry_date` DATE NOT NULL COMMENT '入职日期',
    `regular_date` DATE COMMENT '转正日期',
    `work_location` VARCHAR(100) COMMENT '工作地点',
    `is_main_job` TINYINT NOT NULL DEFAULT 1 COMMENT '是否主任职：1-是，0-否',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-在职，2-离职',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_position_id` (`position_id`),
    KEY `idx_is_main_job` (`is_main_job`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_employee_job_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_employee_job_org` FOREIGN KEY (`org_id`) REFERENCES `hr_org` (`id`),
    CONSTRAINT `fk_employee_job_dept` FOREIGN KEY (`dept_id`) REFERENCES `hr_department` (`id`),
    CONSTRAINT `fk_employee_job_position` FOREIGN KEY (`position_id`) REFERENCES `hr_position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工任职信息表';

-- 员工家庭成员表
CREATE TABLE `hr_employee_family` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `relationship` VARCHAR(20) NOT NULL COMMENT '关系：father-父亲，mother-母亲，spouse-配偶，child-子女',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `birthday` DATE COMMENT '出生日期',
    `id_card_no` VARCHAR(18) COMMENT '身份证号',
    `mobile` VARCHAR(11) COMMENT '手机号',
    `occupation` VARCHAR(100) COMMENT '职业',
    `work_unit` VARCHAR(100) COMMENT '工作单位',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    CONSTRAINT `fk_employee_family_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工家庭成员表';

-- 员工教育经历表
CREATE TABLE `hr_employee_education` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `school_name` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `education_level` VARCHAR(20) NOT NULL COMMENT '学历层次：primary-小学，middle-初中，high-高中，college-大专，bachelor-本科，master-硕士，doctor-博士',
    `major` VARCHAR(100) COMMENT '专业',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `is_highest` TINYINT NOT NULL DEFAULT 0 COMMENT '是否最高学历：1-是，0-否',
    `degree_type` VARCHAR(20) COMMENT '学位类型：bachelor-学士，master-硕士，doctor-博士',
    `graduation_certificate` VARCHAR(100) COMMENT '毕业证书编号',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_is_highest` (`is_highest`),
    CONSTRAINT `fk_employee_education_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工教育经历表';

-- 员工工作经历表
CREATE TABLE `hr_employee_work_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `position` VARCHAR(100) NOT NULL COMMENT '职位',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `job_description` TEXT COMMENT '工作描述',
    `resign_reason` VARCHAR(500) COMMENT '离职原因',
    `witness` VARCHAR(50) COMMENT '证明人',
    `witness_mobile` VARCHAR(11) COMMENT '证明人电话',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    CONSTRAINT `fk_employee_work_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工工作经历表';

-- 员工附件表
CREATE TABLE `hr_employee_attachment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `attachment_type` VARCHAR(20) NOT NULL COMMENT '附件类型：id_card-身份证，diploma-毕业证，degree-学位证，contract-劳动合同，resume-简历，other-其他',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_attachment_type` (`attachment_type`),
    CONSTRAINT `fk_employee_attachment_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工附件表';

-- 员工异动记录表
CREATE TABLE `hr_employee_change_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT '异动类型：entry-入职，transfer-调动，promotion-晋升，demotion-降职，resign-离职，retire-退休',
    `change_date` DATE NOT NULL COMMENT '异动日期',
    `before_value` JSON COMMENT '变更前值',
    `after_value` JSON COMMENT '变更后值',
    `change_reason` VARCHAR(500) COMMENT '异动原因',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_change_date` (`change_date`),
    CONSTRAINT `fk_employee_change_employee` FOREIGN KEY (`employee_id`) REFERENCES `hr_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工异动记录表';
