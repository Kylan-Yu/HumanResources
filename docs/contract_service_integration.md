# Contract Service 联调说明

## 📋 模块概述

`contract-service` 是劳动合同管理中心，提供完整的合同全生命周期管理功能，包括合同新增、编辑、查询、删除、详情查看、状态管理、合同续签、变更记录和到期预警。

## 🚀 快速启动

### 1. 数据库初始化

```sql
-- 执行表结构创建
SOURCE F:/java/myself/human/database/tables/05_contract_tables.sql;
```

### 2. 后端服务启动

```bash
# 进入contract-service目录
cd F:/java/myself/human/backend/hrms-contract

# 启动服务
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8084`

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

### 合同管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/contracts/page` | 分页查询合同 | contract:list |
| GET | `/contracts/{id}` | 获取合同详情 | contract:detail |
| POST | `/contracts` | 创建合同 | contract:add |
| PUT | `/contracts/{id}` | 更新合同 | contract:edit |
| DELETE | `/contracts/{id}` | 删除合同 | contract:remove |
| PUT | `/contracts/{id}/status` | 更新合同状态 | contract:status |
| POST | `/contracts/{id}/renew` | 续签合同 | contract:renew |
| GET | `/contracts/expire-warning/page` | 查询即将到期的合同 | contract:warning |

### 合同记录接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/contracts/{id}/records` | 获取合同变更记录 | contract:detail |

## 🎯 业务流程

### 1. 合同创建流程

1. **前端调用**：`POST /contracts`
2. **数据验证**：员工存在性检查、日期有效性验证
3. **合同编号生成**：自动生成 `CT/HT{YYYYMMDD}{序列号}` 格式
4. **合同信息保存**：保存到 `hr_contract` 表
5. **变更记录创建**：记录创建操作到 `hr_contract_record` 表
6. **返回结果**：返回合同ID

### 2. 合同续签流程

1. **前端调用**：`POST /contracts/{id}/renew`
2. **合同验证**：检查合同是否存在且状态为生效
3. **信息更新**：更新结束日期、签署日期、续签次数
4. **状态重置**：将合同状态重置为生效
5. **变更记录创建**：记录续签操作
6. **返回结果**：返回操作结果

### 3. 合同状态管理流程

1. **前端调用**：`PUT /contracts/{id}/status`
2. **状态验证**：检查状态值有效性
3. **状态更新**：更新合同状态
4. **变更记录创建**：记录状态变更
5. **返回结果**：返回操作结果

### 4. 合同详情查询流程

1. **前端调用**：`GET /contracts/{id}`
2. **基础信息查询**：从 `hr_contract` 表获取
3. **变更记录查询**：从 `hr_contract_record` 表获取
4. **状态检查**：如果已过期自动标记为EXPIRED
5. **数据聚合**：组合成完整的合同VO
6. **返回结果**：返回合同详情VO

## 🔧 配置说明

### application.yml 配置

```yaml
server:
  port: 8084

spring:
  application:
    name: hrms-contract
  
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
  type-aliases-package: com.hrms.contract.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 🧪 测试用例

### 1. 创建合同测试

```bash
curl -X POST http://localhost:8084/contracts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "employeeId": 1,
    "contractType": "LABOR_CONTRACT",
    "contractSubject": "标准劳动合同",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "signDate": "2024-01-01",
    "industryType": "company",
    "remark": "标准劳动合同"
  }'
```

### 2. 查询合同列表测试

```bash
curl -X GET "http://localhost:8084/contracts/page?pageNum=1&pageSize=10&contractType=LABOR_CONTRACT" \
  -H "Authorization: Bearer {token}"
```

### 3. 续签合同测试

```bash
curl -X POST http://localhost:8084/contracts/1/renew \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "newEndDate": "2025-12-31",
    "newSignDate": "2024-12-31",
    "renewReason": "合同续签",
    "remark": "续签一年"
  }'
```

### 4. 查询到期预警测试

```bash
curl -X GET "http://localhost:8084/contracts/expire-warning/page?warningDays=30" \
  -H "Authorization: Bearer {token}"
```

## 🐛 常见问题

### 1. 合同编号生成失败

**问题**：合同编号生成失败或重复
**解决方案**：
- 检查数据库连接
- 确认 `hr_contract` 表存在
- 检查 `generateContractNo` SQL语句

### 2. 合同续签失败

**问题**：续签时提示合同不存在或状态不正确
**解决方案**：
- 检查合同ID是否正确
- 确认合同状态为ACTIVE
- 验证新结束日期晚于当前结束日期

### 3. 前端页面无法访问

**问题**：前端页面显示404
**解决方案**：
- 检查路由配置
- 确认组件路径正确
- 检查权限配置

### 4. 数据验证失败

**问题**：创建/更新合同时数据验证失败
**解决方案**：
- 检查必填字段
- 验证日期格式和逻辑
- 确认员工ID存在

## 📊 性能优化

### 1. 数据库优化

- 为常用查询字段添加索引：`employee_id`、`contract_no`、`contract_status`、`end_date`
- 使用分页查询减少数据传输量
- 考虑读写分离

### 2. 缓存策略

- 合同基础信息缓存
- 合同类型枚举缓存
- 员工基础信息缓存

### 3. 前端优化

- 使用懒加载减少初始加载时间
- 实现虚拟滚动处理大量数据
- 使用防抖优化搜索功能

## 🔐 权限设计

### 角色权限映射

| 角色 | 权限 |
|------|------|
| admin | contract:* |
| hr_manager | contract:list, contract:add, contract:edit, contract:detail, contract:status, contract:renew, contract:warning |
| hr_staff | contract:list, contract:detail |
| manager | contract:list, contract:detail, contract:renew |
| employee | contract:detail(own) |

### 菜单权限

```json
{
  "contract": {
    "name": "合同管理",
    "icon": "FileTextOutlined",
    "children": [
      {
        "name": "合同列表",
        "path": "/contract/list",
        "permission": "contract:list"
      },
      {
        "name": "到期预警",
        "path": "/contract/expire-warning",
        "permission": "contract:warning"
      }
    ]
  }
}
```

## 📱 前端页面说明

### 页面结构

```
src/pages/contract/
├── list/                    # 合同列表页
├── detail/                  # 合同详情页
├── expireWarning/           # 到期预警页
└── components/              # 公共组件
    └── ContractRenewModal.tsx
```

### 页面功能

1. **合同列表页**：
   - 支持多条件筛选（员工、合同编号、类型、状态、日期范围）
   - 支持分页、排序
   - 支持新增、编辑、删除、详情、续签、状态变更操作
   - 状态标签可视化

2. **合同详情页**：
   - 展示合同基础信息
   - 展示合同变更记录
   - 支持编辑操作

3. **到期预警页**：
   - 统计卡片展示预警数量
   - 支持按预警天数筛选
   - 剩余天数可视化
   - 支持跳转详情

4. **续签模态框**：
   - 表单验证（新结束日期必须晚于当前日期）
   - 显示当前合同信息
   - 支持续签原因和备注输入

## 🔄 与其他模块集成

### 1. 与 employee-service 集成

- 员工基础信息获取
- 员工状态验证
- 员工组织架构信息

### 2. 与 org-service 集成

- 组织架构数据
- 部门信息关联

### 3. 与 future modules 集成

- payroll-service：薪资计算参考合同信息
- attendance-service：考勤管理参考合同状态
- recruit-service：新员工自动生成合同

## 📈 监控和日志

### 1. 业务监控

- 合同创建/续签频率
- 合同到期预警数量
- 接口响应时间
- 错误率统计

### 2. 日志记录

- 合同信息变更日志
- 续签操作日志
- 状态变更日志
- 系统异常日志

## 🎉 完成状态

✅ **已完成功能**
- [x] 合同基础信息管理
- [x] 合同状态管理
- [x] 合同续签功能
- [x] 合同变更记录
- [x] 合同到期预警
- [x] 前端页面开发
- [x] API接口开发
- [x] 数据库设计
- [x] 权限控制

✅ **可运行状态**
- 后端服务可正常启动
- 前端页面可正常访问
- 数据库表结构完整
- 示例数据可正常使用

## 🚀 下一步开发

**Recruit Service** 开发清单已准备完成，包含：
- 招聘需求管理
- 招聘职位管理
- 候选人管理
- 面试管理
- Offer管理
- 候选人转员工

---

**Contract Service 模块已完成，可以开始进行 Recruit Service 的开发！** 🎉
