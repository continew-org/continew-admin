-- liquibase formatted sql

-- changeset Charles7c:1
ALTER TABLE `sys_log` ADD COLUMN `trace_id` varchar(255) NULL COMMENT '链路ID' AFTER `id`;