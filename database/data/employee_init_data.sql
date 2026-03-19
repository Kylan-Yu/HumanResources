-- 员工模块初始化数据
-- 包含员工、任职信息、家庭成员、教育经历、工作经历、附件、异动记录的示例数据

-- 插入员工基础信息
INSERT INTO hr_employee (employee_no, name, gender, birthday, id_card_no, mobile, email, marital_status, nationality, domicile_address, current_address, employee_status, industry_type, ext_json, remark, created_by, created_time, updated_by, updated_time, deleted) VALUES
('EMP202603180001', '张三', 1, '1990-05-15', '110101199005150001', '13800138001', 'zhangsan@example.com', 2, '中国', '北京市朝阳区', '北京市海淀区', 1, 'company', NULL, '系统管理员', 1, NOW(), 1, NOW(), 0),
('EMP202603180002', '李四', 2, '1992-08-20', '110101199208200002', '13800138002', 'lisi@example.com', 1, '中国', '上海市浦东新区', '上海市黄浦区', 1, 'company', NULL, '技术负责人', 1, NOW(), 1, NOW(), 0),
('EMP202603180003', '王五', 1, '1988-12-10', '110101198812100003', '13800138003', 'wangwu@example.com', 2, '中国', '广州市天河区', '广州市越秀区', 1, 'hospital', '{"licenseNumber": "DOC001", "department": "内科"}', '主治医师', 1, NOW(), 1, NOW(), 0),
('EMP202603180004', '赵六', 2, '1995-03-25', '110101199503250004', '13800138004', 'zhaoliu@example.com', 1, '中国', '深圳市南山区', '深圳市福田区', 1, 'hospital', '{"licenseNumber": "NUR001", "department": "外科"}', '护士长', 1, NOW(), 1, NOW(), 0);

-- 插入员工任职信息
INSERT INTO hr_employee_job (employee_id, org_id, dept_id, position_id, rank_id, leader_id, employee_type, employment_type, entry_date, regular_date, work_location, is_main_job, status, created_time, updated_time, deleted) VALUES
(1, 1, 1, 1, 1, NULL, 'formal', 'fulltime', '2020-01-15', '2020-04-15', '北京市海淀区', 1, 1, NOW(), NOW(), 0),
(2, 1, 2, 2, 2, 1, 'formal', 'fulltime', '2020-03-01', '2020-06-01', '北京市海淀区', 1, 1, NOW(), NOW(), 0),
(3, 2, 3, 3, 3, NULL, 'formal', 'fulltime', '2019-07-01', '2019-10-01', '广州市天河区', 1, 1, NOW(), NOW(), 0),
(4, 2, 4, 4, 4, 3, 'formal', 'fulltime', '2021-02-01', '2021-05-01', '广州市天河区', 1, 1, NOW(), NOW(), 0);

-- 插入员工家庭成员
INSERT INTO hr_employee_family (employee_id, name, relationship, gender, birthday, id_card_no, mobile, occupation, work_unit, remark, created_time, updated_time, deleted) VALUES
(1, '张父', 'father', 1, '1960-01-01', '110101196001010001', '13800138005', '退休工人', '北京机械厂', '父亲信息', NOW(), NOW(), 0),
(1, '张母', 'mother', 2, '1962-03-15', '110101196203150002', '13800138006', '退休教师', '北京第一小学', '母亲信息', NOW(), NOW(), 0),
(2, '李妻', 'spouse', 2, '1993-07-20', '110101199307200005', '13800138007', '会计师', '北京会计师事务所', '配偶信息', NOW(), NOW(), 0);

-- 插入员工教育经历
INSERT INTO hr_employee_education (employee_id, school_name, education_level, major, start_date, end_date, is_highest, degree_type, graduation_certificate, remark, created_time, updated_time, deleted) VALUES
(1, '清华大学', 'bachelor', '计算机科学与技术', '2008-09-01', '2012-06-30', 1, 'bachelor', '2012001', '本科最高学历', NOW(), NOW(), 0),
(1, '北京大学', 'master', '软件工程', '2012-09-01', '2015-06-30', 0, 'master', '2015001', '硕士学历', NOW(), NOW(), 0),
(2, '复旦大学', 'bachelor', '电子信息工程', '2010-09-01', '2014-06-30', 1, 'bachelor', '2014001', '本科最高学历', NOW(), NOW(), 0),
(3, '中山大学医学院', 'bachelor', '临床医学', '2013-09-01', '2018-06-30', 1, 'bachelor', '2018001', '医学学士', NOW(), NOW(), 0);

-- 插入员工工作经历
INSERT INTO hr_employee_work_experience (employee_id, company_name, position, start_date, end_date, job_description, resign_reason, witness, witness_mobile, remark, created_time, updated_time, deleted) VALUES
(1, '腾讯科技', '软件工程师', '2015-07-01', '2018-12-31', '负责后端系统开发，参与多个大型项目', '职业发展', '王经理', '13900139001', '第一份工作', NOW(), NOW(), 0),
(1, '阿里巴巴', '高级软件工程师', '2019-01-01', '2019-12-31', '负责电商平台核心模块开发', '寻求更大挑战', '李总监', '13900139002', '第二份工作', NOW(), NOW(), 0),
(2, '华为技术', '硬件工程师', '2014-08-01', '2017-11-30', '负责通信设备硬件设计与测试', '职业转型', '陈主管', '13900139003', '硬件开发经验', NOW(), NOW(), 0),
(3, '广州第一人民医院', '实习医生', '2018-07-01', '2019-06-30', '内科轮转实习，积累临床经验', '完成实习', '王主任', '13900139004', '实习经历', NOW(), NOW(), 0);

-- 插入员工附件
INSERT INTO hr_employee_attachment (employee_id, attachment_type, file_name, file_path, file_size, file_type, upload_time, remark, created_time, updated_time, deleted) VALUES
(1, 'id_card', '张三身份证.jpg', '/uploads/employee/1/id_card.jpg', 1024000, 'image/jpeg', NOW(), '身份证扫描件', NOW(), NOW(), 0),
(1, 'diploma', '张三毕业证.jpg', '/uploads/employee/1/diploma.jpg', 2048000, 'image/jpeg', NOW(), '清华大学毕业证', NOW(), NOW(), 0),
(1, 'degree', '张三学位证.jpg', '/uploads/employee/1/degree.jpg', 1536000, 'image/jpeg', NOW(), '北京大学学位证', NOW(), NOW(), 0),
(1, 'contract', '张三劳动合同.pdf', '/uploads/employee/1/contract.pdf', 512000, 'application/pdf', NOW(), '劳动合同', NOW(), NOW(), 0),
(2, 'resume', '李四简历.pdf', '/uploads/employee/2/resume.pdf', 256000, 'application/pdf', NOW(), '个人简历', NOW(), NOW(), 0);

-- 插入员工异动记录
INSERT INTO hr_employee_change_record (employee_id, change_type, change_date, before_value, after_value, change_reason, approver_id, approve_time, remark, created_time, updated_time, deleted) VALUES
(1, 'entry', '2020-01-15', NULL, '{"position": "软件工程师", "salary": "15000"}', '新员工入职', 1, NOW(), '入职异动', NOW(), NOW(), 0),
(1, 'promotion', '2020-12-01', '{"position": "软件工程师", "level": "P6"}', '{"position": "高级软件工程师", "level": "P7"}', '年度晋升', 1, NOW(), '晋升异动', NOW(), NOW(), 0),
(2, 'transfer', '2021-01-01', '{"department": "研发部", "team": "后端组"}', '{"department": "技术部", "team": "架构组"}', '部门调整', 1, NOW(), '部门调动', NOW(), NOW(), 0),
(3, 'resign', '2022-06-30', '{"status": "在职", "position": "主治医师"}', '{"status": "离职", "position": "主治医师"}', '个人原因', 1, NOW(), '离职异动', NOW(), NOW(), 0);
