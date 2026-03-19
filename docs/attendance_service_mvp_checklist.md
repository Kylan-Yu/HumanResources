# Attendance Service MVP 开发清单

## 📋 模块概述

`attendance-service` 是考勤管理服务模块，提供完整的考勤管理功能，包括打卡记录、考勤统计、请假管理、加班管理等核心功能。

## 🎯 MVP 功能范围

### 1. 核心功能
- [x] 员工打卡（上班/下班）
- [x] 考勤记录查询
- [x] 考勤统计报表
- [x] 请假申请与审批
- [x] 加班申请与审批
- [x] 考勤异常处理

### 2. 扩展功能（MVP+）
- [ ] 排班管理
- [ ] 假期管理
- [ ] 考勤规则配置
- [ ] 移动端打卡
- [ ] 人脸识别打卡

## 🗄️ 数据库设计

### 核心表结构

```sql
-- 考勤记录表
CREATE TABLE hr_attendance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    check_in_time DATETIME COMMENT '上班打卡时间',
    check_out_time DATETIME COMMENT '下班打卡时间',
    work_hours DECIMAL(4,2) COMMENT '工作时长（小时）',
    overtime_hours DECIMAL(4,2) COMMENT '加班时长（小时）',
    attendance_status INT DEFAULT 1 COMMENT '考勤状态：1-正常，2-迟到，3-早退，4-旷工，5-请假',
    check_in_location VARCHAR(255) COMMENT '上班打卡地点',
    check_out_location VARCHAR(255) COMMENT '下班打卡地点',
    check_in_device VARCHAR(50) COMMENT '上班打卡设备',
    check_out_device VARCHAR(50) COMMENT '下班打卡设备',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_employee_date (employee_id, attendance_date),
    INDEX idx_attendance_date (attendance_date),
    FOREIGN KEY (employee_id) REFERENCES hr_employee(id)
);

-- 请假申请表
CREATE TABLE hr_leave_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    leave_type INT NOT NULL COMMENT '请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    leave_days DECIMAL(3,1) NOT NULL COMMENT '请假天数',
    reason VARCHAR(1000) NOT NULL COMMENT '请假原因',
    approver_id BIGINT COMMENT '审批人ID',
    approve_status INT DEFAULT 0 COMMENT '审批状态：0-待审批，1-已通过，2-已拒绝',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    attachment_path VARCHAR(500) COMMENT '附件路径',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_employee_leave (employee_id, leave_type),
    INDEX idx_approve_status (approve_status),
    FOREIGN KEY (employee_id) REFERENCES hr_employee(id),
    FOREIGN KEY (approver_id) REFERENCES hr_employee(id)
);

-- 加班申请表
CREATE TABLE hr_overtime_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    overtime_date DATE NOT NULL COMMENT '加班日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    overtime_hours DECIMAL(4,2) NOT NULL COMMENT '加班时长（小时）',
    overtime_type INT DEFAULT 1 COMMENT '加班类型：1-工作日加班，2-周末加班，3-节假日加班',
    reason VARCHAR(1000) NOT NULL COMMENT '加班原因',
    approver_id BIGINT COMMENT '审批人ID',
    approve_status INT DEFAULT 0 COMMENT '审批状态：0-待审批，1-已通过，2-已拒绝',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_employee_overtime (employee_id, overtime_date),
    INDEX idx_approve_status (approve_status),
    FOREIGN KEY (employee_id) REFERENCES hr_employee(id),
    FOREIGN KEY (approver_id) REFERENCES hr_employee(id)
);

-- 考勤规则表
CREATE TABLE hr_attendance_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    org_id BIGINT COMMENT '适用组织ID',
    dept_id BIGINT COMMENT '适用部门ID',
    work_start_time TIME NOT NULL COMMENT '上班时间',
    work_end_time TIME NOT NULL COMMENT '下班时间',
    late_tolerance INT DEFAULT 5 COMMENT '迟到宽限时间（分钟）',
    early_tolerance INT DEFAULT 5 COMMENT '早退宽限时间（分钟）',
    work_days VARCHAR(20) DEFAULT '1,2,3,4,5' COMMENT '工作日：1-周一，2-周二，...，7-周日',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    INDEX idx_org_dept (org_id, dept_id),
    FOREIGN KEY (org_id) REFERENCES hr_org(id),
    FOREIGN KEY (dept_id) REFERENCES hr_department(id)
);
```

## 🔧 后端开发任务

### 1. 实体类 (Entity)
- [ ] `AttendanceRecord` - 考勤记录实体
- [ ] `LeaveApplication` - 请假申请实体
- [ ] `OvertimeApplication` - 加班申请实体
- [ ] `AttendanceRule` - 考勤规则实体

### 2. DTO 类
- [ ] `AttendanceCheckInDTO` - 打卡DTO
- [ ] `AttendanceQueryDTO` - 考勤查询DTO
- [ ] `LeaveApplicationDTO` - 请假申请DTO
- [ ] `OvertimeApplicationDTO` - 加班申请DTO
- [ ] `AttendanceStatisticsDTO` - 考勤统计DTO

### 3. VO 类
- [ ] `AttendanceRecordVO` - 考勤记录VO
- [ ] `LeaveApplicationVO` - 请假申请VO
- [ ] `OvertimeApplicationVO` - 加班申请VO
- [ ] `AttendanceStatisticsVO` - 考勤统计VO
- [ ] `MonthlyAttendanceVO` - 月度考勤VO

### 4. Mapper 接口
- [ ] `AttendanceRecordMapper` - 考勤记录Mapper
- [ ] `LeaveApplicationMapper` - 请假申请Mapper
- [ ] `OvertimeApplicationMapper` - 加班申请Mapper
- [ ] `AttendanceRuleMapper` - 考勤规则Mapper

### 5. Service 接口与实现
- [ ] `AttendanceService` - 考勤服务接口
- [ ] `AttendanceServiceImpl` - 考勤服务实现
- [ ] `LeaveService` - 请假服务接口
- [ ] `LeaveServiceImpl` - 请假服务实现
- [ ] `OvertimeService` - 加班服务接口
- [ ] `OvertimeServiceImpl` - 加班服务实现

### 6. Controller 控制器
- [ ] `AttendanceController` - 考勤控制器
- [ ] `LeaveController` - 请假控制器
- [ ] `OvertimeController` - 加班控制器

### 7. 核心业务逻辑
- [ ] 员工打卡逻辑
- [ ] 考勤状态计算
- [ ] 工作时长计算
- [ ] 加班时长计算
- [ ] 请假天数计算
- [ ] 考勤统计报表生成

### 8. 工具类
- [ ] `AttendanceUtils` - 考勤工具类
- [ ] `DateUtils` - 日期工具类
- [ ] `LocationUtils` - 地理位置工具类

## 🎨 前端开发任务

### 1. API 接口
- [ ] `src/api/attendance.ts` - 考勤API
- [ ] `src/api/leave.ts` - 请假API
- [ ] `src/api/overtime.ts` - 加班API

### 2. 类型定义
- [ ] `src/types/attendance.ts` - 考勤类型定义

### 3. 页面组件
- [ ] `src/pages/attendance/dashboard/index.tsx` - 考勤仪表板
- [ ] `src/pages/attendance/record/index.tsx` - 考勤记录页
- [ ] `src/pages/attendance/checkin/index.tsx` - 打卡页面
- [ ] `src/pages/attendance/statistics/index.tsx` - 考勤统计页
- [ ] `src/pages/leave/application/index.tsx` - 请假申请页
- [ ] `src/pages/leave/approval/index.tsx` - 请假审批页
- [ ] `src/pages/overtime/application/index.tsx` - 加班申请页
- [ ] `src/pages/overtime/approval/index.tsx` - 加班审批页

### 4. 公共组件
- [ ] `src/components/AttendanceCalendar.tsx` - 考勤日历
- [ ] `src/components/CheckInButton.tsx` - 打卡按钮
- [ ] `src/components/AttendanceChart.tsx` - 考勤图表
- [ ] `src/components/LeaveForm.tsx` - 请假表单
- [ ] `src/components/OvertimeForm.tsx` - 加班表单

## 🔄 API 接口设计

### 考勤打卡接口
```java
// 打卡
POST /attendance/checkin
{
    "employeeId": 1,
    "checkType": "IN|OUT", // IN-上班打卡，OUT-下班打卡
    "location": "北京市海淀区",
    "device": "WEB|MOBILE"
}

// 获取今日考勤状态
GET /attendance/today/{employeeId}

// 考勤记录查询
GET /attendance/records/page
{
    "pageNum": 1,
    "pageSize": 10,
    "employeeId": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
}
```

### 请假管理接口
```java
// 请假申请
POST /leave/application
{
    "employeeId": 1,
    "leaveType": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-01-02",
    "reason": "事假"
}

// 请假审批
PUT /leave/approval/{id}
{
    "approveStatus": 1,
    "approveRemark": "同意"
}

// 请假记录查询
GET /leave/records/page
{
    "pageNum": 1,
    "pageSize": 10,
    "employeeId": 1,
    "approveStatus": 1
}
```

### 加班管理接口
```java
// 加班申请
POST /overtime/application
{
    "employeeId": 1,
    "overtimeDate": "2024-01-01",
    "startTime": "18:00",
    "endTime": "21:00",
    "overtimeType": 1,
    "reason": "项目紧急"
}

// 加班审批
PUT /overtime/approval/{id}
{
    "approveStatus": 1,
    "approveRemark": "同意"
}

// 加班记录查询
GET /overtime/records/page
{
    "pageNum": 1,
    "pageSize": 10,
    "employeeId": 1,
    "approveStatus": 1
}
```

## 📊 统计报表

### 1. 个人考勤统计
- [ ] 月度考勤汇总
- [ ] 考勤异常统计
- [ ] 加班时长统计
- [ ] 请假天数统计

### 2. 部门考勤统计
- [ ] 部门出勤率统计
- [ ] 部门考勤异常统计
- [ ] 部门加班统计

### 3. 公司考勤统计
- [ ] 整体出勤率
- [ ] 考勤异常分析
- [ ] 加班趋势分析

## 🔐 权限设计

### 角色权限
- **admin**: 考勤管理全部权限
- **hr_manager**: 考勤审批、统计权限
- **hr_staff**: 考勤记录查询权限
- **manager**: 部门考勤审批权限
- **employee**: 个人考勤打卡、申请权限

### 权限点
- `attendance:checkin` - 打卡权限
- `attendance:record` - 考勤记录查询
- `attendance:statistics` - 考勤统计
- `leave:apply` - 请假申请
- `leave:approve` - 请假审批
- `overtime:apply` - 加班申请
- `overtime:approve` - 加班审批

## 🧪 测试用例

### 1. 单元测试
- [ ] 打卡逻辑测试
- [ ] 考勤状态计算测试
- [ ] 工作时长计算测试
- [ ] 请假天数计算测试

### 2. 集成测试
- [ ] 考勤流程测试
- [ ] 请假流程测试
- [ ] 加班流程测试

### 3. 性能测试
- [ ] 大量打卡记录查询
- [ ] 统计报表生成性能

## 🚀 部署配置

### 1. 配置文件
- [ ] `application.yml` - 主配置文件
- [ ] `application-dev.yml` - 开发环境配置
- [ ] `application-prod.yml` - 生产环境配置

### 2. 依赖管理
- [ ] Spring Boot 3.x
- [ ] MyBatis-Plus
- [ ] MySQL 8.0
- [ ] Redis（缓存）

### 3. 服务注册
- [ ] Nacos 服务注册
- [ ] 配置中心集成
- [ ] 服务发现

## 📱 移动端扩展（MVP+）

### 1. 移动端API
- [ ] 移动端打卡接口
- [ ] 位置验证接口
- [ ] 推送通知接口

### 2. 移动端功能
- [ ] GPS定位打卡
- [ ] 人脸识别打卡
- [ ] 考勤提醒
- [ ] 请假移动审批

## 📈 监控与日志

### 1. 业务监控
- [ ] 打卡成功率监控
- [ ] 考勤异常告警
- [ ] 系统性能监控

### 2. 日志记录
- [ ] 打卡操作日志
- [ ] 审批操作日志
- [ ] 系统异常日志

## ⏰ 开发计划

### Phase 1: 核心功能（2周）
- [ ] 数据库表设计
- [ ] 实体类和基础CRUD
- [ ] 打卡功能实现
- [ ] 基础考勤记录查询

### Phase 2: 请假加班（1周）
- [ ] 请假申请和审批
- [ ] 加班申请和审批
- [ ] 相关页面开发

### Phase 3: 统计报表（1周）
- [ ] 考勤统计功能
- [ ] 报表页面开发
- [ ] 数据可视化

### Phase 4: 优化完善（1周）
- [ ] 性能优化
- [ ] 异常处理
- [ ] 测试和文档

## 🎯 验收标准

### 1. 功能完整性
- [ ] 所有MVP功能正常运行
- [ ] 业务流程完整闭环
- [ ] 异常情况处理完善

### 2. 性能指标
- [ ] 打卡响应时间 < 500ms
- [ ] 查询响应时间 < 1s
- [ ] 统计报表生成 < 3s

### 3. 用户体验
- [ ] 界面友好易用
- [ ] 操作流程顺畅
- [ ] 移动端适配良好

## 🔄 与其他模块集成

### 1. 与 employee-service 集成
- 员工基础信息获取
- 员工状态验证

### 2. 与 org-service 集成
- 组织架构数据
- 部门权限控制

### 3. 与 notification-service 集成
- 考勤提醒通知
- 审批结果通知

---

**开发优先级**: 高 → 中 → 低  
**预计完成时间**: 5周  
**负责人**: 开发团队  
**技术栈**: Spring Boot 3 + React 18 + MySQL 8.0
