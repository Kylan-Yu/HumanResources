-- 检查组织管理相关表是否存在
USE hrms_db;

-- 检查表是否存在
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    CREATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'hrms_db' 
    AND TABLE_NAME IN ('hr_org', 'hr_dept', 'hr_position')
ORDER BY TABLE_NAME;

-- 检查表数据
SELECT 'hr_org' as table_name, COUNT(*) as record_count FROM hr_org
UNION ALL
SELECT 'hr_dept' as table_name, COUNT(*) as record_count FROM hr_dept
UNION ALL  
SELECT 'hr_position' as table_name, COUNT(*) as record_count FROM hr_position;
