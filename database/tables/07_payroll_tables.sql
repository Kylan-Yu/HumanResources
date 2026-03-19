-- 薪资管理相关表结构

-- 薪资标准表
CREATE TABLE hr_payroll_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    standard_name VARCHAR(100) NOT NULL COMMENT '标准名称',
    org_id BIGINT COMMENT '组织ID',
    dept_id BIGINT COMMENT '部门ID',
    position_id BIGINT COMMENT '岗位ID',
    grade_level VARCHAR(20) COMMENT '职级',
    base_salary DECIMAL(10,2) NOT NULL COMMENT '基本薪资',
    performance_salary DECIMAL(10,2) COMMENT '绩效薪资',
    position_allowance DECIMAL(10,2) COMMENT '岗位津贴',
    meal_allowance DECIMAL(10,2) COMMENT '餐补',
    transport_allowance DECIMAL(10,2) COMMENT '交通补贴',
    communication_allowance DECIMAL(10,2) COMMENT '通讯补贴',
    housing_allowance DECIMAL(10,2) COMMENT '住房补贴',
    other_allowance DECIMAL(10,2) COMMENT '其他补贴',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_standard_name (standard_name),
    INDEX idx_org_id (org_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_position_id (position_id),
    INDEX idx_grade_level (grade_level),
    INDEX idx_status (status),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (org_id) REFERENCES hr_organization(id),
    FOREIGN KEY (dept_id) REFERENCES hr_department(id),
    FOREIGN KEY (position_id) REFERENCES hr_position(id)
) COMMENT='薪资标准表';

-- 薪资记录表
CREATE TABLE hr_payroll_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    payroll_no VARCHAR(50) NOT NULL UNIQUE COMMENT '薪资单号',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    payroll_period VARCHAR(20) NOT NULL COMMENT '薪资期间',
    period_start_date DATE NOT NULL COMMENT '薪资期间开始日期',
    period_end_date DATE NOT NULL COMMENT '薪资期间结束日期',
    pay_date DATE NOT NULL COMMENT '发放日期',
    base_salary DECIMAL(10,2) NOT NULL COMMENT '基本薪资',
    performance_salary DECIMAL(10,2) COMMENT '绩效薪资',
    position_allowance DECIMAL(10,2) COMMENT '岗位津贴',
    meal_allowance DECIMAL(10,2) COMMENT '餐补',
    transport_allowance DECIMAL(10,2) COMMENT '交通补贴',
    communication_allowance DECIMAL(10,2) COMMENT '通讯补贴',
    housing_allowance DECIMAL(10,2) COMMENT '住房补贴',
    other_allowance DECIMAL(10,2) COMMENT '其他补贴',
    gross_salary DECIMAL(10,2) NOT NULL COMMENT '应发薪资',
    social_personal DECIMAL(10,2) COMMENT '社保个人',
    fund_personal DECIMAL(10,2) COMMENT '公积金个人',
    income_tax DECIMAL(10,2) COMMENT '个税',
    other_deduction DECIMAL(10,2) COMMENT '其他扣款',
    net_salary DECIMAL(10,2) NOT NULL COMMENT '实发薪资',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_payroll_no (payroll_no),
    INDEX idx_employee_id (employee_id),
    INDEX idx_payroll_period (payroll_period),
    INDEX idx_period_start_date (period_start_date),
    INDEX idx_period_end_date (period_end_date),
    INDEX idx_pay_date (pay_date),
    INDEX idx_status (status),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (employee_id) REFERENCES hr_employee(id)
) COMMENT='薪资记录表';

-- 社保公积金配置表
CREATE TABLE hr_social_fund_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    org_id BIGINT COMMENT '组织ID',
    social_base DECIMAL(10,2) COMMENT '社保基数',
    social_company_rate DECIMAL(5,4) COMMENT '社保公司比例',
    social_personal_rate DECIMAL(5,4) COMMENT '社保个人比例',
    fund_base DECIMAL(10,2) COMMENT '公积金基数',
    fund_company_rate DECIMAL(5,4) COMMENT '公积金公司比例',
    fund_personal_rate DECIMAL(5,4) COMMENT '公积金个人比例',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_config_name (config_name),
    INDEX idx_org_id (org_id),
    INDEX idx_status (status),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (org_id) REFERENCES hr_organization(id)
) COMMENT='社保公积金配置表';

-- 个税配置表
CREATE TABLE hr_tax_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    min_salary DECIMAL(10,2) NOT NULL COMMENT '薪资下限',
    max_salary DECIMAL(10,2) COMMENT '薪资上限',
    tax_rate DECIMAL(5,4) NOT NULL COMMENT '税率',
    quick_deduction DECIMAL(10,2) NOT NULL COMMENT '速算扣除数',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_min_salary (min_salary),
    INDEX idx_max_salary (max_salary),
    INDEX idx_tax_rate (tax_rate),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) COMMENT='个税配置表';

-- 初始化薪资标准数据
INSERT INTO hr_payroll_standard (standard_name, org_id, dept_id, position_id, grade_level, base_salary, performance_salary, position_allowance, meal_allowance, transport_allowance, communication_allowance, housing_allowance, other_allowance, status, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
('Java开发工程师薪资标准', 1, 1, 1, 'P3', 15000.00, 5000.00, 2000.00, 500.00, 800.00, 200.00, 1500.00, 0.00, 'ACTIVE', 'company', 'Java开发工程师标准薪资', 1, NOW(), 1, NOW(), 0),
('前端开发工程师薪资标准', 1, 1, 2, 'P2', 12000.00, 4000.00, 1500.00, 500.00, 600.00, 200.00, 1200.00, 0.00, 'ACTIVE', 'company', '前端开发工程师标准薪资', 1, NOW(), 1, NOW(), 0),
('主治医师薪资标准', 2, 3, 5, 'M3', 20000.00, 8000.00, 3000.00, 800.00, 1000.00, 300.00, 2000.00, 0.00, 'ACTIVE', 'hospital', '主治医师标准薪资', 1, NOW(), 1, NOW(), 0),
('护士薪资标准', 2, 4, 6, 'N2', 8000.00, 2000.00, 1000.00, 400.00, 500.00, 150.00, 800.00, 0.00, 'ACTIVE', 'hospital', '护士标准薪资', 1, NOW(), 1, NOW(), 0);

-- 初始化社保公积金配置数据
INSERT INTO hr_social_fund_config (config_name, org_id, social_base, social_company_rate, social_personal_rate, fund_base, fund_company_rate, fund_personal_rate, status, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
('企业社保公积金配置', 1, 8000.00, 0.1600, 0.0800, 5000.00, 0.1200, 0.1200, 'ACTIVE', 'company', '企业标准社保公积金配置', 1, NOW(), 1, NOW(), 0),
('医院社保公积金配置', 2, 10000.00, 0.1600, 0.0800, 6000.00, 0.1200, 0.1200, 'ACTIVE', 'hospital', '医院标准社保公积金配置', 1, NOW(), 1, NOW(), 0);

-- 初始化个税配置数据
INSERT INTO hr_tax_config (min_salary, max_salary, tax_rate, quick_deduction, status, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
(0.00, 3000.00, 0.0300, 0.00, 'ACTIVE', '3%税率', 1, NOW(), 1, NOW(), 0),
(3000.00, 12000.00, 0.1000, 210.00, 'ACTIVE', '10%税率', 1, NOW(), 1, NOW(), 0),
(12000.00, 25000.00, 0.2000, 1410.00, 'ACTIVE', '20%税率', 1, NOW(), 1, NOW(), 0),
(25000.00, 35000.00, 0.2500, 2660.00, 'ACTIVE', '25%税率', 1, NOW(), 1, NOW(), 0),
(35000.00, 55000.00, 0.3000, 4410.00, 'ACTIVE', '30%税率', 1, NOW(), 1, NOW(), 0),
(55000.00, 80000.00, 0.3500, 7160.00, 'ACTIVE', '35%税率', 1, NOW(), 1, NOW(), 0),
(80000.00, NULL, 0.4500, 15160.00, 'ACTIVE', '45%税率', 1, NOW(), 1, NOW(), 0);
