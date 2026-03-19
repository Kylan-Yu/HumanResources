@echo off
echo ========================================
echo 创建其他服务配置文件
echo ========================================

echo.
echo [1] 创建招聘服务配置...
(
echo server:
echo   port: 8085
echo.
echo spring:
echo   application:
echo     name: hrms-recruit
echo   profiles:
echo     active: dev
echo   cloud:
echo     nacos:
echo       discovery:
echo         server-addr: 192.168.15.100:8848
echo         namespace: hrms
echo         group: DEFAULT_GROUP
echo       config:
echo         server-addr: 192.168.15.100:8848
echo         namespace: hrms
echo         group: DEFAULT_GROUP
echo         file-extension: yml
echo   datasource:
echo     driver-class-name: com.mysql.cj.jdbc.Driver
echo     url: jdbc:mysql://192.168.15.100:3306/hrms_db?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai
echo     username: root
echo     password: shice2022mysql
echo     type: com.alibaba.druid.pool.DruidDataSource
echo     druid:
echo       initial-size: 5
echo       min-idle: 5
echo       max-active: 20
echo       max-wait: 60000
echo       time-between-eviction-runs-millis: 60000
echo       min-evictable-idle-time-millis: 300000
echo       validation-query: SELECT 1
echo       test-while-idle: true
echo       test-on-borrow: false
echo       test-on-return: false
echo   redis:
echo     host: 192.168.15.100
echo     port: 6379
echo     password: 
echo     database: 0
echo     timeout: 3000ms
echo     lettuce:
echo       pool:
echo         max-active: 20
echo         max-idle: 10
echo         min-idle: 0
echo         max-wait: -1ms
echo.
echo # MyBatis Plus配置
echo mybatis-plus:
echo   configuration:
echo     map-underscore-to-camel-case: true
echo     cache-enabled: false
echo     call-setters-on-nulls: true
echo     jdbc-type-for-null: 'null'
echo   global-config:
echo     db-config:
echo       id-type: auto
echo       logic-delete-field: deleted
echo       logic-delete-value: 1
echo       logic-not-delete-value: 0
echo   mapper-locations: classpath*:mapper/**/*.xml
echo.
echo # 日志配置
echo logging:
echo   level:
echo     com.hrms: debug
echo   pattern:
echo     console: '%%d{yyyy-MM-dd HH:mm:ss} [%%thread] %%-5level %%logger{50} - %%msg%%n'
echo.
echo # 管理端点
echo management:
echo   endpoints:
echo     web:
echo       exposure:
echo         include: health,info
) > "hrms-recruit\src\main\resources\application.yml"

echo [2] 创建考勤服务配置...
(
echo server:
echo   port: 8086
echo.
echo spring:
echo   application:
echo     name: hrms-attendance
echo   profiles:
echo     active: dev
echo   cloud:
echo     nacos:
echo       discovery:
echo         server-addr: 192.168.15.100:8848
echo         namespace: hrms
echo         group: DEFAULT_GROUP
echo       config:
echo         server-addr: 192.168.15.100:8848
echo         namespace: hrms
echo         group: DEFAULT_GROUP
echo         file-extension: yml
echo   datasource:
echo     driver-class-name: com.mysql.cj.jdbc.Driver
echo     url: jdbc:mysql://192.168.15.100:3306/hrms_db?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai
echo     username: root
echo     password: shice2022mysql
echo     type: com.alibaba.druid.pool.DruidDataSource
echo     druid:
echo       initial-size: 5
echo       min-idle: 5
echo       max-active: 20
echo       max-wait: 60000
echo       time-between-eviction-runs-millis: 60000
echo       min-evictable-idle-time-millis: 300000
echo       validation-query: SELECT 1
echo       test-while-idle: true
echo       test-on-borrow: false
echo       test-on-return: false
echo   redis:
echo     host: 192.168.15.100
echo     port: 6379
echo     password: 
echo     database: 0
echo     timeout: 3000ms
echo     lettuce:
echo       pool:
echo         max-active: 20
echo         max-idle: 10
echo         min-idle: 0
echo         max-wait: -1ms
echo.
echo # MyBatis Plus配置
echo mybatis-plus:
echo   configuration:
echo     map-underscore-to-camel-case: true
echo     cache-enabled: false
echo     call-setters-on-nulls: true
echo     jdbc-type-for-null: 'null'
echo   global-config:
echo     db-config:
echo       id-type: auto
echo       logic-delete-field: deleted
echo       logic-delete-value: 1
echo       logic-not-delete-value: 0
echo   mapper-locations: classpath*:mapper/**/*.xml
echo.
echo # 日志配置
echo logging:
echo   level:
echo     com.hrms: debug
echo   pattern:
echo     console: '%%d{yyyy-MM-dd HH:mm:ss} [%%thread] %%-5level %%logger{50} - %%msg%%n'
echo.
echo # 管理端点
echo management:
echo   endpoints:
echo     web:
echo       exposure:
echo         include: health,info
) > "hrms-attendance\src\main\resources\application.yml"

echo.
echo 配置文件创建完成！
echo.
echo ========================================
echo 已创建的服务配置：
echo - hrms-recruit (端口8085)
echo - hrms-attendance (端口8086)
echo ========================================

pause
