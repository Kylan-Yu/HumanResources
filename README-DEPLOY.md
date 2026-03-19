# HRMS Deployment Guide

## 1. Overall Project Architecture

HRMS (Human Resource Management System) is a microservices-based human resource management platform built with a frontend-backend separation architecture. It supports both enterprise and hospital industry scenarios.

### Technical Architecture
- **Frontend**: React 18 + TypeScript + Ant Design
- **Backend**: Spring Boot 3.x + Spring Cloud + MyBatis-Plus
- **Database**: MySQL 8.0
- **Cache**: Redis 6.x
- **Service Registry**: Nacos 2.x
- **Gateway**: Spring Cloud Gateway
- **File Storage**: MinIO

### Microservices Architecture
```text
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Gateway       │
│   (React)       │◄──►│   (8080)        │
└─────────────────┘    └─────────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────▼──────┐ ┌─────▼─────┐ ┌───▼──────┐
        │  hrms-auth    │ │ hrms-org   │ │hrms-employee│
        │   (8081)      │ │  (8082)    │ │   (8083)    │
        └──────────────┘ └───────────┘ └───────────┘
                │               │               │
        ┌───────▼──────┐ ┌─────▼─────┐ ┌───▼──────┐
        │hrms-contract  │ │hrms-recruit│ │hrms-payroll │
        │   (8084)      │ │  (8085)    │ │   (8086)    │
        └──────────────┘ └───────────┘ └───────────┘
                │               │               │
                └───────────────┼───────────────┘
                                │
                    ┌───────────▼───────────┐
                    │   Nacos (8848)        │
                    │   MySQL (3306)        │
                    │   Redis (6379)        │
                    │   MinIO (9000)        │
                    └───────────────────────┘
```

## 2. Microservice List and Ports

| Service Name | Port | Description | Startup Order |
|-------------|------|-------------|---------------|
| hrms-gateway | 8080 | Gateway service | 2 |
| hrms-auth | 8081 | Authentication and authorization service | 3 |
| hrms-org | 8082 | Organization management service | 4 |
| hrms-employee | 8083 | Employee management service | 5 |
| hrms-contract | 8084 | Contract management service | 6 |
| hrms-recruit | 8085 | Recruitment management service | 7 |
| hrms-payroll | 8086 | Payroll management service | 8 |

## 3. Service Dependency Relationships

### Infrastructure Dependencies
- **Nacos**: Required by all services
- **MySQL**: Required by all services
- **Redis**: Required by the gateway and authentication service

### Inter-Service Dependencies
```text
hrms-auth (independent)
    ↓
hrms-org → hrms-auth
    ↓
hrms-employee → hrms-org → hrms-auth
    ↓
hrms-contract → hrms-employee → hrms-org → hrms-auth
    ↓
hrms-recruit → hrms-org → hrms-auth
    ↓
hrms-payroll → hrms-employee → hrms-org → hrms-auth
```

## 4. Startup Order

1. **Start infrastructure services**
   ```bash
   # Start MySQL
   systemctl start mysql

   # Start Redis
   systemctl start redis

   # Start Nacos
   cd /opt/nacos
   bash startup.sh -m standalone
   ```

2. **Start application services**
   ```bash
   # 1. Gateway service
   cd /opt/hrms/hrms-gateway
   nohup java -jar hrms-gateway.jar > gateway.log 2>&1 &

   # 2. Authentication service
   cd /opt/hrms/hrms-auth
   nohup java -jar hrms-auth.jar > auth.log 2>&1 &

   # 3. Organization service
   cd /opt/hrms/hrms-org
   nohup java -jar hrms-org.jar > org.log 2>&1 &

   # 4. Employee service
   cd /opt/hrms/hrms-employee
   nohup java -jar hrms-employee.jar > employee.log 2>&1 &

   # 5. Contract service
   cd /opt/hrms/hrms-contract
   nohup java -jar hrms-contract.jar > contract.log 2>&1 &

   # 6. Recruitment service
   cd /opt/hrms/hrms-recruit
   nohup java -jar hrms-recruit.jar > recruit.log 2>&1 &

   # 7. Payroll service
   cd /opt/hrms/hrms-payroll
   nohup java -jar hrms-payroll.jar > payroll.log 2>&1 &
   ```

## 5. Nacos Configuration

### Nacos Installation
```bash
# Download Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz

# Extract
tar -xzf nacos-server-2.2.3.tar.gz

# Configure database
cd nacos/conf
cp application.properties.example application.properties

# Modify application.properties
```

### application.properties Configuration
```properties
# Database configuration
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://localhost:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=root

# Cluster configuration
nacos.core.auth.system.type=nacos
nacos.core.auth.enabled=false
nacos.core.auth.default.token.secret.key=SecretKey012345678901234567890123456789012345678901234567890123456789
```

### Service Registration Configuration
Each service needs the following in `application.yml`:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: hrms
        group: DEFAULT_GROUP
      config:
        server-addr: localhost:8848
        namespace: hrms
        group: DEFAULT_GROUP
        file-extension: yml
```

## 6. MySQL Initialization

### Create Database
```sql
-- Create database
CREATE DATABASE hrms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'hrms'@'%' IDENTIFIED BY 'hrms123';
GRANT ALL PRIVILEGES ON hrms.* TO 'hrms'@'%';
FLUSH PRIVILEGES;
```

### Initialize Table Structures
```bash
# Execute table scripts in order
mysql -uhrms -phrms123 hrms < database/tables/01_auth_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/02_org_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/03_employee_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/04_system_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/05_contract_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/06_recruit_tables.sql
mysql -uhrms -phrms123 hrms < database/tables/07_payroll_tables.sql
```

### Import Initial Data
```bash
# Import seed data
mysql -uhrms -phrms123 hrms < database/data/init_data.sql
```

## 7. Redis Configuration

### Redis Installation
```bash
# CentOS/RHEL
yum install redis

# Ubuntu/Debian
apt-get install redis-server

# Build from source
wget http://download.redis.io/releases/redis-6.2.13.tar.gz
tar -xzf redis-6.2.13.tar.gz
cd redis-6.2.13
make
make install
```

### Redis Configuration File
```bash
# /etc/redis/redis.conf
bind 0.0.0.0
port 6379
requirepass redis123
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

### Start Redis
```bash
# Start Redis
redis-server /etc/redis/redis.conf

# Verify connection
redis-cli -h localhost -p 6379 -a redis123
ping
```

## 8. MinIO Configuration

### MinIO Installation
```bash
# Download MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio

# Grant execute permission
chmod +x minio

# Create data directory
mkdir -p /opt/minio/data

# Start MinIO
MINIO_ROOT_USER=minioadmin MINIO_ROOT_PASSWORD=minioadmin123 ./minio server /opt/minio/data --console-address ":9001"
```

### MinIO Application Configuration
```yaml
# Configure in services that need file upload
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: hrms-files
```

## 9. Frontend Startup

### Start in Development Environment
```bash
# Install dependencies
cd /opt/hrms/frontend
npm install

# Start development server
npm run dev

# Access URL
http://localhost:3000
```

### Environment Configuration
```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api
VITE_UPLOAD_URL=http://localhost:9000
VITE_APP_TITLE=HRMS Human Resource Management System
```

## 10. Frontend Build

### Production Build
```bash
# Build
npm run build

# Build output
dist/
├── assets/
├── index.html
└── ...
```

### Nginx Configuration
```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /opt/hrms/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 11. Backend Build

### Maven Build
```bash
# Build a single service
cd /opt/hrms/hrms-auth
mvn clean package -Dmaven.test.skip=true

# Batch build script
#!/bin/bash
services=("hrms-auth" "hrms-org" "hrms-employee" "hrms-contract" "hrms-recruit" "hrms-payroll")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd /opt/hrms/$service
    mvn clean package -Dmaven.test.skip=true
    cp target/$service.jar /opt/hrms/deploy/
done
```

### Docker Build
```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim

COPY target/hrms-auth.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build image
docker build -t hrms/auth:latest .

# Run container
docker run -d -p 8081:8081 --name hrms-auth hrms/auth:latest
```

## 12. Backend Startup

### Start with JAR Package
```bash
# Start a single service
java -jar hrms-auth.jar

# Start with profile
java -jar hrms-auth.jar --spring.profiles.active=prod

# Run in background
nohup java -jar hrms-auth.jar > auth.log 2>&1 &

# Specify memory
java -Xms512m -Xmx1024m -jar hrms-auth.jar
```

### Startup Script
```bash
#!/bin/bash
# start-all.sh

services=("hrms-gateway:8080" "hrms-auth:8081" "hrms-org:8082" "hrms-employee:8083" "hrms-contract:8084" "hrms-recruit:8085" "hrms-payroll:8086")

for service in "${services[@]}"; do
    name=$(echo $service | cut -d':' -f1)
    port=$(echo $service | cut -d':' -f2)

    echo "Starting $name on port $port..."
    nohup java -jar /opt/hrms/deploy/$name.jar > /opt/hrms/logs/$name.log 2>&1 &

    # Wait for the service to start
    sleep 10

    # Check service status
    if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "$name started successfully"
    else
        echo "$name failed to start"
    fi
done
```

## 13. Linux Deployment Recommendations

### System Requirements
- **Operating System**: CentOS 7+ / Ubuntu 18+
- **Memory**: Minimum 8GB, recommended 16GB
- **CPU**: Minimum 4 cores, recommended 8 cores
- **Disk**: Minimum 100GB, recommended 500GB SSD

### Directory Structure
```bash
/opt/hrms/
├── backend/          # Backend services
├── frontend/         # Frontend files
├── deploy/           # Deployment packages
├── logs/             # Log files
├── config/           # Configuration files
└── scripts/          # Script files
```

### systemd Service Configuration
```bash
# /etc/systemd/system/hrms-auth.service
[Unit]
Description=HRMS Auth Service
After=network.target

[Service]
Type=simple
User=hrms
WorkingDirectory=/opt/hrms/deploy
ExecStart=/usr/bin/java -jar hrms-auth.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Enable service
systemctl enable hrms-auth
systemctl start hrms-auth
systemctl status hrms-auth
```

## 14. Docker Deployment Recommendations

### Docker Compose Configuration
```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: hrms
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database:/docker-entrypoint-initdb.d

  redis:
    image: redis:6.2
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  nacos:
    image: nacos/nacos-server:v2.2.3
    ports:
      - "8848:8848"
    environment:
      MODE: standalone
    depends_on:
      - mysql

  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    volumes:
      - minio_data:/data

  hrms-gateway:
    build: ./backend/hrms-gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos
    environment:
      SPRING_PROFILES_ACTIVE: docker

  hrms-auth:
    build: ./backend/hrms-auth
    ports:
      - "8081:8081"
    depends_on:
      - nacos
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: docker

volumes:
  mysql_data:
  redis_data:
  minio_data:
```

### Startup Commands
```bash
# Build and start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f hrms-auth
```

## 15. Common Errors and Troubleshooting

### Service Startup Failure
```bash
# View service logs
tail -f /opt/hrms/logs/hrms-auth.log

# Check port usage
netstat -tlnp | grep 8081

# Check Java processes
ps aux | grep java

# Check memory usage
free -h
```

### Database Connection Failure
```bash
# Check MySQL status
systemctl status mysql

# Test database connection
mysql -uhrms -phrms123 -h localhost

# Check firewall
firewall-cmd --list-ports
```

### Nacos Connection Failure
```bash
# Check Nacos status
curl http://localhost:8848/nacos/v1/ns/service/list

# View Nacos logs
tail -f /opt/nacos/logs/startup.out

# Check network connectivity
telnet localhost 8848
```

### Frontend Access Failure
```bash
# Check Nginx status
systemctl status nginx

# View Nginx logs
tail -f /var/log/nginx/error.log

# Test backend API
curl http://localhost:8080/api/auth/login
```

## 16. Default Test Accounts

### System Administrator
- **Username**: admin
- **Password**: 123456
- **Permissions**: Full access
- **Purpose**: System administration and configuration maintenance

### HR Manager
- **Username**: hr_manager
- **Password**: 123456
- **Permissions**: HR-related permissions
- **Purpose**: Employee management, recruitment management, and payroll management

### Department Manager
- **Username**: manager
- **Password**: 123456
- **Permissions**: Department-related permissions
- **Purpose**: Employee and contract management within the department

### Regular Employee
- **Username**: employee
- **Password**: 123456
- **Permissions**: Basic read access
- **Purpose**: View personal information and contract information

## 17. Production Environment Recommendations

### Security Configuration
```yaml
# Production environment configuration
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/hrms?useSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  redis:
    host: prod-redis
    password: ${REDIS_PASSWORD}
    port: 6379
    ssl: true

server:
  port: 8081
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
```

### Performance Optimization
```yaml
# JVM options
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication

# Database connection pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Monitoring Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### Logging Configuration
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/opt/hrms/logs/hrms-auth.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/opt/hrms/logs/hrms-auth.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Backup Strategy
```bash
# Database backup script
#!/bin/bash
BACKUP_DIR="/opt/backup/hrms"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup database
mysqldump -uhrms -phrms123 hrms > $BACKUP_DIR/hrms_$DATE.sql

# Backup configuration files
tar -czf $BACKUP_DIR/config_$DATE.tar.gz /opt/hrms/config

# Clean up old backups (keep 30 days)
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
```

### Health Check
```bash
#!/bin/bash
# health-check.sh

services=("hrms-gateway:8080" "hrms-auth:8081" "hrms-org:8082" "hrms-employee:8083" "hrms-contract:8084" "hrms-recruit:8085" "hrms-payroll:8086")

for service in "${services[@]}"; do
    name=$(echo $service | cut -d':' -f1)
    port=$(echo $service | cut -d':' -f2)

    if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "✅ $name is healthy"
    else
        echo "❌ $name is unhealthy"
        # Send alert notification
        curl -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
             -d "chat_id=${TELEGRAM_CHAT_ID}&text=🚨 HRMS Alert: $name is unhealthy"
    fi
done
```

---

## Deployment Verification

After deployment is complete, verify the system using the following steps:

1. **Access the system**
   - Frontend URL: http://localhost:3000
   - Gateway URL: http://localhost:8080
   - Nacos Console: http://localhost:8848/nacos

2. **Login verification**
   - Log in with the default accounts
   - Verify menu permissions
   - Verify feature operations

3. **Service status check**
   - All services are running normally
   - Database connection is working
   - Cache service is working

4. **Log inspection**
   - Check the startup logs of each service
   - Confirm there are no error messages
   - Monitor runtime status

If any issues occur, refer to the troubleshooting section above.
