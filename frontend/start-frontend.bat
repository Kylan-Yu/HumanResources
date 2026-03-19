@echo off
echo ========================================
echo HRMS Frontend 启动脚本
echo ========================================

echo.
echo 检查Node.js环境...
node --version
npm --version

echo.
echo [1] 安装依赖...
npm install

echo.
echo [2] 启动开发服务器...
npm run dev

pause
