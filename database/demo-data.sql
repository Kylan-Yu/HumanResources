-- HRMS Demo Data Script
-- This script creates sample data for demonstration purposes

-- Insert Demo Users
INSERT INTO `users` (`id`, `username`, `password`, `email`, `full_name`, `status`, `created_at`, `updated_at`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', 'admin@hrms.com', 'System Administrator', 'ACTIVE', NOW(), NOW()),
(2, 'john.smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', 'john.smith@hrms.com', 'John Smith', 'ACTIVE', NOW(), NOW()),
(3, 'sarah.jones', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', 'sarah.jones@hrms.com', 'Sarah Jones', 'ACTIVE', NOW(), NOW()),
(4, 'mike.wilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', 'mike.wilson@hrms.com', 'Mike Wilson', 'ACTIVE', NOW(), NOW()),
(5, 'emily.brown', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', 'emily.brown@hrms.com', 'Emily Brown', 'ACTIVE', NOW(), NOW());

-- Insert Roles
INSERT INTO `roles` (`id`, `name`, `code`, `description`, `created_at`, `updated_at`) VALUES
(1, 'Super Administrator', 'SUPER_ADMIN', 'Full system access', NOW(), NOW()),
(2, 'HR Manager', 'HR_MANAGER', 'HR management access', NOW(), NOW()),
(3, 'Department Manager', 'DEPT_MANAGER', 'Department management access', NOW(), NOW()),
(4, 'Employee', 'EMPLOYEE', 'Basic employee access', NOW(), NOW());

-- Assign Roles to Users
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 4);

-- Insert Departments
INSERT INTO `departments` (`id`, `name`, `code`, `description`, `parent_id`, `manager_id`, `created_at`, `updated_at`) VALUES
(1, 'Engineering', 'ENG', 'Software development and IT infrastructure', NULL, 2, NOW(), NOW()),
(2, 'Frontend Team', 'FE', 'Frontend development team', 1, 3, NOW(), NOW()),
(3, 'Backend Team', 'BE', 'Backend development team', 1, 4, NOW(), NOW()),
(4, 'Human Resources', 'HR', 'HR management and operations', NULL, 2, NOW(), NOW()),
(5, 'Marketing', 'MKT', 'Marketing and sales', NULL, 5, NOW(), NOW());

-- Insert Positions
INSERT INTO `positions` (`id`, `title`, `code`, `description`, `department_id`, `level`, `min_salary`, `max_salary`, `created_at`, `updated_at`) VALUES
(1, 'Software Engineer', 'SE', 'Develops software applications', 1, 3, 60000, 90000, NOW(), NOW()),
(2, 'Senior Software Engineer', 'SSE', 'Senior developer with team leadership', 1, 4, 80000, 120000, NOW(), NOW()),
(3, 'Frontend Developer', 'FE_DEV', 'Frontend web developer', 2, 3, 65000, 95000, NOW(), NOW()),
(4, 'Backend Developer', 'BE_DEV', 'Backend server developer', 3, 3, 70000, 100000, NOW(), NOW()),
(5, 'HR Manager', 'HR_MGR', 'Human Resources Manager', 4, 5, 90000, 130000, NOW(), NOW()),
(6, 'Marketing Manager', 'MKT_MGR', 'Marketing Manager', 5, 5, 85000, 120000, NOW(), NOW());

-- Insert Employees
INSERT INTO `employees` (`id`, `employee_id`, `first_name`, `last_name`, `email`, `phone`, `department_id`, `position_id`, `manager_id`, `hire_date`, `salary`, `status`, `created_at`, `updated_at`) VALUES
(1, 'EMP001', 'John', 'Smith', 'john.smith@hrms.com', '+1-555-0101', 1, 2, NULL, '2022-01-15', 95000.00, 'ACTIVE', NOW(), NOW()),
(2, 'EMP002', 'Sarah', 'Jones', 'sarah.jones@hrms.com', '+1-555-0102', 2, 3, 1, '2022-03-20', 75000.00, 'ACTIVE', NOW(), NOW()),
(3, 'EMP003', 'Mike', 'Wilson', 'mike.wilson@hrms.com', '+1-555-0103', 3, 4, 1, '2022-06-10', 80000.00, 'ACTIVE', NOW(), NOW()),
(4, 'EMP004', 'Emily', 'Brown', 'emily.brown@hrms.com', '+1-555-0104', 5, 6, NULL, '2022-09-05', 92000.00, 'ACTIVE', NOW(), NOW()),
(5, 'EMP005', 'David', 'Lee', 'david.lee@hrms.com', '+1-555-0105', 2, 3, 2, '2023-01-15', 70000.00, 'ACTIVE', NOW(), NOW()),
(6, 'EMP006', 'Lisa', 'Wang', 'lisa.wang@hrms.com', '+1-555-0106', 3, 4, 3, '2023-02-20', 75000.00, 'ACTIVE', NOW(), NOW()),
(7, 'EMP007', 'James', 'Taylor', 'james.taylor@hrms.com', '+1-555-0107', 1, 1, 1, '2023-04-10', 65000.00, 'PROBATION', NOW(), NOW()),
(8, 'EMP008', 'Anna', 'Martinez', 'anna.martinez@hrms.com', '+1-555-0108', 4, 5, 1, '2023-05-15', 88000.00, 'ACTIVE', NOW(), NOW());

-- Insert Employee Addresses
INSERT INTO `employee_addresses` (`employee_id`, `address_type`, `street`, `city`, `state`, `zip_code`, `country`, `is_primary`) VALUES
(1, 'HOME', '123 Tech Street', 'San Francisco', 'CA', '94105', 'USA', 1),
(2, 'HOME', '456 Web Avenue', 'San Francisco', 'CA', '94107', 'USA', 1),
(3, 'HOME', '789 Server Road', 'San Francisco', 'CA', '94108', 'USA', 1),
(4, 'HOME', '321 Market Street', 'San Francisco', 'CA', '94102', 'USA', 1),
(5, 'HOME', '654 Frontend Lane', 'Oakland', 'CA', '94607', 'USA', 1),
(6, 'HOME', '987 Backend Drive', 'Oakland', 'CA', '94608', 'USA', 1),
(7, 'HOME', '147 Engineering Blvd', 'San Jose', 'CA', '95110', 'USA', 1),
(8, 'HOME', '258 HR Plaza', 'San Francisco', 'CA', '94103', 'USA', 1);

-- Insert Job Postings
INSERT INTO `job_postings` (`id`, `title`, `description`, `requirements`, `department_id`, `location`, `employment_type`, `min_salary`, `max_salary`, `status`, `posted_date`, `application_deadline`, `created_at`, `updated_at`) VALUES
(1, 'Senior Java Developer', 'We are looking for an experienced Senior Java Developer to join our growing team. You will be responsible for designing and developing high-performance, scalable applications using Java and Spring Boot.', 
'5+ years of Java development experience\nStrong knowledge of Spring Boot and Spring Cloud\nExperience with microservices architecture\nProficiency in RESTful API design\nExperience with MySQL and Redis\nKnowledge of Docker and Kubernetes', 
1, 'San Francisco, CA', 'FULL_TIME', 90000, 130000, 'ACTIVE', '2024-01-01', '2024-02-15', NOW(), NOW()),
(2, 'Frontend Developer', 'Join our frontend team to build amazing user interfaces using React and TypeScript. You will work closely with designers and backend developers to create exceptional user experiences.', 
'3+ years of React development experience\nStrong knowledge of TypeScript\nExperience with modern CSS and responsive design\nFamiliarity with state management libraries (Redux, Zustand)\nUnderstanding of web performance optimization', 
2, 'San Francisco, CA', 'FULL_TIME', 75000, 110000, 'ACTIVE', '2024-01-05', '2024-02-20', NOW(), NOW()),
(3, 'HR Business Partner', 'We are seeking an experienced HR Business Partner to support our engineering organization. You will be responsible for talent management, employee relations, and organizational development.', 
'5+ years of HR experience\nExperience in tech industry preferred\nStrong knowledge of employment laws and regulations\nExcellent communication and interpersonal skills\nExperience with HRIS systems', 
4, 'San Francisco, CA', 'FULL_TIME', 80000, 120000, 'ACTIVE', '2024-01-10', '2024-02-25', NOW(), NOW());

-- Insert Applicants
INSERT INTO `applicants` (`id`, `first_name`, `last_name`, `email`, `phone`, `job_posting_id`, `resume_url`, `status`, `application_date`, `created_at`, `updated_at`) VALUES
(1, 'Robert', 'Johnson', 'robert.johnson@email.com', '+1-555-0201', 1, 'https://storage.hrms.com/resumes/robert_johnson.pdf', 'SCREENING', '2024-01-08', NOW(), NOW()),
(2, 'Maria', 'Garcia', 'maria.garcia@email.com', '+1-555-0202', 1, 'https://storage.hrms.com/resumes/maria_garcia.pdf', 'INTERVIEW_SCHEDULED', '2024-01-09', NOW(), NOW()),
(3, 'Kevin', 'Chen', 'kevin.chen@email.com', '+1-555-0203', 2, 'https://storage.hrms.com/resumes/kevin_chen.pdf', 'SCREENING', '2024-01-12', NOW(), NOW()),
(4, 'Jennifer', 'White', 'jennifer.white@email.com', '+1-555-0204', 2, 'https://storage.hrms.com/resumes/jennifer_white.pdf', 'TECHNICAL_ASSESSMENT', '2024-01-13', NOW(), NOW()),
(5, 'Thomas', 'Anderson', 'thomas.anderson@email.com', '+1-555-0205', 3, 'https://storage.hrms.com/resumes/thomas_anderson.pdf', 'SCREENING', '2024-01-15', NOW(), NOW());

-- Insert Performance Reviews
INSERT INTO `performance_reviews` (`id`, `employee_id`, `reviewer_id`, `review_period`, `goals_achievement`, `competencies_rating`, `overall_rating`, `strengths`, `areas_for_improvement`, `comments`, `status`, `created_at`, `updated_at`) VALUES
(1, 2, 1, '2023-Q4', 85, 4.2, 4.0, 'Excellent technical skills, great team player', 'Time management, delegation', 'Sarah has shown great improvement this quarter. Her technical contributions have been valuable to the team.', 'COMPLETED', NOW(), NOW()),
(2, 3, 1, '2023-Q4', 78, 3.8, 3.5, 'Strong backend development skills', 'Communication with frontend team', 'Mike has delivered solid work this quarter. Need to improve cross-team collaboration.', 'COMPLETED', NOW(), NOW()),
(3, 5, 2, '2023-Q4', 82, 4.0, 4.0, 'Creative problem solver, good attention to detail', 'Could take more initiative', 'David has been a great addition to the frontend team. His attention to detail has improved our code quality.', 'COMPLETED', NOW(), NOW());

-- Insert Performance Goals
INSERT INTO `performance_goals` (`id`, `employee_id`, `title`, `description`, `target_date`, `status`, `progress`, `created_at`, `updated_at`) VALUES
(1, 2, 'Complete React Certification', 'Obtain React developer certification by end of Q1', '2024-03-31', 'IN_PROGRESS', 75, NOW(), NOW()),
(2, 3, 'Optimize Database Queries', 'Reduce average query response time by 30%', '2024-02-28', 'IN_PROGRESS', 60, NOW(), NOW()),
(3, 5, 'Improve Code Coverage', 'Increase unit test coverage to 85%', '2024-03-15', 'NOT_STARTED', 0, NOW(), NOW());

-- Insert Payroll Records
INSERT INTO `payroll_records` (`id`, `employee_id`, `pay_period`, `basic_salary`, `overtime_hours`, `overtime_pay`, `bonuses`, `tax_deduction`, `insurance_deduction`, `other_deductions`, `net_salary`, `status`, `payment_date`, `created_at`, `updated_at`) VALUES
(1, 1, '2024-01', 7916.67, 10, 288.46, 1000.00, 1583.33, 316.67, 50.00, 7255.13, 'PAID', '2024-01-31', NOW(), NOW()),
(2, 2, '2024-01', 6250.00, 8, 184.62, 500.00, 1250.00, 250.00, 30.00, 5404.62, 'PAID', '2024-01-31', NOW(), NOW()),
(3, 3, '2024-01', 6666.67, 15, 432.69, 750.00, 1333.33, 266.67, 40.00, 6209.36, 'PAID', '2024-01-31', NOW(), NOW()),
(4, 4, '2024-01', 7666.67, 5, 144.23, 800.00, 1533.33, 306.67, 45.00, 6725.90, 'PAID', '2024-01-31', NOW(), NOW()),
(5, 5, '2024-01', 5833.33, 12, 263.08, 600.00, 1166.67, 233.33, 35.00, 5261.41, 'PAID', '2024-01-31', NOW(), NOW());

-- Insert Training Programs
INSERT INTO `training_programs` (`id`, `title`, `description`, `duration_days`, `cost`, `category`, `status`, `created_at`, `updated_at`) VALUES
(1, 'Leadership Excellence', 'Develop essential leadership skills for managers', 5, 2500.00, 'LEADERSHIP', 'ACTIVE', NOW(), NOW()),
(2, 'Advanced React Patterns', 'Master advanced React patterns and best practices', 3, 1500.00, 'TECHNICAL', 'ACTIVE', NOW(), NOW()),
(3, 'Effective Communication', 'Improve workplace communication skills', 2, 800.00, 'SOFT_SKILLS', 'ACTIVE', NOW(), NOW()),
(4, 'Project Management Fundamentals', 'Learn the basics of project management', 4, 1800.00, 'PROJECT_MANAGEMENT', 'ACTIVE', NOW(), NOW());

-- Insert Employee Training
INSERT INTO `employee_training` (`id`, `employee_id`, `training_program_id`, `enrollment_date`, `completion_date`, `status`, `score`, `created_at`, `updated_at`) VALUES
(1, 1, 1, '2024-01-10', '2024-01-15', 'COMPLETED', 92, NOW(), NOW()),
(2, 2, 2, '2024-01-15', NULL, 'IN_PROGRESS', NULL, NOW(), NOW()),
(3, 3, 3, '2024-01-20', NULL, 'ENROLLED', NULL, NOW(), NOW()),
(4, 5, 2, '2024-01-12', '2024-01-18', 'COMPLETED', 88, NOW(), NOW());

-- Insert Leave Requests
INSERT INTO `leave_requests` (`id`, `employee_id`, `leave_type`, `start_date`, `end_date`, `days_count`, `reason`, `status`, `approved_by`, `created_at`, `updated_at`) VALUES
(1, 2, 'VACATION', '2024-02-10', '2024-02-14', 5, 'Family vacation to Hawaii', 'APPROVED', 1, NOW(), NOW()),
(2, 3, 'SICK', '2024-01-25', '2024-01-26', 2, 'Medical appointment', 'APPROVED', 1, NOW(), NOW()),
(3, 5, 'PERSONAL', '2024-03-05', '2024-03-05', 1, 'Personal matters', 'PENDING', NULL, NOW(), NOW());

-- Insert Notifications
INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 1, 'New Job Application', 'Robert Johnson has applied for Senior Java Developer position', 'INFO', 0, NOW()),
(2, 2, 'Performance Review Scheduled', 'Your Q1 2024 performance review has been scheduled for March 15, 2024', 'INFO', 0, NOW()),
(3, 3, 'Leave Request Approved', 'Your sick leave request for Jan 25-26 has been approved', 'SUCCESS', 0, NOW()),
(4, 5, 'Training Enrollment Confirmed', 'You have been enrolled in Advanced React Patterns training', 'INFO', 0, NOW());

-- Insert System Configuration
INSERT INTO `system_config` (`key`, `value`, `description`, `category`, `created_at`, `updated_at`) VALUES
('company.name', 'HRMS Demo Company', 'Company name displayed in the system', 'GENERAL', NOW(), NOW()),
('company.logo', 'https://storage.hrms.com/logo.png', 'Company logo URL', 'GENERAL', NOW(), NOW()),
('leave.max_vacation_days', '20', 'Maximum vacation days per year', 'LEAVE_POLICY', NOW(), NOW()),
('leave.max_sick_days', '10', 'Maximum sick days per year', 'LEAVE_POLICY', NOW(), NOW()),
('performance.review_frequency', 'QUARTERLY', 'Performance review frequency', 'PERFORMANCE', NOW(), NOW()),
('payroll.overtime_rate', '1.5', 'Overtime pay rate multiplier', 'PAYROLL', NOW(), NOW());

-- Insert Audit Logs
INSERT INTO `audit_logs` (`id`, `user_id`, `action`, `entity_type`, `entity_id`, `old_values`, `new_values`, `ip_address`, `user_agent`, `created_at`) VALUES
(1, 1, 'CREATE', 'EMPLOYEE', 5, NULL, '{"employeeId": "EMP005", "firstName": "David", "lastName": "Lee"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW()),
(2, 2, 'UPDATE', 'EMPLOYEE', 3, '{"salary": 75000}', '{"salary": 80000}', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', NOW()),
(3, 1, 'APPROVE', 'LEAVE_REQUEST', 1, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW());

-- Update statistics
UPDATE `departments` SET `employee_count` = (
    SELECT COUNT(*) FROM `employees` WHERE `department_id` = `departments`.`id` AND `status` = 'ACTIVE'
);

-- Create indexes for better performance
CREATE INDEX `idx_employees_department` ON `employees`(`department_id`);
CREATE INDEX `idx_employees_manager` ON `employees`(`manager_id`);
CREATE INDEX `idx_employees_status` ON `employees`(`status`);
CREATE INDEX `idx_payroll_employee_period` ON `payroll_records`(`employee_id`, `pay_period`);
CREATE INDEX `idx_performance_reviews_employee` ON `performance_reviews`(`employee_id`, `review_period`);
CREATE INDEX `idx_applicants_job_status` ON `applicants`(`job_posting_id`, `status`);
CREATE INDEX `idx_audit_logs_user_action` ON `audit_logs`(`user_id`, `action`, `created_at`);
