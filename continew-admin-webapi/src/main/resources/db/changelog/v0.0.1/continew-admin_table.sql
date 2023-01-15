-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_user`  (
    `user_id` bigint(20) unsigned AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(255) NOT NULL COMMENT '用户名',
    `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `gender` tinyint(1) unsigned DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    `phone` varchar(255) DEFAULT NULL COMMENT '手机号码',
    `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
    `notes` varchar(512) DEFAULT NULL COMMENT '备注',
    `status` tinyint(1) unsigned DEFAULT 1 COMMENT '状态（1启用 2禁用）',
    `pwd_reset_time` datetime DEFAULT NULL COMMENT '最后一次修改密码的时间',
    `create_user` bigint(20) unsigned NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) unsigned NOT NULL COMMENT '修改人',
    `update_time` datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`user_id`) USING BTREE,
    UNIQUE INDEX `uk_username`(`username`) USING BTREE,
    UNIQUE INDEX `uk_email`(`email`) USING BTREE,
    INDEX `idx_createUser`(`create_user`) USING BTREE,
    INDEX `idx_updateUser`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_log` (
    `log_id` bigint(20) unsigned AUTO_INCREMENT COMMENT '日志ID',
    `description` varchar(255) DEFAULT NULL COMMENT '日志描述',
    `request_url` varchar(512) NOT NULL DEFAULT '' COMMENT '请求URL',
    `request_method` varchar(10) DEFAULT NULL COMMENT '请求方式',
    `request_header` text COMMENT '请求头',
    `request_body` text DEFAULT NULL COMMENT '请求体',
    `status_code` int(11) unsigned DEFAULT NULL COMMENT '状态码',
    `response_header` text DEFAULT NULL COMMENT '响应头',
    `response_body` mediumtext DEFAULT NULL COMMENT '响应体',
    `elapsed_time` bigint(20) unsigned DEFAULT NULL COMMENT '请求耗时（ms）',
    `result` tinyint(1) unsigned DEFAULT 1 COMMENT '操作结果（1成功 2失败）',
    `request_ip` varchar(255) DEFAULT NULL COMMENT '操作IP',
    `location` varchar(512) DEFAULT NULL COMMENT '操作地点',
    `browser` varchar(255) DEFAULT NULL COMMENT '浏览器',
    `exception` mediumtext DEFAULT NULL COMMENT '异常',
    `create_user` bigint(20) unsigned DEFAULT NULL COMMENT '操作人',
    `create_time` datetime NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`log_id`) USING BTREE,
    INDEX `idx_createUser`(`create_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';
