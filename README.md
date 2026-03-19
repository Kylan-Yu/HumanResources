# HRMS Human Resource Management System

## 📋 Project Overview

HRMS (Human Resource Management System) is a human resource management platform built on a Spring Cloud microservice architecture. It supports both enterprise and hospital scenarios, and provides complete capabilities for employee management, organizational structure, compensation and performance, attendance, and training.

---

## 🏗️ Technical Architecture

### Backend Technology Stack
| Technology | Version | Description |
|-----------|---------|-------------|
| **Framework** | Spring Boot 3.0.13 + Spring Cloud 2022.0.4 | Core framework |
| **Microservices** | Spring Cloud Alibaba 2022.0.0.0 | Microservice solution |
| **Database** | MySQL 8.0 | Relational database |
| **Cache** | Redis 6.0+ | In-memory data store |
| **Service Registry** | Nacos 2.3.2 | Service registration and discovery |
| **Gateway** | Spring Cloud Gateway | API gateway |
| **Authentication** | Spring Security + JWT | Security authentication |
| **ORM** | MyBatis Plus 3.5.3 | Object-relational mapping |
| **Documentation** | Knife4j (Swagger 3) | API documentation |
| **Utilities** | Hutool 5.8.20, Fastjson2 2.0.40 | Utility libraries |

### Frontend Technology Stack
| Technology | Version | Description |
|-----------|---------|-------------|
| **Framework** | React 18.2.0 + TypeScript 5.2.2 | Frontend framework |
| **Routing** | React Router DOM 6.8.0 | Route management |
| **UI Library** | Ant Design 5.12.0 | UI component library |
| **State Management** | Zustand 4.4.7 | State management |
| **HTTP Client** | Axios 1.6.0 | HTTP requests |
| **Build Tool** | Vite 4.5.0 | Build tool |
| **Code Style** | ESLint + Prettier | Code quality and formatting |

---

## 🚀 Environment Requirements

### Basic Environment
| Component | Version Requirement | Recommended Version |
|----------|---------------------|---------------------|
| **JDK** | 17+ | OpenJDK 17 |
| **Maven** | 3.6+ | Apache Maven 3.9.4 |
| **Node.js** | 16+ | Node.js 18.17.0 |
| **npm** | 8+ | npm 9.6.7 |
| **MySQL** | 8.0+ | MySQL 8.0.33 |
| **Redis** | 6.0+ | Redis 6.2.13 |
| **Nacos** | 2.3+ | Nacos 2.3.2 |

### Development Tools
- **IDE**: IntelliJ IDEA 2023.2+ or VS Code
- **Database Tools**: Navicat 16+ or DBeaver
- **API Testing**: Postman 10+ or Apifox
- **Git**: 2.40+

---

## 📦 Installation and Deployment

### 1. Environment Preparation

#### JDK 17 Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# macOS (using Homebrew)
brew install openjdk@17

# Windows
# Download and install OpenJDK 17: https://adoptium.net/
```

#### Maven Configuration
```bash
# Verify installation
mvn -version

# Configure Alibaba Cloud mirror (optional)
# Edit ~/.m2/settings.xml
```

#### Node.js Installation
```bash
# Using nvm (recommended)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18.17.0
nvm use 18.17.0

# Or download and install directly
# https://nodejs.org/
```

### 2. Database Initialization

#### MySQL Installation and Configuration
```bash
# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Create database
mysql -u root -p
```

Run the database initialization script:
```bash
# Method 1: Command line execution
mysql -u root -p -h 192.168.15.100 < database/init_complete.sql

# Method 2: MySQL client execution
source /path/to/database/init_complete.sql;
```

#### Redis Installation and Configuration
```bash
# Ubuntu/Debian
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server

# CentOS/RHEL
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis

# Verify connection
redis-cli -h 192.168.15.100 -p 6379 ping
```

### 3. Nacos Startup

#### Download Nacos
```bash
# Download Nacos 2.3.2
wget https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.tar.gz
tar -xzf nacos-server-2.3.2.tar.gz
cd nacos
```

#### Configure Nacos
```bash
# Edit conf/application.properties
# Configure database connection (optional, embedded database is used by default)
```

#### Start Nacos
```bash
# Start in standalone mode
sh startup.sh -m standalone

# Verify startup
# Access: http://192.168.15.100:8848/nacos
# Default account: nacos / nacos
```

### 4. Backend Service Startup

#### Build the Project
```bash
# Enter the project root directory
cd f:/java/myself/human/backend

# Clean and compile
mvn clean compile

# Package
mvn package -DskipTests

# Or use the batch script
./build-and-test.bat
```

#### Service Startup Order
```bash
# 1. Start the Gateway service (Port: 8080)
cd hrms-gateway
mvn spring-boot:run

# 2. Start the Auth service (Port: 8081)
cd hrms-auth
mvn spring-boot:run

# 3. Start the System service (Port: 8082)
cd hrms-system
mvn spring-boot:run

# 4. Start the Organization service (Port: 8083)
cd hrms-org
mvn spring-boot:run

# 5. Start the Employee service (Port: 8084)
cd hrms-employee
mvn spring-boot:run
```

#### Batch Startup
```bash
# Use the startup script
./start-services.bat
```

### 5. Frontend Service Startup

#### Install Dependencies
```bash
# Enter the frontend directory
cd f:/java/myself/human/frontend

# Install dependencies
npm install

# Or use the batch script
./start-frontend.bat
```

#### Start the Development Server
```bash
# Start the development server
npm run dev

# Access URL: http://localhost:3000
```

#### Build for Production
```bash
# Build the production version
npm run build

# Preview the production build
npm run preview
```

---

## 🔐 Default Accounts and Passwords

### System Accounts
| System | Username | Password | Description |
|-------|----------|----------|-------------|
| **HRMS System** | admin | 123456 | Super Administrator |
| **HRMS System** | hr_admin | 123456 | HR Administrator |
| **Nacos** | nacos | nacos | Service registry |
| **MySQL** | root | shice2022mysql | Database |

### Permission Description
- **Super Administrator**: Has full system permissions
- **HR Administrator**: Has HR management permissions
- **Department Manager**: Has management permissions within the assigned department
- **Regular Employee**: Can only view personal information

---

## 🌐 Service Ports

### Backend Service Ports
| Service | Port | Description | Access URL |
|--------|------|-------------|------------|
| **Gateway** | 8080 | API Gateway | http://192.168.15.100:8080 |
| **Auth** | 8081 | Authentication Service | http://192.168.15.100:8081 |
| **System** | 8082 | System Service | http://192.168.15.100:8082 |
| **Org** | 8083 | Organization Service | http://192.168.15.100:8083 |
| **Employee** | 8084 | Employee Service | http://192.168.15.100:8084 |

### Infrastructure Ports
| Component | Port | Description | Access URL |
|----------|------|-------------|------------|
| **Nacos** | 8848 | Service Registry | http://192.168.15.100:8848/nacos |
| **MySQL** | 3306 | Database | 192.168.15.100:3306 |
| **Redis** | 6379 | Cache | 192.168.15.100:6379 |
| **Frontend** | 3000 | Frontend Service | http://localhost:3000 |

### API Documentation Ports
| Service | Documentation URL | Description |
|--------|--------------------|-------------|
| **Gateway** | http://192.168.15.100:8080/doc.html | Gateway API documentation |
| **Auth** | http://192.168.15.100:8081/doc.html | Authentication API documentation |
| **System** | http://192.168.15.100:8082/doc.html | System API documentation |

---

## 🔧 Common Errors and Troubleshooting

### 1. Database Connection Failure

#### Error Messages
```text
Could not create connection to database server
Communications link failure
```

#### Troubleshooting Steps
1. **Check MySQL service status**
   ```bash
   sudo systemctl status mysql
   sudo systemctl start mysql
   ```

2. **Check network connectivity**
   ```bash
   ping 192.168.15.100
   telnet 192.168.15.100 3306
   ```

3. **Verify database configuration**
   - Check the database connection settings in `application.yml`
   - Confirm that the username and password are correct
   - Confirm that the database has been created

4. **Check firewall settings**
   ```bash
   sudo ufw allow 3306
   ```

### 2. Redis Connection Failure

#### Error Messages
```text
Could not get a resource from the pool
JedisConnectionException
```

#### Troubleshooting Steps
1. **Check Redis service status**
   ```bash
   sudo systemctl status redis
   sudo systemctl start redis
   ```

2. **Test Redis connection**
   ```bash
   redis-cli -h 192.168.15.100 -p 6379 ping
   ```

3. **Check Redis configuration**
   - Confirm the Redis bind address
   - Check password configuration
   - Verify the port settings

### 3. Nacos Connection Failure

#### Error Messages
```text
Failed to register service to nacos
Connect to nacos server failed
```

#### Troubleshooting Steps
1. **Check Nacos service status**
   ```bash
   # Check process
   ps -ef | grep nacos

   # View logs
   tail -f logs/nacos.log
   ```

2. **Verify Nacos access**
   ```bash
   curl http://192.168.15.100:8848/nacos/v1/ns/instance/list
   ```

3. **Check network configuration**
   - Confirm the Nacos server address is correct
   - Check firewall settings
   - Verify namespace configuration

### 4. JWT Authentication Failure

#### Error Messages
```text
Authentication failed: Bad credentials
Token has expired
Invalid JWT token
```

#### Troubleshooting Steps
1. **Check user password**
   - Confirm the user exists and the account status is normal
   - Verify that the password is correct (passwords are stored in encrypted form in the database)

2. **Check JWT configuration**
   - Confirm the JWT secret key configuration is correct
   - Check token expiration settings

3. **Verify token format**
   ```bash
   # Parse JWT token
   echo "eyJhbGciOiJIUzI1NiJ9..." | base64 -d
   ```

### 5. Frontend Startup Failure

#### Error Messages
```text
npm ERR! code ENOENT
Module not found
Can't resolve 'react'
```

#### Troubleshooting Steps
1. **Check Node.js version**
   ```bash
   node -v
   npm -v
   ```

2. **Clean dependencies and reinstall**
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Check port occupation**
   ```bash
   # Windows
   netstat -ano | findstr :3000

   # Linux/macOS
   lsof -i :3000
   ```

### 6. Maven Compilation Failure

#### Error Messages
```text
Failed to execute goal on project
Could not resolve dependencies
Compilation failure
```

#### Troubleshooting Steps
1. **Check Maven configuration**
   ```bash
   mvn -version
   echo $MAVEN_HOME
   ```

2. **Clean Maven cache**
   ```bash
   mvn clean
   rm -rf ~/.m2/repository
   mvn compile
   ```

3. **Check dependency versions**
   - Confirm Spring Boot and Spring Cloud version compatibility
   - Check third-party dependency versions

### 7. Service Registration Failure

#### Error Messages
```text
Service registration failed
Instance already exists
```

#### Troubleshooting Steps
1. **Check service configuration**
   - Confirm `spring.application.name` is unique
   - Check Nacos connection settings

2. **Clean Nacos instances**
   - Manually delete the instance in the Nacos console
   - Restart the service to register it again

### 8. CORS Issues

#### Error Messages
```text
Access to XMLHttpRequest at '...' from origin '...' has been blocked by CORS policy
```

#### Troubleshooting Steps
1. **Check Gateway CORS configuration**
   - Confirm that CORS is configured in the Gateway
   - Check allowed origin settings

2. **Check frontend proxy configuration**
   - Confirm that the Vite proxy configuration is correct
   - Verify API request paths

---

## 📞 Technical Support

### Issue Reporting
- **Project Issues**: [GitHub Issues](https://github.com/your-org/hrms/issues)
- **Technical Documentation**: [Project Wiki](https://github.com/your-org/hrms/wiki)

### Development Team
- **Architect**: Responsible for system architecture design
- **Backend Developer**: Responsible for microservice development
- **Frontend Developer**: Responsible for React application development
- **DevOps Engineer**: Responsible for deployment and operations

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

**© 2024 HRMS Team. All rights reserved.**
