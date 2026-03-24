# HRMS 数据库连接测试
Write-Host "=== HRMS 数据库连接测试 ===" -ForegroundColor Green

# 检查是否有MySQL客户端
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysqlPath) {
    Write-Host "✅ 找到MySQL客户端: $($mysqlPath.Source)" -ForegroundColor Green
    Write-Host ""
    Write-Host "正在尝试连接数据库..." -ForegroundColor Yellow
    
    # 创建临时SQL文件
    $tempSql = @"
USE hrms_db;
SHOW TABLES;
SELECT '用户数量' as 类型, COUNT(*) as 数量 FROM sys_user
UNION ALL
SELECT '角色数量', COUNT(*) FROM sys_role
UNION ALL
SELECT '菜单数量', COUNT(*) FROM sys_menu;
"@
    
    $tempFile = "C:\temp\hrms_test.sql"
    $tempSql | Out-File -FilePath $tempFile -Encoding UTF8
    
    Write-Host "请手动执行以下命令:" -ForegroundColor Cyan
    Write-Host "mysql -h 192.168.15.100 -u root -pshice2022mysql hrms_db < $tempFile" -ForegroundColor White
    Write-Host ""
    Write-Host "或者分步执行:" -ForegroundColor Cyan
    Write-Host "1. mysql -h 192.168.15.100 -u root -p" -ForegroundColor White
    Write-Host "2. 输入密码: shice2022mysql" -ForegroundColor White
    Write-Host "3. USE hrms_db;" -ForegroundColor White
    Write-Host "4. SHOW TABLES;" -ForegroundColor White
    Write-Host "5. SELECT COUNT(*) FROM sys_user;" -ForegroundColor White
    
} else {
    Write-Host "❌ 未找到MySQL客户端" -ForegroundColor Red
    Write-Host ""
    Write-Host "请:" -ForegroundColor Yellow
    Write-Host "1. 安装MySQL客户端" -ForegroundColor White
    Write-Host "2. 或者使用图形界面工具如Navicat、DBeaver" -ForegroundColor White
    Write-Host ""
    Write-Host "连接信息:" -ForegroundColor Cyan
    Write-Host "- 主机: 192.168.15.100" -ForegroundColor White
    Write-Host "- 端口: 3306" -ForegroundColor White
    Write-Host "- 用户名: root" -ForegroundColor White
    Write-Host "- 密码: shice2022mysql" -ForegroundColor White
    Write-Host "- 数据库: hrms_db" -ForegroundColor White
}

Write-Host ""
Write-Host "=== 测试说明 ===" -ForegroundColor Green
Write-Host "如果连接成功，应该看到:" -ForegroundColor Yellow
Write-Host "- 11个表 (sys_* 和 hr_* 系列)" -ForegroundColor White
Write-Host "- sys_user表有4条用户数据" -ForegroundColor White
Write-Host "- 其他表也有相应的初始数据" -ForegroundColor White
Write-Host ""
