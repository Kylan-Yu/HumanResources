# Payroll Service 联调说明

## 📋 模块概述

`payroll-service` 是薪资管理中心，提供完整的薪资核算、发放和管理功能，包括薪资标准管理、薪资记录管理、社保公积金管理、个税计算和薪资报表等功能。

## 🚀 快速启动

### 1. 数据库初始化

```sql
-- 执行表结构创建
SOURCE F:/java/myself/human/database/tables/07_payroll_tables.sql;
```

### 2. 后端服务启动

```bash
# 进入payroll-service目录
cd F:/java/myself/human/backend/hrms-payroll

# 启动服务
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8086`

### 3. 前端服务启动

```bash
# 进入前端目录
cd F:/java/myself/human/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端访问：`http://localhost:3000`

## 🔗 API 接口说明

### 薪资标准管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/payroll-standards/page` | 分页查询薪资标准 | payroll:standard:list |
| GET | `/payroll-standards/{id}` | 获取薪资标准详情 | payroll:standard:detail |
| POST | `/payroll-standards` | 创建薪资标准 | payroll:standard:add |
| PUT | `/payroll-standards/{id}` | 更新薪资标准 | payroll:standard:edit |
| DELETE | `/payroll-standards/{id}` | 删除薪资标准 | payroll:standard:remove |
| PUT | `/payroll-standards/{id}/status` | 更新薪资标准状态 | payroll:standard:status |
| GET | `/payroll-standards/employee/{employeeId}` | 根据员工ID查询适用的薪资标准 | payroll:standard:detail |

### 薪资记录管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/payroll-records/page` | 分页查询薪资记录 | payroll:record:list |
| GET | `/payroll-records/{id}` | 获取薪资记录详情 | payroll:record:detail |
| POST | `/payroll-records/calculate` | 薪资核算 | payroll:record:calculate |
| PUT | `/payroll-records/{id}/status` | 更新薪资记录状态 | payroll:record:status |
| GET | `/payroll-records/export` | 导出薪资记录 | payroll:record:export |

### 社保公积金管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/social-fund-configs/page` | 分页查询社保公积金配置 | payroll:social:list |
| GET | `/social-fund-configs/{id}` | 获取社保公积金配置详情 | payroll:social:detail |
| POST | `/social-fund-configs` | 创建社保公积金配置 | payroll:social:add |
| PUT | `/social-fund-configs/{id}` | 更新社保公积金配置 | payroll:social:edit |
| DELETE | `/social-fund-configs/{id}` | 删除社保公积金配置 | payroll:social:remove |

### 个税管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/tax-configs/page` | 分页查询个税配置 | payroll:tax:list |
| GET | `/tax-configs/{id}` | 获取个税配置详情 | payroll:tax:detail |
| POST | `/tax-configs` | 创建个税配置 | payroll:tax:add |
| PUT | `/tax-configs/{id}` | 更新个税配置 | payroll:tax:edit |
| DELETE | `/tax-configs/{id}` | 删除个税配置 | payroll:tax:remove |
| POST | `/tax/calculate` | 个税计算 | payroll:tax:calculate |

## 🎯 业务流程

### 1. 薪资标准创建流程

1. **前端调用**：`POST /payroll-standards`
2. **数据验证**：组织、部门、岗位存在性检查，薪资数值有效性验证
3. **标准信息保存**：保存到 `hr_payroll_standard` 表
4. **状态管理**：支持启用、禁用状态
5. **返回结果**：返回标准ID

### 2. 薪资核算流程

1. **前端调用**：`POST /payroll-records/calculate`
2. **员工筛选**：根据条件筛选需要核算的员工
3. **薪资标准匹配**：根据员工信息匹配适用的薪资标准
4. **应发薪资计算**：基本薪资 + 绩效薪资 + 各项津贴补贴
5. **社保公积金计算**：根据社保公积金配置计算个人部分
6. **个税计算**：根据个税配置计算个人所得税
7. **实发薪资计算**：应发薪资 - 各项扣款
8. **记录保存**：保存到 `hr_payroll_record` 表
9. **返回结果**：返回核算结果

### 3. 薪资发放流程

1. **状态更新**：将薪资记录状态更新为已发放
2. **发放记录**：记录发放时间和操作人
3. **通知发送**：发送薪资发放通知
4. **报表生成**：生成薪资发放报表

### 4. 个税计算流程

1. **应纳税所得额计算**：应发薪资 - 社保公积金个人部分 - 起征点
2. **税率匹配**：根据个税配置匹配适用税率
3. **个税计算**：应纳税所得额 × 税率 - 速算扣除数
4. **结果返回**：返回个税金额

## 🔧 配置说明

### application.yml 配置

```yaml
server:
  port: 8086

spring:
  application:
    name: hrms-payroll
  
  datasource:
    url: jdbc:mysql://localhost:3306/hrms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yml

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.hrms.payroll.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 🧪 测试用例

### 1. 创建薪资标准测试

```bash
curl -X POST http://localhost:8086/payroll-standards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "standardName": "Java开发工程师薪资标准",
    "orgId": 1,
    "deptId": 1,
    "positionId": 1,
    "gradeLevel": "P3",
    "baseSalary": 15000.00,
    "performanceSalary": 5000.00,
    "positionAllowance": 2000.00,
    "mealAllowance": 500.00,
    "transportAllowance": 800.00,
    "communicationAllowance": 200.00,
    "housingAllowance": 1500.00,
    "otherAllowance": 0.00,
    "status": "ACTIVE",
    "industryType": "company",
    "remark": "Java开发工程师标准薪资"
  }'
```

### 2. 查询薪资标准列表测试

```bash
curl -X GET "http://localhost:8086/payroll-standards/page?pageNum=1&pageSize=10&status=ACTIVE" \
  -H "Authorization: Bearer {token}"
```

### 3. 薪资核算测试

```bash
curl -X POST http://localhost:8086/payroll-records/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "employeeIds": [1, 2, 3],
    "payrollPeriod": "2024-03",
    "periodStartDate": "2024-03-01",
    "periodEndDate": "2024-03-31",
    "payDate": "2024-04-10"
  }'
```

### 4. 个税计算测试

```bash
curl -X POST http://localhost:8086/tax/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "grossSalary": 25000.00,
    "socialPersonal": 800.00,
    "fundPersonal": 600.00,
    "deduction": 5000.00
  }'
```

## 🐛 常见问题

### 1. 薪资标准匹配失败

**问题**：无法为员工匹配到合适的薪资标准
**解决方案**：
- 检查员工信息完整性
- 确认薪资标准配置正确
- 验证匹配规则逻辑

### 2. 薪资核算失败

**问题**：薪资核算过程中出现错误
**解决方案**：
- 检查薪资标准数据完整性
- 确认社保公积金配置
- 验证个税配置正确性

### 3. 个税计算错误

**问题**：个税计算结果不正确
**解决方案**：
- 检查个税配置表数据
- 确认税率区间设置
- 验证速算扣除数

### 4. 薪资发放失败

**问题**：薪资发放操作失败
**解决方案**：
- 检查薪资记录状态
- 确认发放权限
- 验证发放时间

## 📊 性能优化

### 1. 数据库优化

- 为常用查询字段添加索引：`employee_id`、`payroll_period`、`pay_date`、`status`
- 使用分页查询减少数据传输量
- 考虑读写分离

### 2. 缓存策略

- 薪资标准信息缓存
- 社保公积金配置缓存
- 个税配置缓存
- 员工基础信息缓存

### 3. 批量处理

- 批量薪资核算
- 批量薪资发放
- 批量数据导入导出

## 🔐 权限设计

### 角色权限映射

| 角色 | 权限 |
|------|------|
| admin | payroll:* |
| hr_manager | payroll:standard:*, payroll:record:*, payroll:social:*, payroll:tax:* |
| hr_staff | payroll:standard:list, payroll:standard:detail, payroll:record:list, payroll:record:detail |
| manager | payroll:record:list, payroll:record:detail, payroll:record:export |
| employee | payroll:record:detail(own) |

### 菜单权限

```json
{
  "payroll": {
    "name": "薪资管理",
    "icon": "DollarOutlined",
    "children": [
      {
        "name": "薪资标准",
        "path": "/payroll/standard",
        "permission": "payroll:standard:list"
      },
      {
        "name": "薪资记录",
        "path": "/payroll/record",
        "permission": "payroll:record:list"
      },
      {
        "name": "社保公积金",
        "path": "/payroll/social",
        "permission": "payroll:social:list"
      },
      {
        "name": "个税管理",
        "path": "/payroll/tax",
        "permission": "payroll:tax:list"
      },
      {
        "name": "薪资报表",
        "path": "/payroll/report",
        "permission": "payroll:report:list"
      }
    ]
  }
}
```

## 📱 前端页面说明

### 页面结构

```
src/pages/payroll/
├── standard/               # 薪资标准管理
│   ├── list/               # 标准列表页
│   ├── detail/             # 标准详情页
│   └── create/             # 标准创建页
├── record/                 # 薪资记录管理
│   ├── list/               # 记录列表页
│   ├── detail/             # 记录详情页
│   └── calculate/          # 薪资核算页
├── social/                 # 社保公积金管理
│   ├── list/               # 配置列表页
│   └── detail/             # 配置详情页
├── tax/                    # 个税管理
│   ├── list/               # 配置列表页
│   └── detail/             # 配置详情页
└── report/                 # 薪资报表
    ├── salary/             # 薪资报表
    ├── tax/                # 个税报表
    └── social/             # 社保公积金报表
```

### 页面功能

1. **薪资标准管理**：
   - 标准创建、编辑、删除
   - 标准状态管理（启用、禁用）
   - 薪资结构设置
   - 薪资标准匹配规则

2. **薪资记录管理**：
   - 薪资核算功能
   - 薪资发放管理
   - 薪资记录查询
   - 薪资明细展示

3. **社保公积金管理**：
   - 社保公积金配置
   - 缴费基数设置
   - 缴费比例管理
   - 行业差异化配置

4. **个税管理**：
   - 个税政策配置
   - 税率区间设置
   - 速算扣除数管理
   - 个税计算器

5. **薪资报表**：
   - 薪资汇总报表
   - 薪资明细报表
   - 个税统计报表
   - 社保公积金报表

## 🔄 与其他模块集成

### 1. 与 org-service 集成

- 组织架构数据获取
- 部门信息验证
- 岗位信息关联

### 2. 与 employee-service 集成

- 员工基础信息获取
- 员工职级信息
- 员工状态验证

### 3. 与 attendance-service 集成

- 考勤数据获取
- 出勤天数计算
- 考勤扣款计算

### 4. 与 contract-service 集成

- 合同信息获取
- 薪资信息验证
- 合同变更同步

## 📈 监控和日志

### 1. 业务监控

- 薪资核算频率和成功率
- 薪资发放及时性
- 个税计算准确性
- 社保公积金缴纳情况

### 2. 日志记录

- 薪资标准变更日志
- 薪资核算日志
- 薪资发放日志
- 个税计算日志

## 🎉 完成状态

✅ **已完成功能**
- [x] 薪资标准管理
- [x] 薪资记录管理
- [x] 社保公积金管理
- [x] 个税管理
- [x] 前端页面开发
- [x] API接口开发
- [x] 数据库设计
- [x] 权限控制

✅ **可运行状态**
- 后端服务可正常启动
- 前端页面可正常访问
- 数据库表结构完整
- 示例数据可正常使用

## 🚀 MVP功能总结

**HRMS系统核心模块已完成开发：**

1. **auth-service** - 认证授权中心 ✅
2. **org-service** - 组织架构管理 ✅
3. **employee-service** - 员工管理 ✅
4. **contract-service** - 合同管理 ✅
5. **recruit-service** - 招聘管理 ✅
6. **payroll-service** - 薪资管理 ✅

**系统特性：**
- 支持企业和医院两种行业类型
- 完整的权限管理体系
- 标准的RESTful API设计
- 前后端分离架构
- 微服务架构支持
- 完整的CRUD操作
- 分页查询和过滤
- 数据验证和业务规则
- 逻辑删除支持

---

**HRMS系统MVP开发完成！** 🎉
