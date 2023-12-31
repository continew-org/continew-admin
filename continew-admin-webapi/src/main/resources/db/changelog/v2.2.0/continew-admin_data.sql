-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1090, '文件管理', 1000, 2, '/system/file', 'File', 'system/file/index', 'file', b'0', b'0', b'0', 'system:file:list', 9, 1, 1, NOW(), NULL, NULL),
(1091, '文件上传', 1090, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:file:upload', 1, 1, 1, NOW(), NULL, NULL),
(1092, '文件修改', 1090, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:file:update', 2, 1, 1, NOW(), NULL, NULL),
(1093, '文件删除', 1090, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:file:delete', 3, 1, 1, NOW(), NULL, NULL),
(1094, '文件下载', 1090, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:file:download', 4, 1, 1, NOW(), NULL, NULL),
(1100, '存储管理', 1000, 2, '/system/storage', 'Storage', 'system/storage/index', 'storage', b'0', b'0', b'1', 'system:storage:list', 10, 1, 1, NOW(), NULL, NULL),
(1101, '存储新增', 1100, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:storage:add', 1, 1, 1, NOW(), NULL, NULL),
(1102, '存储修改', 1100, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:storage:update', 2, 1, 1, NOW(), NULL, NULL),
(1103, '存储删除', 1100, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:storage:delete', 3, 1, 1, NOW(), NULL, NULL),
(1104, '存储导出', 1100, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:storage:export', 4, 1, 1, NOW(), NULL, NULL);

-- 初始化默认存储库
INSERT IGNORE INTO `sys_storage`
(`id`, `name`, `code`, `type`, `access_key`, `secret_key`, `endpoint`, `bucket_name`, `domain`, `description`, `is_default`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, '开发环境', 'local_dev', 2, NULL, NULL, NULL, 'C:/continew-admin/data/file/', 'http://localhost:8000/file/', '本地存储', b'1', 1, 1, 1, NOW(), NULL, NULL),
(2, '生产环境', 'local_prod', 2, NULL, NULL, NULL, '../data/file/', 'http://api.charles7c.top/file/', '本地存储', b'0', 2, 2, 1, NOW(), NULL, NULL);