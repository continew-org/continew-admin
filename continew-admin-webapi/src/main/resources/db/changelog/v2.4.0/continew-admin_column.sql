-- liquibase formatted sql

-- changeset Charles7c:1
ALTER TABLE `sys_log` ADD COLUMN `trace_id` varchar(255) NULL COMMENT '链路ID' AFTER `id`;

-- changeset Charles7c:2
ALTER TABLE `sys_user`
    MODIFY COLUMN `password` varchar(255) DEFAULT NULL COMMENT '密码（加密）' AFTER `nickname`;

-- changeset Charles7c:3
ALTER TABLE `sys_user`
    MODIFY COLUMN `phone` varchar(255) DEFAULT NULL COMMENT '手机号码' AFTER `email`;

-- changeset Charles7c:4
ALTER TABLE `sys_menu`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_dept`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_role`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_user`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_log`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_announcement`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_dict`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_dict_item`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_message`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;
ALTER TABLE `sys_storage`
    MODIFY COLUMN `id` bigint NOT NULL COMMENT 'ID' FIRST;