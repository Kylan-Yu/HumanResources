@echo off
echo === HRMS 项目服务状态检查 ===
echo.

echo 1. 检查MySQL服务...
tasklist | findstr mysqld > nul
if %errorlevel% == 0 (
    echo ✅ MySQL服务运行中
) else (
    echo ❌ MySQL服务未运行
)

echo.
echo 2. 检查前端服务...
tasklist | findstr node > nul
if %errorlevel% == 0 (
    echo ✅ 前端服务运行中
    echo    访问地址: http://localhost:3000
) else (
    echo ❌ 前端服务未运行
)

echo.
echo 3. 检查后端服务...
tasklist | findstr java > nul
if %errorlevel% == 0 (
    echo ✅ 后端服务运行中
    echo    访问地址: http://localhost:8082
    echo    API文档: http://localhost:8082/doc.html
) else (
    echo ❌ 后端服务未运行
)

echo.
echo 4. 测试API连接...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8082/users/page' -Method Get -UseBasicParsing -TimeoutSec 5; Write-Host '✅ API连接正常，状态码:' $response.StatusCode } catch { Write-Host '❌ API连接失败' }"

echo.
echo === 服务状态检查完成 ===
echo.
echo 可用地址:
echo 前端应用: http://localhost:3000
echo 系统API: http://localhost:8082
echo API文档: http://localhost:8082/doc.html
echo.
pause
