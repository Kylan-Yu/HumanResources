# HRMS Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the HRMS (Human Resource Management System) in various environments, from local development to production cloud deployments.

## Quick Start with Docker

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM
- 20GB+ disk space

### One-Command Deployment

```bash
# Clone the repository
git clone https://github.com/your-org/hrms.git
cd hrms

# Start all services
make quick-start

# Or using docker-compose directly
docker-compose up -d
```

### Access the Application

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **API Documentation**: http://localhost:8080/doc.html
- **Nacos Console**: http://localhost:8848/nacos (nacos/nacos)
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin123)
- **Grafana Dashboard**: http://localhost:3001 (admin/admin)

## Environment Configuration

### Environment Variables

Create a `.env` file in the project root:

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=hrms
MYSQL_USER=hrms
MYSQL_PASSWORD=hrms123

# Redis Configuration
REDIS_PASSWORD=redis123

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000

# MinIO Configuration
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123

# Application Configuration
SPRING_PROFILES_ACTIVE=docker
```

### Service Ports

| Service | Port | Description |
|----------|------|-------------|
| Frontend | 3000 | React application |
| API Gateway | 8080 | Spring Cloud Gateway |
| Auth Service | 8081 | Authentication service |
| Org Service | 8082 | Organization service |
| Employee Service | 8083 | Employee service |
| Payroll Service | 8084 | Payroll service |
| Recruitment Service | 8085 | Recruitment service |
| MySQL | 3306 | Database |
| Redis | 6379 | Cache |
| Nacos | 8848 | Service registry |
| MinIO | 9000 | File storage |
| MinIO Console | 9001 | File management UI |
| Prometheus | 9090 | Monitoring |
| Grafana | 3001 | Visualization |

## Development Deployment

### Local Development Setup

#### 1. Backend Services

```bash
# Navigate to backend directory
cd backend

# Build all services
mvn clean package -DskipTests

# Start infrastructure services
docker-compose up -d mysql redis nacos

# Start application services
./start-services.bat
```

#### 2. Frontend Development

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

#### 3. Database Setup

```bash
# Create database and import schema
mysql -u root -p
CREATE DATABASE hrms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Import schema and demo data
mysql -u root -p hrms < database/init_complete.sql
mysql -u root -p hrms < database/demo-data.sql
```

### IDE Configuration

#### IntelliJ IDEA

1. Import the project as a Maven project
2. Set JDK 17 as the project SDK
3. Configure Spring Boot run configurations for each service
4. Enable Lombok plugin
5. Set code style to import from project

#### VS Code

1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure settings.json:
```json
{
    "java.home": "/path/to/jdk-17",
    "spring-boot.ls.checkJVM": false,
    "java.compile.nullAnalysis.mode": "automatic"
}
```

## Production Deployment

### Kubernetes Deployment

#### 1. Prepare Kubernetes Manifests

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: hrms
---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hrms-config
  namespace: hrms
data:
  SPRING_PROFILES_ACTIVE: "kubernetes"
  NACOS_SERVER_ADDR: "nacos-service:8848"
  MYSQL_HOST: "mysql-service"
  REDIS_HOST: "redis-service"
```

#### 2. Deploy Infrastructure

```bash
# Apply namespace and configuration
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Deploy infrastructure services
kubectl apply -f k8s/mysql/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/nacos/

# Wait for services to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n hrms --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n hrms --timeout=300s
kubectl wait --for=condition=ready pod -l app=nacos -n hrms --timeout=300s
```

#### 3. Deploy Application Services

```bash
# Deploy microservices
kubectl apply -f k8s/services/
kubectl apply -f k8s/deployments/

# Deploy ingress
kubectl apply -f k8s/ingress/
```

#### 4. Verify Deployment

```bash
# Check pod status
kubectl get pods -n hrms

# Check services
kubectl get services -n hrms

# Check logs
kubectl logs -f deployment/hrms-gateway -n hrms
```

### Cloud Deployment

#### AWS ECS Deployment

1. **Create ECR Repository**
```bash
aws ecr create-repository --repository-name hrms-gateway
aws ecr create-repository --repository-name hrms-auth
# ... for each service
```

2. **Build and Push Images**
```bash
# Login to ECR
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-west-2.amazonaws.com

# Build and push
docker build -t hrms-gateway ./backend/hrms-gateway
docker tag hrms-gateway:latest <account-id>.dkr.ecr.us-west-2.amazonaws.com/hrms-gateway:latest
docker push <account-id>.dkr.ecr.us-west-2.amazonaws.com/hrms-gateway:latest
```

3. **Create ECS Task Definition**
```json
{
  "family": "hrms-gateway",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "hrms-gateway",
      "image": "<account-id>.dkr.ecr.us-west-2.amazonaws.com/hrms-gateway:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/hrms-gateway",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### Azure AKS Deployment

1. **Create Resource Group and AKS Cluster**
```bash
az group create --name hrms-rg --location eastus
az aks create --resource-group hrms-rg --name hrms-aks --node-count 3 --enable-addons monitoring
```

2. **Configure ACR Integration**
```bash
az acr create --resource-group hrms-rg --name hrmsacr --sku Basic
az aks update --name hrms-aks --resource-group hrms-rg --attach-acr hrmsacr
```

3. **Deploy to AKS**
```bash
# Build and push to ACR
az acr build --registry hrmsacr --image hrms-gateway ./backend/hrms-gateway

# Deploy using Helm
helm install hrms ./helm/hrms --namespace hrms --create-namespace
```

#### Google Cloud GKE Deployment

1. **Create GKE Cluster**
```bash
gcloud container clusters create hrms-cluster \
    --zone us-central1-a \
    --num-nodes 3 \
    --enable-autoscaling \
    --min-nodes 1 \
    --max-nodes 10
```

2. **Build and Push to GCR**
```bash
# Configure Docker to use gcloud as a credential helper
gcloud auth configure-docker

# Build and push
docker build -t gcr.io/PROJECT-ID/hrms-gateway ./backend/hrms-gateway
docker push gcr.io/PROJECT-ID/hrms-gateway
```

3. **Deploy to GKE**
```bash
# Apply manifests
kubectl apply -f k8s/gke/
```

## Monitoring and Logging

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'hrms-gateway'
    static_configs:
      - targets: ['hrms-gateway:8080']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'hrms-auth'
    static_configs:
      - targets: ['hrms-auth:8081']
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboards

Import the provided dashboards:
- HRMS System Overview
- Service Performance Metrics
- Database Performance
- Application Health

### Log Aggregation

#### ELK Stack Setup

```yaml
# docker-compose.logging.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.15.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"
      
  logstash:
    image: docker.elastic.co/logstash/logstash:7.15.0
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    ports:
      - "5044:5044"
      
  kibana:
    image: docker.elastic.co/kibana/kibana:7.15.0
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
```

## Security Configuration

### SSL/TLS Configuration

#### Nginx SSL Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name hrms.yourdomain.com;
    
    ssl_certificate /etc/ssl/certs/hrms.crt;
    ssl_certificate_key /etc/ssl/private/hrms.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    
    location / {
        proxy_pass http://hrms-frontend:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Database Security

```sql
-- Create dedicated database user
CREATE USER 'hrms_app'@'%' IDENTIFIED BY 'strong_password_here';
GRANT SELECT, INSERT, UPDATE, DELETE ON hrms.* TO 'hrms_app'@'%';

-- Enable SSL connections
ALTER USER 'hrms_app'@'%' REQUIRE SSL;
```

### API Security

```yaml
# Rate limiting configuration
spring:
  cloud:
    gateway:
      routes:
        - id: hrms-auth
          uri: lb://hrms-auth
          predicates:
            - Path=/api/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

## Backup and Recovery

### Database Backup

```bash
#!/bin/bash
# backup.sh
BACKUP_DIR="/backups/hrms"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup
mysqldump -u hrms -p hrms | gzip > $BACKUP_DIR/hrms_backup_$DATE.sql.gz

# Upload to S3
aws s3 cp $BACKUP_DIR/hrms_backup_$DATE.sql.gz s3://hrms-backups/database/

# Clean old backups (keep 30 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

### Application Backup

```bash
#!/bin/bash
# backup-app.sh

# Backup configuration
kubectl get configmap hrms-config -n hrms -o yaml > configmap_backup.yaml

# Backup secrets (encrypted)
kubectl get secret hrms-secrets -n hrms -o yaml | ansible-vault encrypt > secrets_backup.yaml

# Backup persistent volumes
kubectl get pvc -n hrms -o yaml > pvc_backup.yaml
```

## Performance Optimization

### JVM Tuning

```bash
# Production JVM options
JAVA_OPTS="-Xms2g -Xmx4g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseStringDeduplication \
           -XX:+OptimizeStringConcat \
           -Djava.security.egd=file:/dev/./urandom"
```

### Database Optimization

```sql
-- MySQL configuration
SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB
SET GLOBAL innodb_log_file_size = 268435456;    -- 256MB
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL sync_binlog = 0;
```

### Caching Strategy

```yaml
# Redis configuration
spring:
  redis:
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
```

## Troubleshooting

### Common Issues

#### Service Won't Start

```bash
# Check service logs
docker-compose logs hrms-gateway

# Check service dependencies
docker-compose ps

# Check resource usage
docker stats
```

#### Database Connection Issues

```bash
# Test database connection
docker-compose exec mysql mysql -u hrms -phrms123 -e "SELECT 1"

# Check database logs
docker-compose logs mysql
```

#### Memory Issues

```bash
# Check JVM memory usage
docker-compose exec hrms-gateway jstat -gc 1

# Adjust heap size
export JAVA_OPTS="-Xms1g -Xmx2g"
```

### Health Checks

```bash
# Service health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8081/actuator/health

# Overall system health
make health
```

## Maintenance

### Regular Tasks

1. **Daily**
   - Monitor system health
   - Check error logs
   - Verify backup completion

2. **Weekly**
   - Review performance metrics
   - Apply security patches
   - Clean up old logs

3. **Monthly**
   - Update dependencies
   - Review capacity planning
   - Test disaster recovery

### Update Process

```bash
# Update application
git pull origin main
make build
make up

# Update database
docker-compose exec mysql mysql -u hrms -phrms123 hrms < migration_script.sql

# Verify update
make health
curl http://localhost:8080/actuator/info
```

## Support

For deployment support:
- **Documentation**: https://docs.hrms.com
- **Issues**: https://github.com/your-org/hrms/issues
- **Email**: support@hrms.com
- **Slack**: #hrms-support
