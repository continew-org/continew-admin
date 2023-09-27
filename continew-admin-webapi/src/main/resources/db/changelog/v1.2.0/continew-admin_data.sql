-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1060, '字典管理', 1000, 2, '/system/dict', 'Dict', 'system/dict/index', 'bookmark', b'0', b'0', b'0', 'system:dict:list', 6, 1, 1, NOW(), NULL, NULL),
(1061, '字典新增', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dict:add', 1, 1, 1, NOW(), NULL, NULL),
(1062, '字典修改', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dict:update', 2, 1, 1, NOW(), NULL, NULL),
(1063, '字典删除', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dict:delete', 3, 1, 1, NOW(), NULL, NULL),
(1064, '字典导出', 1060, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dict:export', 4, 1, 1, NOW(), NULL, NULL),
(1070, '系统配置', 1000, 2, '/system/config', 'Config', 'system/config/index', 'desktop', b'0', b'0', b'0', 'system:config:list', 7, 1, 1, NOW(), NULL, NULL),
(1071, '修改配置', 1070, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:config:update', 1, 1, 1, NOW(), NULL, NULL),
(1072, '恢复默认', 1070, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:config:reset', 2, 1, 1, NOW(), NULL, NULL);

-- 初始化默认字典
INSERT IGNORE INTO `sys_dict`
(`id`, `name`, `code`, `description`, `is_system`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, '公告类型', 'announcement_type', NULL, b'1', 1, NOW(), NULL, NULL);

-- 初始化默认字典项
INSERT IGNORE INTO `sys_dict_item`
(`id`, `label`, `value`, `color`, `sort`, `description`, `dict_id`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, '通知', '1', 'blue', 1, NULL, 1, 1, NOW(), NULL, NULL),
(2, '活动', '2', 'orangered', 2, NULL, 1, 1, NOW(), NULL, NULL);

-- 初始化默认参数
INSERT IGNORE INTO `sys_option`
(`name`, `code`, `value`, `default_value`, `description`, `update_user`, `update_time`)
VALUES
('系统标题', 'site_title', NULL, 'ContiNew Admin', '用于显示登录页面的系统标题。', NULL, NULL),
('版权信息', 'site_copyright', NULL,
'Copyright © 2022-present&nbsp;<a href="https://blog.charles7c.top/about/me" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">Charles7c</a>&nbsp;<span>⋅</span>&nbsp;<a href="https://github.com/Charles7c/continew-admin" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">ContiNew Admin</a>&nbsp;<span>⋅</span>&nbsp;<a href="https://beian.miit.gov.cn" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">津ICP备2022005864号-2</a>',
'用于显示登录页面的底部版权信息。', NULL, NULL),
('系统LOGO（16*16）', 'site_favicon', NULL, 'https://cnadmin.charles7c.top/favicon.ico', '用于显示浏览器地址栏的系统LOGO。', NULL, NULL),
('系统LOGO（33*33）', 'site_logo', NULL, 'https://cnadmin.charles7c.top/logo.svg', '用于显示登录页面的系统LOGO。', NULL, NULL);