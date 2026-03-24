@echo off
echo === HRMS 数据库连接测试 ===
echo.

echo 1. 测试MySQL端口连接...
telnet 192.168.15.100 3306

echo.
echo 2. 如果上面显示黑屏或连接成功，说明端口可达
echo    如果显示"无法连接"，说明网络不通
echo.

echo 3. 请手动在MySQL客户端中执行以下命令测试：
echo    mysql -h 192.168.15.100 -u root -p
echo    输入密码: shice2022mysql
echo.
echo    然后执行:
echo    USE hrms_db;
echo    SHOW TABLES;
echo    SELECT COUNT(*) FROM sys_user;
echo.

pause
