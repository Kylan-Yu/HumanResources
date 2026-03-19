-- auth-service 完整表结构

-- 用户表（更新版）
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '岗位ID',
  `industry_type` varchar(20) DEFAULT 'company' COMMENT '行业类型：company-企业，hospital-医院',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_email` (`email`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表（更新版）
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `data_scope` varchar(20) DEFAULT 'all' COMMENT '数据权限：all-全部，org-本组织，dept-本部门，self-本人',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 菜单表（更新版）
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `route_path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component_path` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `sort_no` int DEFAULT 0 COMMENT '排序',
  `visible` tinyint DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 用户角色关联表（更新版）
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色菜单关联表（更新版）
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 登录日志表（更新版）
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT '' COMMENT '用户名',
  `login_ip` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  `login_status` tinyint DEFAULT 0 COMMENT '登录状态：0-成功，1-失败',
  `message` varchar(255) DEFAULT '' COMMENT '提示消息',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
