# 测试前端API连通性
Write-Host "=== HRMS 前后端API连通性测试 ===" -ForegroundColor Green

# 测试用户API
Write-Host "`n1. 测试用户分页查询API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/users/page" -Method Get
    Write-Host "✅ 用户API正常 - 总用户数: $($response.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 用户API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试组织API
Write-Host "`n2. 测试组织分页查询API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/orgs/page" -Method Get
    Write-Host "✅ 组织API正常 - 总组织数: $($response.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 组织API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试员工API
Write-Host "`n3. 测试员工分页查询API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/employees/page" -Method Get
    Write-Host "✅ 员工API正常 - 总员工数: $($response.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 员工API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试前端代理
Write-Host "`n4. 测试前端代理连接..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:3000/api/users/page" -Method Get
    Write-Host "✅ 前端代理正常 - 通过代理访问用户API成功" -ForegroundColor Green
} catch {
    Write-Host "❌ 前端代理失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "前端地址: http://localhost:3000" -ForegroundColor Cyan
Write-Host "后端API: http://localhost:8081" -ForegroundColor Cyan
Write-Host "API文档: http://localhost:8081/swagger-ui.html" -ForegroundColor Cyan
