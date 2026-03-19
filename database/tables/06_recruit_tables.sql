-- 招聘管理相关表结构

-- 招聘需求表
CREATE TABLE hr_recruit_requirement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    requirement_no VARCHAR(50) NOT NULL UNIQUE COMMENT '需求编号',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    org_id BIGINT NOT NULL COMMENT '组织ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    position_id BIGINT NOT NULL COMMENT '岗位ID',
    headcount INT NOT NULL COMMENT '招聘人数',
    urgency_level VARCHAR(20) NOT NULL COMMENT '紧急程度',
    requirement_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '需求状态',
    expected_entry_date DATE NOT NULL COMMENT '期望入职日期',
    reason TEXT NOT NULL COMMENT '招聘原因',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_requirement_no (requirement_no),
    INDEX idx_org_id (org_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_position_id (position_id),
    INDEX idx_requirement_status (requirement_status),
    INDEX idx_urgency_level (urgency_level),
    INDEX idx_expected_entry_date (expected_entry_date),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (org_id) REFERENCES hr_organization(id),
    FOREIGN KEY (dept_id) REFERENCES hr_department(id),
    FOREIGN KEY (position_id) REFERENCES hr_position(id)
) COMMENT='招聘需求表';

-- 招聘职位表
CREATE TABLE hr_recruit_position (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    requirement_id BIGINT NOT NULL COMMENT '需求ID',
    position_name VARCHAR(100) NOT NULL COMMENT '职位名称',
    job_description TEXT COMMENT '职位描述',
    job_requirements TEXT COMMENT '职位要求',
    salary_min BIGINT COMMENT '最低薪资',
    salary_max BIGINT COMMENT '最高薪资',
    city VARCHAR(50) COMMENT '工作城市',
    employment_type VARCHAR(20) COMMENT '雇佣类型',
    publish_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_requirement_id (requirement_id),
    INDEX idx_position_name (position_name),
    INDEX idx_publish_status (publish_status),
    INDEX idx_employment_type (employment_type),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (requirement_id) REFERENCES hr_recruit_requirement(id) ON DELETE CASCADE
) COMMENT='招聘职位表';

-- 候选人表
CREATE TABLE hr_candidate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    candidate_no VARCHAR(50) NOT NULL UNIQUE COMMENT '候选人编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    resume_url VARCHAR(500) COMMENT '简历地址',
    source_channel VARCHAR(50) NOT NULL COMMENT '来源渠道',
    apply_position_id BIGINT COMMENT '申请职位ID',
    candidate_status VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT '候选人状态',
    current_company VARCHAR(100) COMMENT '当前公司',
    current_position VARCHAR(100) COMMENT '当前职位',
    expected_salary BIGINT COMMENT '期望薪资',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_candidate_no (candidate_no),
    INDEX idx_name (name),
    INDEX idx_mobile (mobile),
    INDEX idx_email (email),
    INDEX idx_source_channel (source_channel),
    INDEX idx_apply_position_id (apply_position_id),
    INDEX idx_candidate_status (candidate_status),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (apply_position_id) REFERENCES hr_recruit_position(id)
) COMMENT='候选人表';

-- 候选人面试表
CREATE TABLE hr_candidate_interview (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    interview_round INT NOT NULL COMMENT '面试轮次',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    interviewer_name VARCHAR(50) NOT NULL COMMENT '面试官姓名',
    interview_time DATETIME NOT NULL COMMENT '面试时间',
    interview_type VARCHAR(20) NOT NULL COMMENT '面试类型',
    score INT COMMENT '面试评分',
    result VARCHAR(20) COMMENT '面试结果',
    feedback TEXT COMMENT '面试反馈',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_candidate_id (candidate_id),
    INDEX idx_interviewer_id (interviewer_id),
    INDEX idx_interview_time (interview_time),
    INDEX idx_interview_type (interview_type),
    INDEX idx_result (result),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (candidate_id) REFERENCES hr_candidate(id) ON DELETE CASCADE
) COMMENT='候选人面试表';

-- 候选人Offer表
CREATE TABLE hr_candidate_offer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    offer_no VARCHAR(50) NOT NULL UNIQUE COMMENT 'Offer编号',
    position_id BIGINT NOT NULL COMMENT '职位ID',
    salary_amount BIGINT NOT NULL COMMENT '薪资金额',
    entry_date DATE NOT NULL COMMENT '入职日期',
    offer_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'Offer状态',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_candidate_id (candidate_id),
    INDEX idx_offer_no (offer_no),
    INDEX idx_position_id (position_id),
    INDEX idx_offer_status (offer_status),
    INDEX idx_entry_date (entry_date),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (candidate_id) REFERENCES hr_candidate(id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES hr_recruit_position(id)
) COMMENT='候选人Offer表';

-- 初始化招聘需求数据
INSERT INTO hr_recruit_requirement (requirement_no, title, org_id, dept_id, position_id, headcount, urgency_level, requirement_status, expected_entry_date, reason, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
('REQ202403180001', 'Java开发工程师招聘', 1, 1, 1, 2, 'HIGH', 'OPEN', '2024-04-01', '业务扩展需要增加Java开发人员', 'company', '急需有经验的Java开发人员', 1, NOW(), 1, NOW(), 0),
('REQ202403180002', '前端开发工程师招聘', 1, 1, 2, 1, 'MEDIUM', 'OPEN', '2024-04-15', '项目需要前端开发支持', 'company', '需要熟悉React的前端开发', 1, NOW(), 1, NOW(), 0),
('HREQ202403180001', '主治医师招聘', 2, 3, 5, 3, 'HIGH', 'OPEN', '2024-03-25', '科室人员不足，需要补充主治医师', 'hospital', '急需有经验的内科主治医师', 1, NOW(), 1, NOW(), 0);

-- 初始化招聘职位数据
INSERT INTO hr_recruit_position (requirement_id, position_name, job_description, job_requirements, salary_min, salary_max, city, employment_type, publish_status, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
(1, 'Java开发工程师', '负责Java后端系统开发和维护', '3年以上Java开发经验，熟悉Spring Boot框架', 15000, 25000, '北京', 'FULL_TIME', 'PUBLISHED', 'company', '有互联网公司经验优先', 1, NOW(), 1, NOW(), 0),
(2, '前端开发工程师', '负责前端页面开发和交互实现', '2年以上前端开发经验，熟悉React框架', 12000, 20000, '北京', 'FULL_TIME', 'PUBLISHED', 'company', '有移动端开发经验优先', 1, NOW(), 1, NOW(), 0),
(3, '主治医师', '负责内科诊疗工作', '5年以上临床工作经验，持有执业医师证', 20000, 35000, '上海', 'FULL_TIME', 'PUBLISHED', 'hospital', '有三级医院工作经验优先', 1, NOW(), 1, NOW(), 0);

-- 初始化候选人数據
INSERT INTO hr_candidate (candidate_no, name, gender, mobile, email, source_channel, apply_position_id, candidate_status, current_company, current_position, expected_salary, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
('CAN202403180001', '张三', 'MALE', '13800138001', 'zhangsan@email.com', 'ONLINE', 1, 'SCREENING', 'ABC科技公司', 'Java开发工程师', 18000, 'company', '有3年Java开发经验', 1, NOW(), 1, NOW(), 0),
('CAN202403180002', '李四', 'FEMALE', '13800138002', 'lisi@email.com', 'REFERRAL', 2, 'INTERVIEWING', 'XYZ软件公司', '前端开发工程师', 15000, 'company', '有2年前端开发经验', 1, NOW(), 1, NOW(), 0),
('CAN202403180003', '王五', 'MALE', '13800138003', 'wangwu@email.com', 'CAMPUS', 3, 'NEW', '医学院附属医院', '住院医师', 25000, 'hospital', '应届毕业生', 1, NOW(), 1, NOW(), 0);
