-- HRMS 数据库测试SQL脚本
-- 请在MySQL客户端中执行以下命令

-- 1. 连接数据库后，使用hrms_db数据库
USE hrms_db;

-- 2. 显示所有表
SHOW TABLES;

-- 3. 检查sys_user表结构
DESCRIBE sys_user;

-- 4. 检查用户数量
SELECT COUNT(*) as user_count FROM sys_user;

-- 5. 显示用户数据（前5条）
SELECT id, username, real_name, email, phone, status FROM sys_user LIMIT 5;

-- 6. 检查其他重要表的数据量
SELECT 'sys_role' as table_name, COUNT(*) as count FROM sys_role
UNION ALL
SELECT 'sys_menu', COUNT(*) FROM sys_menu
UNION ALL
SELECT 'sys_dict', COUNT(*) FROM sys_dict
UNION ALL
SELECT 'hr_org', COUNT(*) FROM hr_org
UNION ALL
SELECT 'hr_dept', COUNT(*) FROM hr_dept
UNION ALL
SELECT 'hr_position', COUNT(*) FROM hr_position
UNION ALL
SELECT 'hr_employee', COUNT(*) FROM hr_employee;

-- 7. 检查用户角色关联
SELECT ur.user_id, u.username, ur.role_id, r.role_name 
FROM sys_user_role ur
LEFT JOIN sys_user u ON ur.user_id = u.id
LEFT JOIN sys_role r ON ur.role_id = r.id
LIMIT 5;
