-- liquibase formatted sql

-- changeset BUSS_BCLS:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1060, '消息管理', 1000, 2, '/system/message', 'Message', 'system/message/index', 'notification', b'0', b'0', b'0', 'system:message:list', 6, 1, 1, NOW(), NULL, NULL),
(1061, '消息删除', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:message:delete', 1, 1, 1, NOW(), NULL, NULL);
