# 简单的数据库连接测试
Write-Host "=== HRMS 数据库连接测试 ===" -ForegroundColor Green

# 测试端口连接
Write-Host "1. 测试MySQL端口连接..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("192.168.15.100", 3306)
    $tcpClient.Close()
    Write-Host "✅ MySQL端口3306可达" -ForegroundColor Green
} catch {
    Write-Host "❌ MySQL端口3306不可达: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 测试Redis端口
Write-Host "2. 测试Redis端口连接..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("192.168.15.100", 6379)
    $tcpClient.Close()
    Write-Host "✅ Redis端口6379可达" -ForegroundColor Green
} catch {
    Write-Host "❌ Redis端口6379不可达: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试MinIO端口
Write-Host "3. 测试MinIO端口连接..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("192.168.15.100", 9000)
    $tcpClient.Close()
    Write-Host "✅ MinIO端口9000可达" -ForegroundColor Green
} catch {
    Write-Host "❌ MinIO端口9000不可达: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 端口测试完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "如果端口都可达，请手动在MySQL客户端中测试:" -ForegroundColor Yellow
Write-Host "1. 连接: mysql -h 192.168.15.100 -u root -p" -ForegroundColor Cyan
Write-Host "2. 密码: shice2022mysql" -ForegroundColor Cyan
Write-Host "3. 执行: USE hrms_db; SHOW TABLES; SELECT COUNT(*) FROM sys_user;" -ForegroundColor Cyan
Write-Host ""
