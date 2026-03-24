# HRMS 数据库连接测试
Write-Host "=== HRMS 数据库连接测试 ===" -ForegroundColor Green

# 检查MySQL客户端
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysqlPath) {
    Write-Host "找到MySQL客户端: $($mysqlPath.Source)" -ForegroundColor Green
} else {
    Write-Host "未找到MySQL客户端" -ForegroundColor Red
}

Write-Host ""
Write-Host "连接信息:" -ForegroundColor Cyan
Write-Host "- 主机: 192.168.15.100" -ForegroundColor White
Write-Host "- 端口: 3306" -ForegroundColor White
Write-Host "- 用户名: root" -ForegroundColor White
Write-Host "- 密码: shice2022mysql" -ForegroundColor White
Write-Host "- 数据库: hrms_db" -ForegroundColor White

Write-Host ""
Write-Host "测试命令:" -ForegroundColor Yellow
Write-Host "mysql -h 192.168.15.100 -u root -p" -ForegroundColor White
Write-Host "输入密码后执行: USE hrms_db; SHOW TABLES;" -ForegroundColor White

Write-Host ""
Write-Host "预期结果:" -ForegroundColor Green
Write-Host "- 11个表" -ForegroundColor White
Write-Host "- sys_user表有4条数据" -ForegroundColor White
