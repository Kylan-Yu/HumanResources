# Recruit Service 联调说明

## 📋 模块概述

`recruit-service` 是招聘管理中心，提供完整的招聘流程管理功能，包括招聘需求管理、职位发布、候选人管理、面试安排、Offer发放和候选人转员工的全流程支持。

## 🚀 快速启动

### 1. 数据库初始化

```sql
-- 执行表结构创建
SOURCE F:/java/myself/human/database/tables/06_recruit_tables.sql;
```

### 2. 后端服务启动

```bash
# 进入recruit-service目录
cd F:/java/myself/human/backend/hrms-recruit

# 启动服务
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8085`

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

### 招聘需求管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/recruit-requirements/page` | 分页查询招聘需求 | recruit:requirement:list |
| GET | `/recruit-requirements/{id}` | 获取招聘需求详情 | recruit:requirement:detail |
| POST | `/recruit-requirements` | 创建招聘需求 | recruit:requirement:add |
| PUT | `/recruit-requirements/{id}` | 更新招聘需求 | recruit:requirement:edit |
| DELETE | `/recruit-requirements/{id}` | 删除招聘需求 | recruit:requirement:remove |
| PUT | `/recruit-requirements/{id}/status` | 更新招聘需求状态 | recruit:requirement:status |

### 招聘职位管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/recruit-positions/page` | 分页查询招聘职位 | recruit:position:list |
| GET | `/recruit-positions/{id}` | 获取招聘职位详情 | recruit:position:detail |
| POST | `/recruit-positions` | 创建招聘职位 | recruit:position:add |
| PUT | `/recruit-positions/{id}` | 更新招聘职位 | recruit:position:edit |
| DELETE | `/recruit-positions/{id}` | 删除招聘职位 | recruit:position:remove |
| PUT | `/recruit-positions/{id}/publish` | 发布/下线招聘职位 | recruit:position:publish |

### 候选人管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/candidates/page` | 分页查询候选人 | recruit:candidate:list |
| GET | `/candidates/{id}` | 获取候选人详情 | recruit:candidate:detail |
| POST | `/candidates` | 创建候选人 | recruit:candidate:add |
| PUT | `/candidates/{id}` | 更新候选人 | recruit:candidate:edit |
| DELETE | `/candidates/{id}` | 删除候选人 | recruit:candidate:remove |
| PUT | `/candidates/{id}/status` | 更新候选人状态 | recruit:candidate:status |

### 面试管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/interviews/page` | 分页查询面试记录 | recruit:interview:list |
| GET | `/interviews/{id}` | 获取面试详情 | recruit:interview:detail |
| POST | `/interviews` | 安排面试 | recruit:interview:add |
| PUT | `/interviews/{id}` | 更新面试记录 | recruit:interview:edit |
| DELETE | `/interviews/{id}` | 删除面试记录 | recruit:interview:remove |

### Offer管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/offers/page` | 分页查询Offer | recruit:offer:list |
| GET | `/offers/{id}` | 获取Offer详情 | recruit:offer:detail |
| POST | `/offers` | 发放Offer | recruit:offer:add |
| PUT | `/offers/{id}` | 更新Offer | recruit:offer:edit |
| DELETE | `/offers/{id}` | 撤销Offer | recruit:offer:remove |
| PUT | `/offers/{id}/status` | 更新Offer状态 | recruit:offer:status |

## 🎯 业务流程

### 1. 招聘需求创建流程

1. **前端调用**：`POST /recruit-requirements`
2. **数据验证**：组织、部门、岗位存在性检查，日期有效性验证
3. **需求编号生成**：自动生成 `REQ/HREQ{YYYYMMDD}{序列号}` 格式
4. **需求信息保存**：保存到 `hr_recruit_requirement` 表
5. **返回结果**：返回需求ID

### 2. 招聘职位发布流程

1. **前端调用**：`POST /recruit-positions`
2. **需求验证**：检查需求是否存在且状态为开放
3. **职位信息保存**：保存到 `hr_recruit_position` 表
4. **发布状态管理**：支持草稿、已发布、下线状态
5. **返回结果**：返回职位ID

### 3. 候选人管理流程

1. **前端调用**：`POST /candidates`
2. **候选人编号生成**：自动生成 `CAN{YYYYMMDD}{序列号}` 格式
3. **信息保存**：保存到 `hr_candidate` 表
4. **状态管理**：新建、筛选、面试中、待发Offer、已发Offer、已接受、已拒绝、已入职
5. **返回结果**：返回候选人ID

### 4. 面试安排流程

1. **前端调用**：`POST /interviews`
2. **候选人验证**：检查候选人是否存在
3. **面试轮次管理**：自动计算面试轮次
4. **面试官安排**：记录面试官信息
5. **面试反馈记录**：记录面试评分和反馈
6. **返回结果**：返回面试ID

### 5. Offer发放流程

1. **前端调用**：`POST /offers`
2. **候选人验证**：检查候选人状态是否允许发放Offer
3. **Offer编号生成**：自动生成 `OFFER{YYYYMMDD}{序列号}` 格式
4. **薪资确认**：记录最终薪资金额
5. **入职日期确认**：记录入职日期
6. **返回结果**：返回OfferID

### 6. 候选人转员工流程

1. **Offer接受确认**：候选人接受Offer
2. **员工信息生成**：根据候选人信息生成员工档案
3. **合同创建**：自动创建劳动合同
4. **入职手续办理**：完成入职相关手续
5. **状态更新**：更新候选人状态为已入职

## 🔧 配置说明

### application.yml 配置

```yaml
server:
  port: 8085

spring:
  application:
    name: hrms-recruit
  
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
  type-aliases-package: com.hrms.recruit.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 🧪 测试用例

### 1. 创建招聘需求测试

```bash
curl -X POST http://localhost:8085/recruit-requirements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "title": "Java开发工程师招聘",
    "orgId": 1,
    "deptId": 1,
    "positionId": 1,
    "headcount": 2,
    "urgencyLevel": "HIGH",
    "expectedEntryDate": "2024-04-01",
    "reason": "业务扩展需要增加Java开发人员",
    "industryType": "company",
    "remark": "急需有经验的Java开发人员"
  }'
```

### 2. 查询招聘需求列表测试

```bash
curl -X GET "http://localhost:8085/recruit-requirements/page?pageNum=1&pageSize=10&requirementStatus=OPEN" \
  -H "Authorization: Bearer {token}"
```

### 3. 创建候选人测试

```bash
curl -X POST http://localhost:8085/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "张三",
    "gender": "MALE",
    "mobile": "13800138001",
    "email": "zhangsan@email.com",
    "sourceChannel": "ONLINE",
    "applyPositionId": 1,
    "currentCompany": "ABC科技公司",
    "currentPosition": "Java开发工程师",
    "expectedSalary": 18000,
    "industryType": "company",
    "remark": "有3年Java开发经验"
  }'
```

### 4. 安排面试测试

```bash
curl -X POST http://localhost:8085/interviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "candidateId": 1,
    "interviewRound": 1,
    "interviewerId": 1,
    "interviewerName": "面试官",
    "interviewTime": "2024-03-20T14:00:00",
    "interviewType": "TECHNICAL",
    "score": 85,
    "result": "PASS",
    "feedback": "技术能力较强，符合要求"
  }'
```

### 5. 发放Offer测试

```bash
curl -X POST http://localhost:8085/offers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "candidateId": 1,
    "positionId": 1,
    "salaryAmount": 18000,
    "entryDate": "2024-04-01",
    "remark": "标准Offer"
  }'
```

## 🐛 常见问题

### 1. 招聘需求编号生成失败

**问题**：需求编号生成失败或重复
**解决方案**：
- 检查数据库连接
- 确认 `hr_recruit_requirement` 表存在
- 检查 `generateRequirementNo` SQL语句

### 2. 候选人状态流转失败

**问题**：候选人状态无法正常流转
**解决方案**：
- 检查状态值是否正确
- 确认状态流转规则
- 验证业务逻辑

### 3. 面试安排冲突

**问题**：同一时间安排多个面试
**解决方案**：
- 检查面试时间冲突
- 验证面试官可用性
- 实现时间冲突检测

### 4. Offer发放失败

**问题**：Offer无法发放或状态异常
**解决方案**：
- 检查候选人状态
- 确认薪资范围合理
- 验证入职日期有效性

## 📊 性能优化

### 1. 数据库优化

- 为常用查询字段添加索引：`requirement_no`、`candidate_no`、`mobile`、`candidate_status`
- 使用分页查询减少数据传输量
- 考虑读写分离

### 2. 缓存策略

- 招聘需求基础信息缓存
- 职位信息缓存
- 组织架构信息缓存

### 3. 前端优化

- 使用懒加载减少初始加载时间
- 实现虚拟滚动处理大量数据
- 使用防抖优化搜索功能

## 🔐 权限设计

### 角色权限映射

| 角色 | 权限 |
|------|------|
| admin | recruit:* |
| hr_manager | recruit:requirement:*, recruit:position:*, recruit:candidate:*, recruit:interview:*, recruit:offer:* |
| hr_staff | recruit:requirement:list, recruit:requirement:detail, recruit:candidate:list, recruit:candidate:detail |
| manager | recruit:requirement:list, recruit:requirement:detail, recruit:candidate:list, recruit:candidate:detail |
| interviewer | recruit:interview:list, recruit:interview:detail, recruit:interview:add, recruit:interview:edit |

### 菜单权限

```json
{
  "recruit": {
    "name": "招聘管理",
    "icon": "TeamOutlined",
    "children": [
      {
        "name": "招聘需求",
        "path": "/recruit/requirement",
        "permission": "recruit:requirement:list"
      },
      {
        "name": "招聘职位",
        "path": "/recruit/position",
        "permission": "recruit:position:list"
      },
      {
        "name": "候选人管理",
        "path": "/recruit/candidate",
        "permission": "recruit:candidate:list"
      },
      {
        "name": "面试管理",
        "path": "/recruit/interview",
        "permission": "recruit:interview:list"
      },
      {
        "name": "Offer管理",
        "path": "/recruit/offer",
        "permission": "recruit:offer:list"
      }
    ]
  }
}
```

## 📱 前端页面说明

### 页面结构

```
src/pages/recruit/
├── requirement/             # 招聘需求管理
│   ├── list/               # 需求列表页
│   ├── detail/             # 需求详情页
│   └── create/             # 需求创建页
├── position/               # 招聘职位管理
│   ├── list/               # 职位列表页
│   └── detail/             # 职位详情页
├── candidate/              # 候选人管理
│   ├── list/               # 候选人列表页
│   ├── detail/             # 候选人详情页
│   └── profile/            # 候选人档案页
├── interview/              # 面试管理
│   ├── list/               # 面试列表页
│   ├── schedule/           # 面试安排页
│   └── feedback/           # 面试反馈页
└── offer/                  # Offer管理
    ├── list/               # Offer列表页
    └── detail/             # Offer详情页
```

### 页面功能

1. **招聘需求管理**：
   - 需求创建、编辑、删除
   - 需求状态管理（草稿、开放、关闭、取消）
   - 需求统计和报表
   - 紧急程度标识

2. **招聘职位管理**：
   - 职位发布和下线
   - 职位详情展示
   - 薪资范围管理
   - 职位状态跟踪

3. **候选人管理**：
   - 候选人信息录入
   - 简历上传和管理
   - 候选人状态流转
   - 候选人搜索和筛选

4. **面试管理**：
   - 面试安排和调度
   - 面试反馈记录
   - 面试评分管理
   - 面试统计报表

5. **Offer管理**：
   - Offer发放和跟踪
   - Offer状态管理
   - 薪资谈判记录
   - 入职日期管理

## 🔄 与其他模块集成

### 1. 与 org-service 集成

- 组织架构数据获取
- 部门信息验证
- 岗位信息关联

### 2. 与 employee-service 集成

- 候选人转员工
- 员工基础信息继承
- 员工档案创建

### 3. 与 contract-service 集成

- 自动生成劳动合同
- 合同信息关联
- 入职手续办理

### 4. 与 payroll-service 集成

- 薪资信息传递
- 薪资核算参考
- 薪资结构设置

## 📈 监控和日志

### 1. 业务监控

- 招聘需求创建/关闭频率
- 候选人转化率
- 面试通过率
- Offer接受率
- 平均招聘周期

### 2. 日志记录

- 招聘需求变更日志
- 候选人状态变更日志
- 面试安排日志
- Offer发放日志
- 系统异常日志

## 🎉 完成状态

✅ **已完成功能**
- [x] 招聘需求管理
- [x] 招聘职位管理
- [x] 候选人管理
- [x] 面试管理
- [x] Offer管理
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

**Payroll Service** 开发清单已准备完成，包含：
- 薪资标准管理
- 薪资核算
- 薪资发放
- 薪资报表
- 社保公积金管理
- 个税计算

---

**Recruit Service 模块已完成，可以开始进行 Payroll Service 的开发！** 🎉
