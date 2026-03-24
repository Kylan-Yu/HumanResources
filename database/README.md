# HRMS 数据库说明

## 📁 文件结构

```
database/
├── hrms_full_complete.sql    # 完整的数据库初始化脚本（整合所有SQL文件）
└── README.md                 # 本说明文档
```

## 🎯 表名规范

### 系统管理表（sys_前缀）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 用户表 | id, username, phone, real_name |
| sys_role | 角色表 | id, role_code, role_name |
| sys_menu | 菜单表 | id, menu_name, menu_type |
| sys_dict | 字典表 | id, dict_name, dict_type |
| sys_dict_item | 字典项表 | id, dict_label, dict_value |
| sys_user_role | 用户角色关联表 | user_id, role_id |
| sys_role_menu | 角色菜单关联表 | role_id, menu_id |

### 业务管理表（hr_前缀）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| hr_org | 组织表 | id, org_code, org_name |
| hr_dept | 部门表 | id, dept_code, dept_name |
| hr_position | 岗位表 | id, position_code, position_name |
| hr_employee | 员工表 | id, employee_code, real_name |

## 📊 数据库信息

- **数据库名**: `hrms_db`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: `InnoDB`

## 🔧 字段映射

### sys_user 表关键字段

| Java实体字段 | 数据库字段 | 说明 |
|-------------|-----------|------|
| id | id | 主键ID |
| username | username | 用户名 |
| password | password | 密码 |
| realName | real_name | 真实姓名 |
| mobile | phone | 手机号 ⚠️ 注意映射 |
| email | email | 邮箱 |
| status | status | 状态 |
| lastLoginTime | last_login_time | 最后登录时间 |
| lastLoginIp | last_login_ip | 最后登录IP |
| createdBy | created_by | 创建人 |
| createdTime | created_time | 创建时间 |
| updatedBy | updated_by | 更新人 |
| updatedTime | updated_time | 更新时间 |
| deleted | deleted | 删除标记 |

## 🚀 使用方法

### 1. 初始化数据库

```bash
# 连接到MySQL服务器
mysql -h 192.168.15.100 -u root -p

# 执行初始化脚本
source /path/to/hrms_full_complete.sql
```

### 2. 验证安装

```sql
-- 检查数据库
USE hrms_db;
SHOW TABLES;

-- 检查用户数据
SELECT COUNT(*) FROM sys_user;

-- 检查角色数据
SELECT COUNT(*) FROM sys_role;
```

## 📝 初始数据

### 默认用户

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 超级管理员 | 系统管理员 |
| hr_manager | admin123 | HR经理 | 人力资源经理 |
| manager | admin123 | 部门经理 | 部门经理 |
| employee | admin123 | 普通员工 | 普通员工 |

> **注意**: 所有默认用户的密码都是 `admin123`（BCrypt加密）

## 🔍 重要说明

1. **表名统一**: 所有表都使用 `sys_` 前缀
2. **字段映射**: 手机号字段在数据库中是 `phone`，Java实体中是 `mobile`
3. **逻辑删除**: 使用 `deleted` 字段进行逻辑删除（0=未删除，1=已删除）
4. **审计字段**: 包含 `created_by`, `created_time`, `updated_by`, `updated_time`
5. **状态字段**: 使用 `status` 字段控制启用/禁用状态

## 🛠️ 维护

### 清理数据库

```sql
-- 删除数据库（谨慎操作）
DROP DATABASE IF EXISTS hrms_db;

-- 重新创建
source hrms_complete.sql;
```

### 备份数据库

```bash
mysqldump -h 192.168.15.100 -u root -p hrms_db > hrms_db_backup.sql
```

## 📞 技术支持

如有问题，请检查：
1. MySQL服务是否正常运行
2. 网络连接是否正常
3. 用户权限是否足够
4. 表名和字段映射是否正确
