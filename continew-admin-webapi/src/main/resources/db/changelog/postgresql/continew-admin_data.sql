-- liquibase formatted sql

-- changeset Charles7c:2.5.0
-- comment 初始化表数据
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', 'Layout', '/system/user', 'icon-settings', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(1010, '用户管理', 1000, 2, '/system/user', 'User', 'system/user/index', NULL, 'icon-user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(1011, '用户列表', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:list', 1, 1, 1, NOW(), NULL, NULL),
(1012, '用户详情', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:detail', 2, 1, 1, NOW(), NULL, NULL),
(1013, '用户新增', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:add', 3, 1, 1, NOW(), NULL, NULL),
(1014, '用户修改', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:update', 4, 1, 1, NOW(), NULL, NULL),
(1015, '用户删除', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:delete', 5, 1, 1, NOW(), NULL, NULL),
(1016, '用户导出', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:export', 6, 1, 1, NOW(), NULL, NULL),
(1017, '用户导入', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:import', 7, 1, 1, NOW(), NULL, NULL),
(1018, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:resetPwd', 8, 1, 1, NOW(), NULL, NULL),
(1030, '角色管理', 1000, 2, '/system/role', 'Role', 'system/role/index', NULL, 'icon-user-group', b'0', b'0', b'0', NULL, 2, 1, 1, NOW(), NULL, NULL),
(1031, '角色列表', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:list', 1, 1, 1, NOW(), NULL, NULL),
(1032, '角色详情', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:detail', 2, 1, 1, NOW(), NULL, NULL),
(1033, '角色新增', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:add', 3, 1, 1, NOW(), NULL, NULL),
(1034, '角色修改', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:update', 4, 1, 1, NOW(), NULL, NULL),
(1035, '角色删除', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:delete', 5, 1, 1, NOW(), NULL, NULL),
(1036, '角色导出', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:export', 6, 1, 1, NOW(), NULL, NULL),
(1050, '菜单管理', 1000, 2, '/system/menu', 'Menu', 'system/menu/index', NULL, 'icon-menu', b'0', b'0', b'0', NULL, 3, 1, 1, NOW(), NULL, NULL),
(1051, '菜单列表', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:list', 1, 1, 1, NOW(), NULL, NULL),
(1052, '菜单新增', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:add', 2, 1, 1, NOW(), NULL, NULL),
(1053, '菜单修改', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:update', 3, 1, 1, NOW(), NULL, NULL),
(1054, '菜单删除', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:delete', 4, 1, 1, NOW(), NULL, NULL),
(1055, '菜单导出', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:export', 5, 1, 1, NOW(), NULL, NULL),
(1060, '部门管理', 1000, 2, '/system/dept', 'Dept', 'system/dept/index', NULL, 'icon-mind-mapping', b'0', b'0', b'0', NULL, 4, 1, 1, NOW(), NULL, NULL),
(1061, '部门列表', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:list', 1, 1, 1, NOW(), NULL, NULL),
(1062, '部门详情', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:detail', 2, 1, 1, NOW(), NULL, NULL),
(1063, '部门新增', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:add', 3, 1, 1, NOW(), NULL, NULL),
(1064, '部门修改', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:update', 4, 1, 1, NOW(), NULL, NULL),
(1065, '部门删除', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:delete', 5, 1, 1, NOW(), NULL, NULL),
(1066, '部门导出', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:export', 6, 1, 1, NOW(), NULL, NULL),
(1070, '字典管理', 1000, 2, '/system/dict', 'Dict', 'system/dict/index', NULL, 'icon-bookmark', b'0', b'0', b'0', NULL, 5, 1, 1, NOW(), NULL, NULL),
(1071, '字典列表', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:list', 1, 1, 1, NOW(), NULL, NULL),
(1072, '字典新增', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:add', 2, 1, 1, NOW(), NULL, NULL),
(1073, '字典修改', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:update', 3, 1, 1, NOW(), NULL, NULL),
(1074, '字典删除', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:delete', 4, 1, 1, NOW(), NULL, NULL),
(1075, '字典导出', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:export', 5, 1, 1, NOW(), NULL, NULL),
(1090, '通知公告', 1000, 2, '/system/notice', 'Notice', 'system/notice/index', NULL, 'icon-notification', b'0', b'0', b'0', NULL, 6, 1, 1, NOW(), NULL, NULL),
(1091, '公告列表', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:list', 1, 1, 1, NOW(), NULL, NULL),
(1092, '公告详情', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:detail', 2, 1, 1, NOW(), NULL, NULL),
(1093, '公告新增', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:add', 3, 1, 1, NOW(), NULL, NULL),
(1094, '公告修改', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:update', 4, 1, 1, NOW(), NULL, NULL),
(1095, '公告删除', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:delete', 5, 1, 1, NOW(), NULL, NULL),
(1100, '系统日志', 1000, 2, '/system/log', 'Log', 'system/log/index', NULL, 'icon-history', b'0', b'0', b'0', NULL, 7, 1, 1, NOW(), NULL, NULL),
(1101, '日志列表', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:log:list', 1, 1, 1, NOW(), NULL, NULL),
(1102, '日志导出', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:log:export', 2, 1, 1, NOW(), NULL, NULL),
(1110, '文件管理', 1000, 2, '/system/file', 'File', 'system/file/index', NULL, 'icon-file', b'0', b'0', b'0', 'system:file:list', 8, 1, 1, NOW(), NULL, NULL),
(1111, '文件列表', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:list', 1, 1, 1, NOW(), NULL, NULL),
(1112, '文件详情', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:detail', 2, 1, 1, NOW(), NULL, NULL),
(1113, '文件上传', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:upload', 3, 1, 1, NOW(), NULL, NULL),
(1114, '文件修改', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:update', 4, 1, 1, NOW(), NULL, NULL),
(1115, '文件删除', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:delete', 5, 1, 1, NOW(), NULL, NULL),
(1116, '文件下载', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:download', 6, 1, 1, NOW(), NULL, NULL),
(1120, '存储管理', 1000, 2, '/system/storage', 'Storage', 'system/storage/index', NULL, 'icon-storage', b'0', b'0', b'0', NULL, 8, 1, 1, NOW(), NULL, NULL),
(1121, '存储列表', 1120, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:list', 1, 1, 1, NOW(), NULL, NULL),
(1122, '存储详情', 1120, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:detail', 2, 1, 1, NOW(), NULL, NULL),
(1123, '存储新增', 1120, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:add', 3, 1, 1, NOW(), NULL, NULL),
(1124, '存储修改', 1120, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:update', 4, 1, 1, NOW(), NULL, NULL),
(1125, '存储删除', 1120, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:delete', 5, 1, 1, NOW(), NULL, NULL),
(1190, '系统配置', 1000, 2, '/system/config', 'Config', 'system/config/index', NULL, 'icon-desktop', b'0', b'0', b'0', NULL, 999, 1, 1, NOW(), NULL, NULL),
(1191, '配置查询', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:query', 1, 1, 1, NOW(), NULL, NULL),
(1192, '配置修改', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:update', 2, 1, 1, NOW(), NULL, NULL),
(2000, '系统监控', 0, 1, '/monitor', 'Monitor', 'Layout', '/monitor/online', 'icon-computer', b'0', b'0', b'0', NULL, 2, 1, 1, NOW(), NULL, NULL),
(2010, '在线用户', 2000, 2, '/monitor/online', 'OnlineUser', 'monitor/online/index', NULL, 'icon-user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(2011, '用户查询', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:list', 1, 1, 1, NOW(), NULL, NULL),
(2012, '用户强退', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:kickout', 2, 1, 1, NOW(), NULL, NULL),
(3000, '系统工具', 0, 1, '/tool', 'Tool', 'Layout', '/tool/generator', 'icon-tool', b'0', b'0', b'0', NULL, 3, 1, 1, NOW(), NULL, NULL),
(3010, '代码生成', 3000, 2, '/tool/generator', 'Generator', 'tool/generator/index', NULL, 'icon-code', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(3011, '数据表查询', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tool:generator:list', 1, 1, 1, NOW(), NULL, NULL),
(10000, '关于项目', 0, 1, '/project', 'Project', 'Layout', NULL, 'icon-apps', b'0', b'0', b'0', NULL, 999, 1, 1, NOW(), NULL, NULL),
(10001, '接口文档', 10000, 2, 'https://api.charles7c.top/doc.html', NULL, NULL, NULL, 'icon-code-square', b'1', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(10002, 'Gitee', 10000, 2, 'https://gitee.com/continew/continew-admin', NULL, NULL, NULL, 'svg-gitee', b'1', b'0', b'0', NULL, 2, 1, 1, NOW(), NULL, NULL),
(10003, 'GitHub', 10000, 2, 'https://github.com/charles7c/continew-admin', NULL, NULL, NULL, 'icon-github', b'1', b'0', b'0', NULL, 3, 1, 1, NOW(), NULL, NULL);

-- 初始化默认部门
INSERT INTO "sys_dept"
("id", "name", "parent_id", "ancestors", "description", "sort", "status", "is_system", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, 'Xxx科技有限公司', 0, '0', '系统初始部门', 1, 1, true, 1, NOW(), NULL, NULL),
(547887852587843590, '天津总部', 1, '0,1', NULL, 1, 1, false, 1, NOW(), NULL, NULL),
(547888008188133385, '研发部', 547887852587843590, '0,1,547887852587843590', NULL, 1, 1, false, 1, NOW(), NULL, NULL),
(547888460711591948, 'UI部', 547887852587843590, '0,1,547887852587843590', NULL, 2, 1, false, 1, NOW(), NULL, NULL),
(547888483713155087, '测试部', 547887852587843590, '0,1,547887852587843590', NULL, 3, 1, false, 1, NOW(), NULL, NULL),
(547888505959743506, '运维部', 547887852587843590, '0,1,547887852587843590', NULL, 4, 1, false, 1, NOW(), NULL, NULL),
(547888556819873814, '研发一组', 547888008188133385, '0,1,547887852587843590,547888008188133385', NULL, 1, 1, false, 1, NOW(), NULL, NULL),
(547888580614160409, '研发二组', 547888008188133385, '0,1,547887852587843590,547888008188133385', NULL, 2, 2, false, 1, NOW(), NULL, NULL);

-- 初始化默认角色
INSERT INTO "sys_role"
("id", "name", "code", "data_scope", "description", "sort", "status", "is_system", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, '超级管理员', 'admin', 1, '系统初始角色', 1, 1, true, 1, NOW(), NULL, NULL),
(547888897925840928, '测试人员', 'test', 5, NULL, 2, 1, false, 1, NOW(), NULL, NULL);

-- 初始化默认用户：admin/admin123；test/123456
INSERT INTO "sys_user"
("id", "username", "nickname", "password", "gender", "email", "phone", "avatar", "description", "status", "is_system", "pwd_reset_time", "dept_id", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, 'admin', '超级管理员', '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa', 1, '42190c6c5639d2ca4edb4150a35e058559ccf8270361a23745a2fd285a273c28', '5bda89a4609a65546422ea56bfe5eab4', NULL, '系统初始用户', 1, true, NOW(), 1, 1, NOW(), NULL, NULL),
(547889293968801831, 'test', '测试员', '{bcrypt}$2a$10$meMbyso06lupZjxT88fG8undZo6.DSNUmifRfnnre8r/s13ciq6M6', 2, NULL, NULL, NULL, NULL, 2, false, NOW(), 547888483713155087, 1, NOW(), NULL, NULL);

-- 初始化默认参数
INSERT INTO "sys_option"
("name", "code", "value", "default_value", "description", "update_user", "update_time")
VALUES
('系统标题', 'site_title', NULL, 'ContiNew Admin', '用于显示登录页面的系统标题。', NULL, NULL),
('版权信息', 'site_copyright', NULL,
 'Copyright © 2022-present&nbsp;<a href="https://blog.charles7c.top/about/me" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">Charles7c</a>&nbsp;<span>⋅</span>&nbsp;<a href="https://github.com/Charles7c/continew-admin" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">ContiNew Admin</a>&nbsp;<span>⋅</span>&nbsp;<a href="https://beian.miit.gov.cn" target="_blank" rel="noopener" style="text-decoration: none; color: rgb(78,89,105)">津ICP备2022005864号-2</a>',
 '用于显示登录页面的底部版权信息。', NULL, NULL),
('系统LOGO（16*16）', 'site_favicon', NULL, 'https://cnadmin.charles7c.top/favicon.ico', '用于显示浏览器地址栏的系统LOGO。', NULL, NULL),
('系统LOGO（33*33）', 'site_logo', NULL, 'https://cnadmin.charles7c.top/logo.svg', '用于显示登录页面的系统LOGO。', NULL, NULL);

-- 初始化默认字典
INSERT INTO "sys_dict"
("id", "name", "code", "description", "is_system", "create_user", "create_time", "update_user", "update_time")
VALUES
(547889614262632491, '公告类型', 'announcement_type', NULL, true, 1, NOW(), NULL, NULL);

INSERT INTO "sys_dict_item"
("id", "label", "value", "color", "sort", "description", "status", "dict_id", "create_user", "create_time", "update_user", "update_time")
VALUES
(547889649658363951, '通知', '1', 'blue', 1, NULL, 547889614262632491, 1, 1, NOW(), NULL, NULL),
(547890124537462835, '活动', '2', 'orangered', 2, NULL, 547889614262632491, 1, 1, NOW(), NULL, NULL);

-- 初始化默认用户和角色关联数据
INSERT INTO "sys_user_role"
("user_id", "role_id")
VALUES
(1, 1),
(547889293968801831, 547888897925840928);

-- 初始化默认角色和菜单关联数据
INSERT INTO "sys_role_menu"
("role_id", "menu_id")
VALUES
(547889293968801831, 1000),
(547889293968801831, 1010),
(547889293968801831, 1011),
(547889293968801831, 1012),
(547889293968801831, 1013),
(547889293968801831, 1014);

-- 初始化默认角色和部门关联数据
INSERT INTO "sys_role_dept" ("role_id", "dept_id") VALUES (547888897925840928, 547888483713155087);

-- 初始化默认存储库
INSERT INTO "sys_storage"
("id", "name", "code", "type", "access_key", "secret_key", "endpoint", "bucket_name", "domain", "description", "is_default", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
(547890346239983671, '开发环境', 'local_dev', 2, NULL, NULL, NULL, 'C:/continew-admin/data/file/', 'http://localhost:8000/file', '本地存储', true, 1, 1, 1, NOW(), NULL, NULL),
(547890366586552377, '生产环境', 'local_prod', 2, NULL, NULL, NULL, '../data/file/', 'http://api.charles7c.top/file', '本地存储', false, 2, 2, 1, NOW(), NULL, NULL);