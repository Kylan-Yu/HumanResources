@echo off
echo ========================================
echo HRMS 微服务启动脚本
echo ========================================
echo.
echo 环境配置：
echo - MySQL: 192.168.15.100:3306
echo - Redis: 192.168.15.100:6379
echo - Nacos: 192.168.15.100:8848
echo.

echo 请确保以下服务已启动：
echo 1. MySQL数据库 (端口3306)
echo 2. Redis缓存 (端口6379)
echo 3. Nacos注册中心 (端口8848)
echo.

set /p confirm="确认环境已就绪？(y/n): "
if /i "%confirm%" neq "y" (
    echo 请先启动基础服务！
    pause
    exit /b 1
)

echo.
echo 开始启动微服务...
echo.

echo [1] 启动Gateway服务 (端口8080)...
start "Gateway" cmd /k "cd hrms-gateway && mvn spring-boot:run"

timeout /t 10

echo [2] 启动Auth服务 (端口8081)...
start "Auth" cmd /k "cd hrms-auth && mvn spring-boot:run"

timeout /t 10

echo [3] 启动System服务 (端口8082)...
start "System" cmd /k "cd hrms-system && mvn spring-boot:run"

timeout /t 10

echo [4] 启动Org服务 (端口8083)...
start "Org" cmd /k "cd hrms-org && mvn spring-boot:run"

timeout /t 10

echo [5] 启动Employee服务 (端口8084)...
start "Employee" cmd /k "cd hrms-employee && mvn spring-boot:run"

echo.
echo ========================================
echo 微服务启动完成！
echo.
echo 服务访问地址：
echo - Gateway: http://localhost:8080
echo - Auth: http://localhost:8081
echo - System: http://localhost:8082
echo - Org: http://localhost:8083
echo - Employee: http://localhost:8084
echo.
echo API文档地址：
echo - Gateway: http://localhost:8080/doc.html
echo - Auth: http://localhost:8081/doc.html
echo ========================================

pause
