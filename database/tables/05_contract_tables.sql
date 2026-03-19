-- 合同管理相关表结构

-- 合同表
CREATE TABLE hr_contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    contract_no VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
    contract_type VARCHAR(50) NOT NULL COMMENT '合同类型',
    contract_subject VARCHAR(200) NOT NULL COMMENT '合同主体',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    sign_date DATE NOT NULL COMMENT '签署日期',
    contract_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '合同状态',
    renew_count INT DEFAULT 0 COMMENT '续签次数',
    industry_type VARCHAR(20) NOT NULL COMMENT '行业类型',
    ext_json TEXT COMMENT '扩展字段',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_employee_id (employee_id),
    INDEX idx_contract_no (contract_no),
    INDEX idx_contract_status (contract_status),
    INDEX idx_end_date (end_date),
    INDEX idx_industry_type (industry_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (employee_id) REFERENCES hr_employee(id)
) COMMENT='合同表';

-- 合同记录表
CREATE TABLE hr_contract_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    record_type VARCHAR(20) NOT NULL COMMENT '记录类型',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    change_reason VARCHAR(500) COMMENT '变更原因',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_record_type (record_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (contract_id) REFERENCES hr_contract(id) ON DELETE CASCADE
) COMMENT='合同记录表';

-- 初始化合同数据
INSERT INTO hr_contract (employee_id, contract_no, contract_type, contract_subject, start_date, end_date, sign_date, contract_status, renew_count, industry_type, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
(1, 'CT202403180001', 'LABOR_CONTRACT', '劳动合同', '2024-01-01', '2024-12-31', '2024-01-01', 'ACTIVE', 0, 'company', '标准劳动合同', 1, NOW(), 1, NOW(), 0),
(2, 'CT202403180002', 'LABOR_CONTRACT', '劳动合同', '2024-02-01', '2025-01-31', '2024-02-01', 'ACTIVE', 0, 'company', '技术岗位劳动合同', 1, NOW(), 1, NOW(), 0),
(3, 'HT202403180001', 'LABOR_CONTRACT', '劳动合同', '2023-06-01', '2024-05-31', '2023-06-01', 'ACTIVE', 0, 'hospital', '医师劳动合同', 1, NOW(), 1, NOW(), 0),
(4, 'HT202403180002', 'REEMPLOYMENT_AGREEMENT', '返聘协议', '2024-01-15', '2024-12-15', '2024-01-15', 'ACTIVE', 0, 'hospital', '退休医师返聘协议', 1, NOW(), 1, NOW(), 0);

-- 初始化合同记录数据
INSERT INTO hr_contract_record (contract_id, record_type, old_value, new_value, change_reason, operator_id, operator_name, created_time) VALUES
(1, 'CREATE', NULL, NULL, '创建合同', 1, '系统管理员', NOW()),
(2, 'CREATE', NULL, NULL, '创建合同', 1, '系统管理员', NOW()),
(3, 'CREATE', NULL, NULL, '创建合同', 1, '系统管理员', NOW()),
(4, 'CREATE', NULL, NULL, '创建合同', 1, '系统管理员', NOW());
