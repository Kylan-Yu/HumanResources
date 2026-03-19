# Employee Service 联调说明

## 📋 模块概述

`employee-service` 是员工管理服务模块，提供完整的员工档案管理功能，包括员工基础信息、任职信息、家庭成员、教育经历、工作经历、附件和异动记录管理。

## 🚀 快速启动

### 1. 数据库初始化

```sql
-- 执行表结构创建
SOURCE F:/java/myself/human/database/tables/04_employee_complete_tables.sql;

-- 执行初始化数据
SOURCE F:/java/myself/human/database/data/employee_init_data.sql;
```

### 2. 后端服务启动

```bash
# 进入employee-service目录
cd F:/java/myself/human/backend/hrms-employee

# 启动服务
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8083`

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

### 员工管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/employees/page` | 分页查询员工 | employee:list |
| POST | `/employees` | 创建员工 | employee:add |
| PUT | `/employees/{id}` | 更新员工 | employee:edit |
| DELETE | `/employees/{id}` | 删除员工 | employee:remove |
| GET | `/employees/{id}` | 获取员工详情 | employee:detail |
| PUT | `/employees/{id}/status` | 更新员工状态 | employee:status |

### 员工扩展信息接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/employees/{id}/family` | 获取家庭成员 | employee:detail |
| POST | `/employees/{id}/family` | 添加家庭成员 | employee:edit |
| PUT | `/employee-family/{id}` | 更新家庭成员 | employee:edit |
| DELETE | `/employee-family/{id}` | 删除家庭成员 | employee:edit |
| GET | `/employees/{id}/education` | 获取教育经历 | employee:detail |
| POST | `/employees/{id}/education` | 添加教育经历 | employee:edit |
| PUT | `/employee-education/{id}` | 更新教育经历 | employee:edit |
| DELETE | `/employee-education/{id}` | 删除教育经历 | employee:edit |
| GET | `/employees/{id}/work-experiences` | 获取工作经历 | employee:detail |
| POST | `/employees/{id}/work-experiences` | 添加工作经历 | employee:edit |
| PUT | `/employee-work-experiences/{id}` | 更新工作经历 | employee:edit |
| DELETE | `/employee-work-experiences/{id}` | 删除工作经历 | employee:edit |
| GET | `/employees/{id}/attachments` | 获取附件列表 | employee:detail |
| POST | `/employees/{id}/attachments` | 上传附件 | employee:edit |
| DELETE | `/employee-attachments/{id}` | 删除附件 | employee:edit |
| GET | `/employees/{id}/change-records` | 获取异动记录 | employee:detail |

## 🎯 业务流程

### 1. 员工创建流程

1. **前端调用**：`POST /employees`
2. **数据验证**：身份证号、手机号、邮箱唯一性检查
3. **员工编号生成**：自动生成 `EMP{YYYYMMDD}{序列号}` 格式
4. **基础信息保存**：保存到 `hr_employee` 表
5. **任职信息保存**：如果有任职信息，保存到 `hr_employee_job` 表
6. **返回结果**：返回员工ID

### 2. 员工状态变更流程

1. **前端调用**：`PUT /employees/{id}/status`
2. **状态验证**：检查状态值有效性
3. **异动记录创建**：记录状态变更历史
4. **状态更新**：更新员工状态
5. **返回结果**：返回操作结果

### 3. 员工详情查询流程

1. **前端调用**：`GET /employees/{id}`
2. **基础信息查询**：从 `hr_employee` 表获取
3. **关联信息查询**：
   - 任职信息：`hr_employee_job`
   - 家庭成员：`hr_employee_family`
   - 教育经历：`hr_employee_education`
   - 工作经历：`hr_employee_work_experience`
   - 附件：`hr_employee_attachment`
   - 异动记录：`hr_employee_change_record`
4. **数据聚合**：组合成完整的员工VO
5. **返回结果**：返回员工详情VO

## 🔧 配置说明

### application.yml 配置

```yaml
server:
  port: 8083

spring:
  application:
    name: hrms-employee
  
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
  type-aliases-package: com.hrms.employee.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 🧪 测试用例

### 1. 创建员工测试

```bash
curl -X POST http://localhost:8083/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "测试员工",
    "gender": 1,
    "mobile": "13800138000",
    "industryType": "company",
    "jobInfo": {
      "orgId": 1,
      "deptId": 1,
      "positionId": 1,
      "employeeType": "formal",
      "employmentType": "fulltime",
      "entryDate": "2024-01-01",
      "isMainJob": 1
    }
  }'
```

### 2. 查询员工列表测试

```bash
curl -X GET "http://localhost:8083/employees/page?pageNum=1&pageSize=10&name=张" \
  -H "Authorization: Bearer {token}"
```

### 3. 更新员工状态测试

```bash
curl -X PUT http://localhost:8083/employees/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"status": 2}'
```

## 🐛 常见问题

### 1. 员工编号生成失败

**问题**：员工编号生成失败或重复
**解决方案**：
- 检查数据库连接
- 确认 `hr_employee` 表存在
- 检查 `generateEmployeeNo` SQL语句

### 2. 任职信息关联失败

**问题**：员工创建时任职信息保存失败
**解决方案**：
- 检查组织、部门、岗位是否存在
- 确认外键约束正确
- 验证权限配置

### 3. 前端页面无法访问

**问题**：前端页面显示404
**解决方案**：
- 检查路由配置
- 确认组件路径正确
- 检查权限配置

### 4. 数据验证失败

**问题**：创建/更新员工时数据验证失败
**解决方案**：
- 检查必填字段
- 验证数据格式（身份证号、手机号等）
- 确认唯一性约束

## 📊 性能优化

### 1. 数据库优化

- 为常用查询字段添加索引：`employee_no`、`name`、`mobile`
- 使用分页查询减少数据传输量
- 考虑读写分离

### 2. 缓存策略

- 员工基础信息缓存
- 组织架构信息缓存
- 权限信息缓存

### 3. 前端优化

- 使用懒加载减少初始加载时间
- 实现虚拟滚动处理大量数据
- 使用防抖优化搜索功能

## 🔐 权限配置

### 角色权限映射

| 角色 | 权限 |
|------|------|
| admin | employee:* |
| hr_manager | employee:list, employee:add, employee:edit, employee:detail |
| hr_staff | employee:list, employee:detail |
| employee | employee:detail(own) |

### 菜单权限

```json
{
  "employee": {
    "name": "员工管理",
    "icon": "TeamOutlined",
    "children": [
      {
        "name": "员工列表",
        "path": "/employee/list",
        "permission": "employee:list"
      }
    ]
  }
}
```

## 📱 前端页面说明

### 页面结构

```
src/pages/employee/
├── list/           # 员工列表页
├── create/         # 新增员工页
├── edit/           # 编辑员工页
├── detail/         # 员工详情页
└── components/     # 公共组件
    ├── JobForm.tsx
    ├── FamilyTable.tsx
    ├── EducationTable.tsx
    ├── WorkExperienceTable.tsx
    └── AttachmentTable.tsx
```

### 页面功能

1. **员工列表页**：支持搜索、分页、状态管理、批量操作
2. **新增员工页**：表单验证、任职信息录入、实时预览
3. **编辑员工页**：信息修改、状态更新、历史记录
4. **员工详情页**：Tab展示、关联信息查看、操作记录

## 🔄 与其他模块集成

### 1. 与 auth-service 集成

- 使用统一的认证和授权
- 共享用户权限体系
- 统一的异常处理

### 2. 与 org-service 集成

- 组织架构数据同步
- 部门信息关联
- 岗位职级映射

### 3. 与 future modules 集成

- 考勤模块关联员工ID
- 薪资模块使用员工信息
- 培训模块关联员工档案

## 📈 监控和日志

### 1. 关键指标监控

- 员工创建/更新频率
- 接口响应时间
- 数据库查询性能
- 错误率统计

### 2. 日志记录

- 员工信息变更日志
- 权限操作日志
- 系统异常日志
- 性能监控日志

## 🎉 完成状态

✅ **已完成功能**
- [x] 员工基础信息管理
- [x] 任职信息管理
- [x] 家庭成员管理
- [x] 教育经历管理
- [x] 工作经历管理
- [x] 附件管理
- [x] 异动记录管理
- [x] 前端页面开发
- [x] API接口开发
- [x] 数据库设计
- [x] 权限控制

✅ **可运行状态**
- 后端服务可正常启动
- 前端页面可正常访问
- 数据库表结构完整
- 示例数据可正常使用

🚀 **下一步开发**
- attendance-service（考勤管理）
- payroll-service（薪资管理）
- training-service（培训管理）
- workflow-service（工作流引擎）
