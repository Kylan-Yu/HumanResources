-- 人力资源管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS hrms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hrms_db;

-- 设置时区
SET time_zone = '+08:00';

-- 创建用户和授权（可选）
-- CREATE USER 'hrms_user'@'%' IDENTIFIED BY 'hrms_password';
-- GRANT ALL PRIVILEGES ON hrms_db.* TO 'hrms_user'@'%';
-- FLUSH PRIVILEGES;

-- 执行建表脚本
SOURCE tables/01_sys_tables.sql;
SOURCE tables/02_org_tables.sql;
SOURCE tables/03_employee_tables.sql;
SOURCE tables/04_recruit_tables.sql;
SOURCE tables/05_attendance_tables.sql;
SOURCE tables/06_schedule_tables.sql;
SOURCE tables/07_payroll_tables.sql;
SOURCE tables/08_performance_tables.sql;
SOURCE tables/09_contract_tables.sql;
SOURCE tables/10_training_tables.sql;
SOURCE tables/11_certificate_tables.sql;
SOURCE tables/12_workflow_tables.sql;
SOURCE tables/13_message_tables.sql;

-- 执行初始化数据脚本
SOURCE data/init_data.sql;
