# 测试数据库连接
Write-Host "=== 数据库连接测试 ===" -ForegroundColor Green

# 测试MySQL连接
try {
    $connection = New-Object System.Data.SqlClient.SqlConnection
    $connection.ConnectionString = "Server=192.168.15.100;Port=3306;Database=hrms_db;User Id=root;Password=shice2022mysql;"
    $connection.Open()
    Write-Host "✅ MySQL连接成功" -ForegroundColor Green
    
    # 测试查询
    $command = $connection.CreateCommand()
    $command.CommandText = "SELECT COUNT(*) as user_count FROM sys_user"
    $reader = $command.ExecuteReader()
    if ($reader.Read()) {
        $userCount = $reader["user_count"]
        Write-Host "✅ 用户表查询成功，用户总数: $userCount" -ForegroundColor Green
    }
    $reader.Close()
    $connection.Close()
} catch {
    Write-Host "❌ 数据库连接失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试Redis连接
try {
    $redisResponse = Test-NetConnection -ComputerName 192.168.15.100 -Port 6379 -InformationLevel Quiet
    if ($redisResponse) {
        Write-Host "✅ Redis连接成功" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Redis连接测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试MinIO连接
try {
    $minioResponse = Test-NetConnection -ComputerName 192.168.15.100 -Port 9000 -InformationLevel Quiet
    if ($minioResponse) {
        Write-Host "✅ MinIO连接成功" -ForegroundColor Green
    } else {
        Write-Host "❌ MinIO连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ MinIO连接测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
