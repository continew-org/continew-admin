-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `title` varchar(50) NOT NULL COMMENT '菜单标题',
    `parent_id` bigint(20) UNSIGNED DEFAULT 0 COMMENT '上级菜单ID',
    `type` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '菜单类型（1：目录，2：菜单，3：按钮）',
    `path` varchar(512) DEFAULT NULL COMMENT '路由地址',
    `name` varchar(50) DEFAULT NULL COMMENT '组件名称',
    `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
    `icon` varchar(255) DEFAULT NULL COMMENT '菜单图标',
    `is_external` bit(1) DEFAULT b'0' COMMENT '是否外链',
    `is_cache` bit(1) DEFAULT b'0' COMMENT '是否缓存',
    `is_hidden` bit(1) DEFAULT b'0' COMMENT '是否隐藏',
    `permission` varchar(255) DEFAULT NULL COMMENT '权限标识',
    `sort` int(11) UNSIGNED DEFAULT 999 COMMENT '菜单排序',
    `status` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '状态（1：启用，2：禁用）',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_title_parent_id`(`title`, `parent_id`) USING BTREE,
    INDEX `idx_parent_id`(`parent_id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `name` varchar(50) NOT NULL COMMENT '部门名称',
    `parent_id` bigint(20) UNSIGNED DEFAULT 0 COMMENT '上级部门ID',
    `ancestors` varchar(512) DEFAULT '' COMMENT '祖级列表',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `sort` int(11) UNSIGNED DEFAULT 999 COMMENT '部门排序',
    `status` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '状态（1：启用，2：禁用）',
    `type` tinyint(1) UNSIGNED DEFAULT 2 COMMENT '类型（1：系统内置，2：自定义）',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_name_parent_id`(`name`, `parent_id`) USING BTREE,
    INDEX `idx_parent_id`(`parent_id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `name` varchar(50) NOT NULL COMMENT '角色名称',
    `code` varchar(50) NOT NULL COMMENT '角色编码',
    `data_scope` tinyint(1) DEFAULT 4 COMMENT '数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `sort` int(11) UNSIGNED DEFAULT 999 COMMENT '角色排序',
    `status` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '状态（1：启用，2：禁用）',
    `type` tinyint(1) UNSIGNED DEFAULT 2 COMMENT '类型（1：系统内置，2：自定义）',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_name`(`name`) USING BTREE,
    UNIQUE INDEX `uk_code`(`code`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
    `menu_id` bigint(20) UNSIGNED NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

CREATE TABLE IF NOT EXISTS `sys_role_dept` (
    `role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
    `dept_id` bigint(20) UNSIGNED NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和部门关联表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `gender` tinyint(1) UNSIGNED DEFAULT 0 COMMENT '性别（0：未知，1：男，2：女）',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(50) DEFAULT NULL COMMENT '手机号码',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '状态（1：启用，2：禁用）',
    `type` tinyint(1) UNSIGNED DEFAULT 2 COMMENT '类型（1：系统内置，2：自定义）',
    `pwd_reset_time` datetime DEFAULT NULL COMMENT '最后一次修改密码时间',
    `dept_id` bigint(20) UNSIGNED NOT NULL COMMENT '部门ID',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_username`(`username`) USING BTREE,
    UNIQUE INDEX `uk_email`(`email`) USING BTREE,
    UNIQUE INDEX `uk_phone`(`phone`) USING BTREE,
    INDEX `idx_dept_id`(`dept_id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `description` varchar(255) NOT NULL COMMENT '日志描述',
    `module` varchar(50) NOT NULL COMMENT '所属模块',
    `request_url` varchar(512) NOT NULL COMMENT '请求URL',
    `request_method` varchar(10) NOT NULL COMMENT '请求方式',
    `request_headers` text DEFAULT NULL COMMENT '请求头',
    `request_body` text DEFAULT NULL COMMENT '请求体',
    `status_code` int(11) UNSIGNED NOT NULL COMMENT '状态码',
    `response_headers` text DEFAULT NULL COMMENT '响应头',
    `response_body` mediumtext DEFAULT NULL COMMENT '响应体',
    `elapsed_time` bigint(20) UNSIGNED NOT NULL COMMENT '请求耗时（ms）',
    `status` tinyint(1) UNSIGNED DEFAULT 1 COMMENT '操作状态（1：成功，2：失败）',
    `client_ip` varchar(100) DEFAULT NULL COMMENT '客户端IP',
    `location` varchar(255) DEFAULT NULL COMMENT 'IP归属地',
    `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
    `error_msg` text DEFAULT NULL COMMENT '错误信息',
    `exception_detail` mediumtext DEFAULT NULL COMMENT '异常详情',
    `create_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';
