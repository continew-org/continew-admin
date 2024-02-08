-- liquibase formatted sql

-- changeset Charles7c:1
ALTER TABLE `sys_log` ADD COLUMN `trace_id` varchar(255) NULL COMMENT '链路ID' AFTER `id`;

-- changeset Charles7c:2
ALTER TABLE `sys_user`
    MODIFY COLUMN `password` varchar(255) DEFAULT NULL COMMENT '密码（加密）' AFTER `nickname`;

-- changeset Charles7c:3
ALTER TABLE `sys_user`
    MODIFY COLUMN `phone` varchar(255) DEFAULT NULL COMMENT '手机号码' AFTER `email`;