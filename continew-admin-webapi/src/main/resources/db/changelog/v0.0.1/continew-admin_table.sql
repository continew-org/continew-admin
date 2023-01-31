-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_dept`  (
    `dept_id` bigint(20) unsigned AUTO_INCREMENT COMMENT '部门ID',
    `dept_name` varchar(255) NOT NULL COMMENT '部门名称',
    `parent_id` bigint(20) unsigned DEFAULT 0 COMMENT '上级部门ID',
    `dept_sort` int(11) unsigned DEFAULT 999 COMMENT '部门排序',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) unsigned DEFAULT 1 COMMENT '状态（1启用 2禁用）',
    `create_user` bigint(20) unsigned NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) unsigned NOT NULL COMMENT '修改人',
    `update_time` datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`dept_id`) USING BTREE,
    INDEX `idx_parent_id`(`parent_id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `sys_user`  (
    `user_id` bigint(20) unsigned AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(255) NOT NULL COMMENT '用户名',
    `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `gender` tinyint(1) unsigned DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    `phone` varchar(255) DEFAULT NULL COMMENT '手机号码',
    `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) unsigned DEFAULT 1 COMMENT '状态（1启用 2禁用）',
    `pwd_reset_time` datetime DEFAULT NULL COMMENT '最后一次修改密码的时间',
    `create_user` bigint(20) unsigned NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) unsigned NOT NULL COMMENT '修改人',
    `update_time` datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`user_id`) USING BTREE,
    UNIQUE INDEX `uk_username`(`username`) USING BTREE,
    UNIQUE INDEX `uk_email`(`email`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_log` (
    `log_id` bigint(20) unsigned AUTO_INCREMENT COMMENT '日志ID',
    `description` varchar(255) NOT NULL COMMENT '日志描述',
    `module` varchar(255) NOT NULL COMMENT '所属模块',
    `request_url` varchar(512) NOT NULL COMMENT '请求URL',
    `request_method` varchar(10) NOT NULL COMMENT '请求方式',
    `request_headers` text DEFAULT NULL COMMENT '请求头',
    `request_body` text DEFAULT NULL COMMENT '请求体',
    `status_code` int(11) unsigned NOT NULL COMMENT '状态码',
    `response_headers` text DEFAULT NULL COMMENT '响应头',
    `response_body` mediumtext DEFAULT NULL COMMENT '响应体',
    `elapsed_time` bigint(20) unsigned NOT NULL COMMENT '请求耗时（ms）',
    `status` tinyint(1) unsigned DEFAULT 1 COMMENT '操作状态（1成功 2失败）',
    `client_ip` varchar(255) DEFAULT NULL COMMENT '客户端IP',
    `location` varchar(512) DEFAULT NULL COMMENT 'IP归属地',
    `browser` varchar(255) DEFAULT NULL COMMENT '浏览器',
    `error_msg` text DEFAULT NULL COMMENT '错误信息',
    `exception_detail` mediumtext DEFAULT NULL COMMENT '异常详情',
    `create_user` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`log_id`) USING BTREE,
    INDEX `idx_createUser`(`create_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';
