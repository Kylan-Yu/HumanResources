@echo off
echo === HRMS Database Initialization ===

echo Creating hrms database...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "CREATE DATABASE IF NOT EXISTS hrms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo Creating system tables...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot hrms < "F:\java\myself\human\database\tables\04_system_tables.sql"

echo Checking tables...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot hrms -e "SHOW TABLES;"

echo Database initialization completed!
pause
