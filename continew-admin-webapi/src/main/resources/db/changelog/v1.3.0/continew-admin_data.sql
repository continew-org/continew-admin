-- liquibase formatted sql

-- changeset BUSS_BCLS:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1060, '消息管理', 1000, 2, '/system/message', 'Message', 'system/message/index', 'notification', b'0', b'0', b'0', 'system:message:list', 6, 1, 1, NOW(), NULL, NULL),
(1061, '消息删除', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:message:delete', 1, 1, 1, NOW(), NULL, NULL);

-- 初始化默认字典
INSERT IGNORE INTO `sys_dict`
(`id`, `name`, `code`, `description`, `is_system`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(2, '消息类型', 'message_type', NULL, b'1', 1, NOW(), NULL, NULL);

INSERT IGNORE INTO `sys_dict_item`
(`id`, `label`, `value`, `color`, `sort`, `description`, `dict_id`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(3, '系统消息', '1', 'blue', 1, NULL, 2, 1, NOW(), NULL, NULL);
