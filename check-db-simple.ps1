Write-Host "检查数据库连接..."

try {
    # 使用MySQL客户端检查
    $result = & mysql -h 192.168.15.100 -u root -pshice2022mysql hrms_db -e "SHOW TABLES LIKE 'hr_%';" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "数据库连接成功"
        Write-Host "找到的表:"
        Write-Host $result
    } else {
        Write-Host "数据库连接失败"
    }
} catch {
    Write-Host "检查失败: $($_.Exception.Message)"
}
