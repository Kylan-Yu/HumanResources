# 使用MySQL客户端测试连接
Write-Host "=== MySQL连接测试 ===" -ForegroundColor Green

# 方法1: 检查MySQL端口是否可达
try {
    $portTest = Test-NetConnection -ComputerName 192.168.15.100 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($portTest) {
        Write-Host "✅ MySQL端口3306可达" -ForegroundColor Green
    } else {
        Write-Host "❌ MySQL端口3306不可达" -ForegroundColor Red
        exit
    }
} catch {
    Write-Host "❌ 端口测试失败: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 方法2: 尝试使用telnet测试
Write-Host "尝试telnet连接测试..." -ForegroundColor Yellow

# 方法3: 检查是否有MySQL客户端
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
    "mysql.exe"
)

$mysqlFound = $false
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        Write-Host "找到MySQL客户端: $path" -ForegroundColor Green
        $mysqlFound = $true
        break
    }
}

if ($mysqlFound) {
    Write-Host "MySQL客户端可用，可以进一步测试" -ForegroundColor Green
} else {
    Write-Host "❌ 未找到MySQL客户端" -ForegroundColor Red
}

Write-Host "=== MySQL连接测试完成 ===" -ForegroundColor Green
