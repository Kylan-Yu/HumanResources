@echo off
echo ========================================
echo HRMS Backend Build and Test
echo ========================================

echo.
echo [1] Cleaning previous builds...
call mvn clean

echo.
echo [2] Compiling all modules...
call mvn compile -DskipTests

echo.
echo [3] Running tests...
call mvn test

echo.
echo [4] Packaging applications...
call mvn package -DskipTests

echo.
echo [5] Build completed!
echo.
echo ========================================
echo Build artifacts location:
echo - hrms-common/target/hrms-common-1.0.0.jar
echo - hrms-gateway/target/hrms-gateway-1.0.0.jar
echo - hrms-auth/target/hrms-auth-1.0.0.jar
echo - hrms-system/target/hrms-system-1.0.0.jar
echo - hrms-org/target/hrms-org-1.0.0.jar
echo - hrms-employee/target/hrms-employee-1.0.0.jar
echo ========================================

pause
