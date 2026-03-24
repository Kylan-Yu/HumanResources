# 检查数据库表是否存在
Write-Host "检查数据库连接和表..."

# 尝试连接数据库
try {
    $connection = New-Object System.Data.SqlClient.SqlConnection
    $connection.ConnectionString = "Server=192.168.15.100;Database=hrms_db;User Id=root;Password=shice2022mysql;"
    $connection.Open()
    
    # 检查表是否存在
    $command = $connection.CreateCommand()
    $command.CommandText = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'hrms_db' AND TABLE_NAME IN ('hr_org', 'hr_dept', 'hr_position')"
    
    $reader = $command.ExecuteReader()
    $tables = @()
    while ($reader.Read()) {
        $tables += $reader["TABLE_NAME"]
    }
    $reader.Close()
    
    Write-Host "找到的表: $($tables -join ', ')"
    
    # 检查每个表的数据量
    foreach ($table in $tables) {
        $command.CommandText = "SELECT COUNT(*) as count FROM $table"
        $count = $command.ExecuteScalar()
        Write-Host "$table: $count 条记录"
    }
    
    $connection.Close()
    Write-Host "数据库检查完成"
    
} catch {
    Write-Host "数据库连接失败: $($_.Exception.Message)"
}
