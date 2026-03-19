-- 初始化基础数据

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
(1, '启用', '1', 'sys_status', '', 'primary', 'Y', 1, '启用状态', 1, NOW()),
(2, '禁用', '0', 'sys_status', '', 'danger', 'N', 1, '禁用状态', 1, NOW()),
-- 性别
(1, '男', '1', 'gender', '', 'primary', 'N', 1, '男性', 1, NOW()),
(2, '女', '2', 'gender', '', 'success', 'N', 1, '女性', 1, NOW()),
-- 婚姻状况
(1, '未婚', 'single', 'marital_status', '', 'info', 'N', 1, '未婚', 1, NOW()),
(2, '已婚', 'married', 'marital_status', '', 'success', 'N', 1, '已婚', 1, NOW()),
(3, '离异', 'divorced', 'marital_status', '', 'warning', 'N', 1, '离异', 1, NOW()),
(4, '丧偶', 'widowed', 'marital_status', '', 'danger', 'N', 1, '丧偶', 1, NOW()),
-- 政治面貌
(1, '党员', 'member', 'political_status', '', 'danger', 'N', 1, '中共党员', 1, NOW()),
(2, '团员', 'league', 'political_status', '', 'primary', 'N', 1, '共青团员', 1, NOW()),
(3, '群众', 'mass', 'political_status', '', 'default', 'Y', 1, '群众', 1, NOW()),
-- 证件类型
(1, '身份证', 'id_card', 'id_card_type', '', 'primary', 'Y', 1, '身份证', 1, NOW()),
(2, '护照', 'passport', 'id_card_type', '', 'info', 'N', 1, '护照', 1, NOW()),
(3, '其他', 'other', 'id_card_type', '', 'default', 'N', 1, '其他证件', 1, NOW()),
-- 员工状态
(1, '待入职', 'pending', 'employee_status', '', 'warning', 'N', 1, '待入职', 1, NOW()),
(2, '试用期', 'probation', 'employee_status', '', 'processing', 'N', 1, '试用期', 1, NOW()),
(3, '正式', 'regular', 'employee_status', '', 'success', 'Y', 1, '正式员工', 1, NOW()),
(4, '调动', 'transfer', 'employee_status', '', 'primary', 'N', 1, '调动中', 1, NOW()),
(5, '停薪留职', 'leave_absence', 'employee_status', '', 'default', 'N', 1, '停薪留职', 1, NOW()),
(6, '离职', 'resign', 'employee_status', '', 'danger', 'N', 1, '已离职', 1, NOW()),
(7, '退休', 'retire', 'employee_status', '', 'info', 'N', 1, '已退休', 1, NOW()),
-- 学历层次
(1, '高中', 'high_school', 'education_level', '', 'default', 'N', 1, '高中', 1, NOW()),
(2, '大专', 'junior_college', 'education_level', '', 'info', 'N', 1, '大专', 1, NOW()),
(3, '本科', 'bachelor', 'education_level', '', 'primary', 'Y', 1, '本科', 1, NOW()),
(4, '硕士', 'master', 'education_level', '', 'success', 'N', 1, '硕士', 1, NOW()),
(5, '博士', 'doctor', 'education_level', '', 'danger', 'N', 1, '博士', 1, NOW()),
-- 学位
(1, '专科', 'associate', 'degree', '', 'info', 'N', 1, '专科', 1, NOW()),
(2, '学士', 'bachelor', 'degree', '', 'primary', 'Y', 1, '学士', 1, NOW()),
(3, '硕士', 'master', 'degree', '', 'success', 'N', 1, '硕士', 1, NOW()),
(4, '博士', 'doctor', 'degree', '', 'danger', 'N', 1, '博士', 1, NOW()),
-- 组织类型
(1, '公司', 'company', 'org_type', '', 'primary', 'Y', 1, '公司', 1, NOW()),
(2, '医院', 'hospital', 'org_type', '', 'success', 'N', 1, '医院', 1, NOW()),
(3, '分公司', 'branch', 'org_type', '', 'info', 'N', 1, '分公司', 1, NOW()),
(4, '院区', 'campus', 'org_type', '', 'warning', 'N', 1, '院区', 1, NOW()),
-- 部门类型
(1, '部门', 'dept', 'dept_type', '', 'primary', 'Y', 1, '普通部门', 1, NOW()),
(2, '办公室', 'office', 'dept_type', '', 'info', 'N', 1, '办公室', 1, NOW()),
(3, '病区', 'ward', 'dept_type', '', 'success', 'N', 1, '病区', 1, NOW()),
(4, '门诊', 'clinic', 'dept_type', '', 'warning', 'N', 1, '门诊', 1, NOW()),
(5, '急诊', 'emergency', 'dept_type', '', 'danger', 'N', 1, '急诊', 1, NOW()),
(6, '医技', 'medical', 'dept_type', '', 'processing', 'N', 1, '医技科室', 1, NOW()),
(7, '护理', 'nursing', 'dept_type', '', 'cyan', 'N', 1, '护理部门', 1, NOW()),
(8, '行政后勤', 'admin', 'dept_type', '', 'default', 'N', 1, '行政后勤', 1, NOW()),
-- 岗位类别
(1, '管理', 'management', 'position_category', '', 'danger', 'N', 1, '管理岗位', 1, NOW()),
(2, '技术', 'technical', 'position_category', '', 'purple', 'N', 1, '技术岗位', 1, NOW()),
(3, '医疗', 'medical', 'position_category', '', 'success', 'N', 1, '医疗岗位', 1, NOW()),
(4, '护理', 'nursing', 'position_category', '', 'cyan', 'N', 1, '护理岗位', 1, NOW()),
(5, '药剂', 'pharmacy', 'position_category', '', 'warning', 'N', 1, '药剂岗位', 1, NOW()),
(6, '检验', 'laboratory', 'position_category', '', 'orange', 'N', 1, '检验岗位', 1, NOW()),
(7, '行政', 'admin', 'position_category', '', 'default', 'Y', 1, '行政岗位', 1, NOW()),
(8, '财务', 'finance', 'position_category', '', 'gold', 'N', 1, '财务岗位', 1, NOW()),
(9, '人事', 'hr', 'position_category', '', 'lime', 'N', 1, '人事岗位', 1, NOW()),
(10, '其他', 'other', 'position_category', '', 'default', 'N', 1, '其他岗位', 1, NOW()),
-- 职等
(1, '专业序列', 'P', 'rank_grade', '', 'primary', 'N', 1, '专业序列', 1, NOW()),
(2, '管理序列', 'M', 'rank_grade', '', 'danger', 'N', 1, '管理序列', 1, NOW()),
(3, '技术序列', 'T', 'rank_grade', '', 'purple', 'N', 1, '技术序列', 1, NOW()),
-- 职级序列
(1, '管理', 'management', 'rank_series', '', 'danger', 'N', 1, '管理序列', 1, NOW()),
(2, '专业', 'professional', 'rank_series', '', 'primary', 'N', 1, '专业序列', 1, NOW()),
(3, '技术', 'technical', 'rank_series', '', 'purple', 'N', 1, '技术序列', 1, NOW()),
(4, '医疗', 'medical', 'rank_series', '', 'success', 'N', 1, '医疗序列', 1, NOW()),
(5, '护理', 'nursing', 'rank_series', '', 'cyan', 'N', 1, '护理序列', 1, NOW()),
-- 异动类型
(1, '入职', 'hire', 'change_type', '', 'success', 'N', 1, '入职', 1, NOW()),
(2, '调岗', 'transfer', 'change_type', '', 'primary', 'N', 1, '调岗', 1, NOW()),
(3, '晋升', 'promotion', 'change_type', '', 'success', 'N', 1, '晋升', 1, NOW()),
(4, '降职', 'demotion', 'change_type', '', 'warning', 'N', 1, '降职', 1, NOW()),
(5, '调薪', 'salary_adjust', 'change_type', '', 'gold', 'N', 1, '调薪', 1, NOW()),
(6, '离职', 'resign', 'change_type', '', 'danger', 'N', 1, '离职', 1, NOW()),
(7, '退休', 'retire', 'change_type', '', 'info', 'N', 1, '退休', 1, NOW()),
-- 审批状态
(1, '待审批', 'pending', 'approval_status', '', 'warning', 'N', 1, '待审批', 1, NOW()),
(2, '已审批', 'approved', 'approval_status', '', 'success', 'Y', 1, '已审批', 1, NOW()),
(3, '已拒绝', 'rejected', 'approval_status', '', 'danger', 'N', 1, '已拒绝', 1, NOW());

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
(0, '组织管理', 1, '/org', NULL, NULL, 'Apartment', 2, 1, 1, 1, NOW()),
(200, '组织架构', 2, '/org/tree', 'org/Tree', 'org:tree:list', 'Apartment', 1, 1, 1, 1, NOW()),
(200, '部门管理', 2, '/org/dept', 'org/Dept', 'org:dept:list', 'Team', 2, 1, 1, 1, NOW()),
(200, '岗位管理', 2, '/org/position', 'org/Position', 'org:position:list', 'Idcard', 3, 1, 1, 1, NOW()),
(200, '职级管理', 2, '/org/rank', 'org/Rank', 'org:rank:list', 'Trophy', 4, 1, 1, 1, NOW()),
(0, '员工管理', 1, '/employee', NULL, NULL, 'User', 3, 1, 1, 1, NOW()),
(300, '员工档案', 2, '/employee/list', 'employee/List', 'employee:list', 'User', 1, 1, 1, 1, NOW()),
(300, '员工异动', 2, '/employee/change', 'employee/Change', 'employee:change:list', 'Swap', 2, 1, 1, 1, NOW()),
(0, '工作台', 1, '/dashboard', NULL, NULL, 'Dashboard', 1, 1, 1, 1, NOW()),
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
