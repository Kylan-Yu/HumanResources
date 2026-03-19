-- 系统管理相关表结构

-- 用户表
CREATE TABLE hr_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    mobile VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_username (username),
    INDEX idx_mobile (mobile),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) COMMENT='用户表';

-- 角色表
CREATE TABLE hr_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) COMMENT='角色表';

-- 菜单表
CREATE TABLE hr_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) COMMENT '菜单编码',
    menu_type TINYINT NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
    icon VARCHAR(100) COMMENT '菜单图标',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(200) COMMENT '组件路径',
    permission VARCHAR(100) COMMENT '权限标识',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_external TINYINT DEFAULT 0 COMMENT '是否外链：0-否，1-是',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_code (menu_code),
    INDEX idx_menu_type (menu_type),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE hr_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES hr_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES hr_role(id) ON DELETE CASCADE
) COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE hr_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES hr_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES hr_menu(id) ON DELETE CASCADE
) COMMENT='角色菜单关联表';

-- 数据字典表
CREATE TABLE hr_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
    description VARCHAR(200) COMMENT '字典描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_dict_code (dict_code),
    INDEX idx_status (status)
) COMMENT='数据字典表';

-- 数据字典项表
CREATE TABLE hr_dict_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    item_name VARCHAR(100) NOT NULL COMMENT '字典项名称',
    item_code VARCHAR(50) NOT NULL COMMENT '字典项编码',
    item_value VARCHAR(100) COMMENT '字典项值',
    description VARCHAR(200) COMMENT '字典项描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_dict_id (dict_id),
    INDEX idx_item_code (item_code),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (dict_id) REFERENCES hr_dict(id) ON DELETE CASCADE
) COMMENT='数据字典项表';

-- 初始化用户数据
INSERT INTO hr_user (username, password, real_name, mobile, email, status, created_by, created_time, updated_by, updated_time, deleted) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138000', 'admin@hrms.com', 1, 1, NOW(), 1, NOW(), 0),
('hr_manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'HR经理', '13800138001', 'hr@hrms.com', 1, 1, NOW(), 1, NOW(), 0),
('manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '部门经理', '13800138002', 'manager@hrms.com', 1, 1, NOW(), 1, NOW(), 0),
('employee', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '普通员工', '13800138003', 'employee@hrms.com', 1, 1, NOW(), 1, NOW(), 0);

-- 初始化角色数据
INSERT INTO hr_role (role_name, role_code, description, status, sort_order, created_by, created_time, updated_by, updated_time, deleted) VALUES
('超级管理员', 'admin', '系统超级管理员，拥有所有权限', 1, 1, 1, NOW(), 1, NOW(), 0),
('HR经理', 'hr_manager', 'HR经理，拥有HR相关权限', 1, 2, 1, NOW(), 1, NOW(), 0),
('部门经理', 'manager', '部门经理，拥有本部门相关权限', 1, 3, 1, NOW(), 1, NOW(), 0),
('普通员工', 'employee', '普通员工，拥有基础权限', 1, 4, 1, NOW(), 1, NOW(), 0);

-- 初始化用户角色关联数据
INSERT INTO hr_user_role (user_id, role_id, created_by, created_time) VALUES
(1, 1, 1, NOW()),
(2, 2, 1, NOW()),
(3, 3, 1, NOW()),
(4, 4, 1, NOW());

-- 初始化菜单数据
INSERT INTO hr_menu (parent_id, menu_name, menu_code, menu_type, icon, path, component, permission, sort_order, status, is_external, created_by, created_time, updated_by, updated_time, deleted) VALUES
(0, '系统管理', 'system', 1, 'SettingOutlined', '/system', '', 'system:*', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(1, '用户管理', 'user', 2, 'UserOutlined', '/system/user', 'system/user/index', 'system:user:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(1, '角色管理', 'role', 2, 'TeamOutlined', '/system/role', 'system/role/index', 'system:role:list', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(1, '菜单管理', 'menu', 2, 'MenuOutlined', '/system/menu', 'system/menu/index', 'system:menu:list', 3, 1, 0, 1, NOW(), 1, NOW(), 0),
(0, '组织管理', 'org', 1, 'ApartmentOutlined', '/org', '', 'org:*', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(5, '组织架构', 'organization', 2, 'NodeIndexOutlined', '/org/organization', 'org/organization/index', 'org:organization:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(5, '部门管理', 'department', 2, 'ApartmentOutlined', '/org/department', 'org/department/index', 'org:department:list', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(5, '岗位管理', 'position', 2, 'UserAddOutlined', '/org/position', 'org/position/index', 'org:position:list', 3, 1, 0, 1, NOW(), 1, NOW(), 0),
(0, '员工管理', 'employee', 1, 'UserOutlined', '/employee', '', 'employee:*', 3, 1, 0, 1, NOW(), 1, NOW(), 0),
(9, '员工列表', 'employee_list', 2, 'UserOutlined', '/employee/list', 'employee/list/index', 'employee:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(9, '员工档案', 'employee_profile', 2, 'FileTextOutlined', '/employee/profile', 'employee/profile/index', 'employee:profile', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(0, '合同管理', 'contract', 1, 'FileTextOutlined', '/contract', '', 'contract:*', 4, 1, 0, 1, NOW(), 1, NOW(), 0),
(12, '合同列表', 'contract_list', 2, 'FileTextOutlined', '/contract/list', 'contract/list/index', 'contract:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(12, '到期预警', 'contract_warning', 2, 'ExclamationCircleOutlined', '/contract/warning', 'contract/warning/index', 'contract:warning', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(0, '招聘管理', 'recruit', 1, 'TeamOutlined', '/recruit', '', 'recruit:*', 5, 1, 0, 1, NOW(), 1, NOW(), 0),
(15, '招聘需求', 'recruit_requirement', 2, 'FileTextOutlined', '/recruit/requirement', 'recruit/requirement/list/index', 'recruit:requirement:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(15, '候选人管理', 'recruit_candidate', 2, 'UserAddOutlined', '/recruit/candidate', 'recruit/candidate/list/index', 'recruit:candidate:list', 2, 1, 0, 1, NOW(), 1, NOW(), 0),
(0, '薪资管理', 'payroll', 1, 'DollarOutlined', '/payroll', '', 'payroll:*', 6, 1, 0, 1, NOW(), 1, NOW(), 0),
(18, '薪资标准', 'payroll_standard', 2, 'DollarOutlined', '/payroll/standard', 'payroll/standard/list/index', 'payroll:standard:list', 1, 1, 0, 1, NOW(), 1, NOW(), 0),
(18, '薪资记录', 'payroll_record', 2, 'FileTextOutlined', '/payroll/record', 'payroll/record/list/index', 'payroll:record:list', 2, 1, 0, 1, NOW(), 1, NOW(), 0);

-- 初始化角色菜单关联数据（超级管理员拥有所有权限）
INSERT INTO hr_role_menu (role_id, menu_id, created_by, created_time)
SELECT 1, id, 1, NOW() FROM hr_menu WHERE deleted = 0;

-- HR经理权限
INSERT INTO hr_role_menu (role_id, menu_id, created_by, created_time) VALUES
(2, 9, 1, NOW()), (2, 10, 1, NOW()), (2, 11, 1, NOW()),
(2, 12, 1, NOW()), (2, 13, 1, NOW()), (2, 14, 1, NOW()),
(2, 15, 1, NOW()), (2, 16, 1, NOW()), (2, 17, 1, NOW()),
(2, 18, 1, NOW()), (2, 19, 1, NOW()), (2, 20, 1, NOW());

-- 部门经理权限
INSERT INTO hr_role_menu (role_id, menu_id, created_by, created_time) VALUES
(3, 9, 1, NOW()), (3, 10, 1, NOW()), (3, 11, 1, NOW()),
(3, 12, 1, NOW()), (3, 13, 1, NOW()), (3, 14, 1, NOW());

-- 普通员工权限
INSERT INTO hr_role_menu (role_id, menu_id, created_by, created_time) VALUES
(4, 9, 1, NOW()), (4, 10, 1, NOW()),
(4, 12, 1, NOW()), (4, 13, 1, NOW()),
(4, 18, 1, NOW()), (4, 19, 1, NOW());

-- 初始化数据字典数据
INSERT INTO hr_dict (dict_name, dict_code, description, status, created_by, created_time, updated_by, updated_time, deleted) VALUES
('性别', 'gender', '性别字典', 1, 1, NOW(), 1, NOW(), 0),
('状态', 'status', '通用状态字典', 1, 1, NOW(), 1, NOW(), 0),
('行业类型', 'industry_type', '行业类型字典', 1, 1, NOW(), 1, NOW(), 0),
('合同状态', 'contract_status', '合同状态字典', 1, 1, NOW(), 1, NOW(), 0),
('合同类型', 'contract_type', '合同类型字典', 1, 1, NOW(), 1, NOW(), 0);

-- 初始化数据字典项数据
INSERT INTO hr_dict_item (dict_id, item_name, item_code, item_value, description, sort_order, status, created_by, created_time, updated_by, updated_time, deleted) VALUES
-- 性别字典项
(1, '男', 'MALE', '1', '男性', 1, 1, 1, NOW(), 1, NOW(), 0),
(1, '女', 'FEMALE', '2', '女性', 2, 1, 1, NOW(), 1, NOW(), 0),
-- 状态字典项
(2, '启用', 'ENABLE', '1', '启用状态', 1, 1, 1, NOW(), 1, NOW(), 0),
(2, '禁用', 'DISABLE', '0', '禁用状态', 2, 1, 1, NOW(), 1, NOW(), 0),
-- 行业类型字典项
(3, '企业', 'company', 'company', '企业类型', 1, 1, 1, NOW(), 1, NOW(), 0),
(3, '医院', 'hospital', 'hospital', '医院类型', 2, 1, 1, NOW(), 1, NOW(), 0),
-- 合同状态字典项
(4, '草稿', 'DRAFT', 'DRAFT', '草稿状态', 1, 1, 1, NOW(), 1, NOW(), 0),
(4, '生效', 'ACTIVE', 'ACTIVE', '生效状态', 2, 1, 1, NOW(), 1, NOW(), 0),
(4, '到期', 'EXPIRED', 'EXPIRED', '到期状态', 3, 1, 1, NOW(), 1, NOW(), 0),
(4, '终止', 'TERMINATED', 'TERMINATED', '终止状态', 4, 1, 1, NOW(), 1, NOW(), 0),
-- 合同类型字典项
(5, '劳动合同', 'LABOR_CONTRACT', '1', '标准劳动合同', 1, 1, 1, NOW(), 1, NOW(), 0),
(5, '保密协议', 'CONFIDENTIALITY_AGREEMENT', '2', '保密协议', 2, 1, 1, NOW(), 1, NOW(), 0),
(5, '竞业限制协议', 'NON_COMPETE_AGREEMENT', '3', '竞业限制协议', 3, 1, 1, NOW(), 1, NOW(), 0);
