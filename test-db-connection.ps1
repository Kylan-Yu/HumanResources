# 测试数据库连接和表结构
Write-Host "=== HRMS 数据库连接测试 ===" -ForegroundColor Green

# 检查MySQL是否可访问
try {
    $result = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ MySQL连接成功" -ForegroundColor Green
    } else {
        Write-Host "❌ MySQL连接失败" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ MySQL命令未找到，请确保MySQL已安装" -ForegroundColor Red
    exit 1
}

# 检查hrms数据库
try {
    $result = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "USE hrms;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ hrms数据库存在" -ForegroundColor Green
    } else {
        Write-Host "❌ hrms数据库不存在，正在创建..." -ForegroundColor Yellow
        & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "CREATE DATABASE hrms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ hrms数据库创建成功" -ForegroundColor Green
        } else {
            Write-Host "❌ hrms数据库创建失败" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "❌ 数据库操作失败" -ForegroundColor Red
    exit 1
}

# 执行初始化脚本
try {
    Write-Host "🔄 正在执行系统表初始化脚本..." -ForegroundColor Yellow
    $scriptPath = "F:\java\myself\human\database\tables\04_system_tables.sql"
    & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot hrms < $scriptPath 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 系统表初始化成功" -ForegroundColor Green
    } else {
        Write-Host "❌ 系统表初始化失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 脚本执行失败" -ForegroundColor Red
}

# 检查表是否创建成功
try {
    $tables = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot hrms -e "SHOW TABLES;" 2>$null
    if ($tables -match "hr_user") {
        Write-Host "✅ 用户表创建成功" -ForegroundColor Green
    }
    if ($tables -match "hr_role") {
        Write-Host "✅ 角色表创建成功" -ForegroundColor Green
    }
    if ($tables -match "hr_menu") {
        Write-Host "✅ 菜单表创建成功" -ForegroundColor Green
    }
    if ($tables -match "hr_dict") {
        Write-Host "✅ 字典表创建成功" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 表检查失败" -ForegroundColor Red
}

Write-Host "`n=== 数据库初始化完成 ===" -ForegroundColor Green
