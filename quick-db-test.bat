@echo off
echo === HRMS 快速数据库测试 ===
echo.

echo 正在测试数据库连接...
echo.

echo 请确保你已经:
echo 1. 安装了MySQL客户端
echo 2. 执行了 hrms_full_complete.sql 初始化脚本
echo.

echo 执行以下命令测试:
echo.
echo mysql -h 192.168.15.100 -u root -p
echo.
echo 然后输入密码: shice2022mysql
echo.
echo 连接成功后，复制粘贴以下SQL命令:
echo USE hrms_db;
echo SHOW TABLES;
echo SELECT COUNT(*) FROM sys_user;
echo.

echo 如果看到用户数量大于0，说明数据库正常
echo.

pause
