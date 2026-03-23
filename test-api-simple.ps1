# 简单的API测试脚本
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/users/page" -Method Get -UseBasicParsing
    Write-Host "API测试成功!"
    Write-Host "状态码:" $response.StatusCode
    Write-Host "响应内容:"
    Write-Host $response.Content
} catch {
    Write-Host "API测试失败!"
    Write-Host "错误信息:" $_.Exception.Message
    Write-Host "状态码:" $_.Exception.Response.StatusCode
}
