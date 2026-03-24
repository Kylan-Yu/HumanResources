@echo off
echo ========================================
echo HRMS 快速启动脚本
echo ========================================
echo.

echo [1] 启动前端服务...
start "Frontend" cmd /k "cd ../frontend && npm run dev"

echo.
echo [2] 启动Auth服务...
start "Auth" cmd /k "cd hrms-auth && mvn spring-boot:run"

echo.
echo [3] 启动Org服务...
start "Org" cmd /k "cd hrms-org && mvn spring-boot:run"

echo.
echo [4] 启动Employee服务...
start "Employee" cmd /k "cd hrms-employee && mvn spring-boot:run"

echo.
echo ========================================
echo 服务启动中...
echo.
echo 访问地址：
echo - 前端: http://localhost:3000
echo - Auth: http://localhost:8081
echo - Org: http://localhost:8082  
echo - Employee: http://localhost:8083
echo.
echo 请等待服务启动完成...
echo ========================================

pause
