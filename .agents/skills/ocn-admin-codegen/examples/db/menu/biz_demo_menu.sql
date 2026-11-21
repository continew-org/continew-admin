-- liquibase formatted sql

-- changeset charles7c:1
-- comment 示例管理菜单与按钮权限
-- 页面菜单（4000）挂在「系统管理」目录（1000）下，按钮 permission 与 CrudRequestMapping 按 Api 集推导的权限码逐字一致
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(4000, '示例管理', 1000, 2, '/system/demo', 'SystemDemo', 'system/demo/index', NULL, 'boxes', b'0', b'0', b'0', NULL, 999, 1, 1, NOW()),
(4001, '列表', 4000, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:demo:list', 1, 1, 1, NOW()),
(4002, '详情', 4000, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:demo:get', 2, 1, 1, NOW()),
(4003, '新增', 4000, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:demo:create', 3, 1, 1, NOW()),
(4004, '修改', 4000, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:demo:update', 4, 1, 1, NOW()),
(4005, '删除', 4000, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:demo:delete', 5, 1, 1, NOW());
