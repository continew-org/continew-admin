-- liquibase formatted sql

-- changeset BUSS_BCLS:1
-- 初始化默认字典
INSERT IGNORE INTO `sys_dict`
(`id`, `name`, `code`, `description`, `is_system`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(2, '消息类型', 'message_type', NULL, b'1', 1, NOW(), NULL, NULL);

-- 初始化默认字典项
INSERT IGNORE INTO `sys_dict_item`
(`id`, `label`, `value`, `color`, `sort`, `description`, `dict_id`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(3, '系统消息', '1', 'blue', 1, NULL, 2, 1, NOW(), NULL, NULL);
