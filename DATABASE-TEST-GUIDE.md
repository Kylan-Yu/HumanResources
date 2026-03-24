# HRMS 数据库连接测试指南

## 🎯 测试步骤

### 1. 网络连接测试 ✅
所有端口都已测试可达：
- ✅ MySQL (192.168.15.100:3306)
- ✅ Redis (192.168.15.100:6379)  
- ✅ MinIO (192.168.15.100:9000)

### 2. MySQL数据库连接测试

#### 方法一：使用MySQL命令行客户端
```bash
# 连接MySQL服务器
mysql -h 192.168.15.100 -u root -p

# 输入密码：shice2022mysql
```

#### 方法二：使用图形界面工具
- **工具**: Navicat、DBeaver、MySQL Workbench
- **主机**: 192.168.15.100
- **端口**: 3306
- **用户名**: root
- **密码**: shice2022mysql
- **数据库**: hrms_db

### 3. 数据库验证SQL

连接成功后，请执行以下SQL验证：

```sql
-- 使用数据库
USE hrms_db;

-- 查看所有表
SHOW TABLES;

-- 检查用户表
SELECT COUNT(*) FROM sys_user;

-- 查看用户数据
SELECT id, username, real_name, email, phone, status FROM sys_user LIMIT 5;
```

### 4. 可能的问题和解决方案

#### 问题1：连接被拒绝
- **原因**: MySQL服务未启动或防火墙阻止
- **解决**: 检查MySQL服务状态，开放3306端口

#### 问题2：认证失败
- **原因**: 用户名或密码错误
- **解决**: 确认用户名root，密码shice2022mysql

#### 问题3：数据库不存在
- **原因**: hrms_db数据库未创建
- **解决**: 执行完整的初始化脚本 `hrms_full_complete.sql`

#### 问题4：表不存在
- **原因**: 表未创建或表名不匹配
- **解决**: 检查表名是否为sys_user（不是hr_user）

### 5. 预期结果

如果一切正常，应该看到：
- 11个表（sys_* 和 hr_* 系列）
- sys_user表有4条初始用户数据
- 其他表也有相应的初始数据

### 6. 测试文件

项目中的测试文件：
- `test-db-connection-simple.ps1` - 端口连接测试
- `test-mysql-commands.sql` - SQL测试脚本
- `DATABASE-TEST-GUIDE.md` - 本测试指南

### 7. 故障排除

如果连接失败，请检查：
1. 网络是否可达（已测试✅）
2. MySQL服务是否运行
3. 用户权限是否正确
4. 数据库是否已初始化

## 📞 下一步

完成数据库测试后，请告诉我：
1. 连接是否成功
2. 能看到多少个表
3. sys_user表有多少条数据

这样我可以进一步诊断后端API的问题。
