-- liquibase formatted sql

-- changeset Charles7c:2.5.0
-- comment 初始化表数据
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', NULL, 'settings', false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(1010, '用户管理', 1000, 2, '/system/user', 'User', 'system/user/index', 'user', false, false, false, 'system:user:list', 1, 1, 1, NOW(), NULL, NULL),
(1011, '用户新增', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:add', 1, 1, 1, NOW(), NULL, NULL),
(1012, '用户修改', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:update', 2, 1, 1, NOW(), NULL, NULL),
(1013, '用户删除', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:delete', 3, 1, 1, NOW(), NULL, NULL),
(1014, '用户导出', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:export', 4, 1, 1, NOW(), NULL, NULL),
(1015, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:password:reset', 5, 1, 1, NOW(), NULL, NULL),
(1016, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:user:role:update', 6, 1, 1, NOW(), NULL, NULL),
(1020, '部门管理', 1000, 2, '/system/dept', 'Dept', 'system/dept/index', 'user-group', false, false, false, 'system:dept:list', 2, 1, 1, NOW(), NULL, NULL),
(1021, '部门新增', 1020, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dept:add', 1, 1, 1, NOW(), NULL, NULL),
(1022, '部门修改', 1020, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dept:update', 2, 1, 1, NOW(), NULL, NULL),
(1023, '部门删除', 1020, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dept:delete', 3, 1, 1, NOW(), NULL, NULL),
(1024, '部门导出', 1020, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dept:export', 4, 1, 1, NOW(), NULL, NULL),
(1030, '角色管理', 1000, 2, '/system/role', 'Role', 'system/role/index', 'safe', false, false, false, 'system:role:list', 3, 1, 1, NOW(), NULL, NULL),
(1031, '角色新增', 1030, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:role:add', 1, 1, 1, NOW(), NULL, NULL),
(1032, '角色修改', 1030, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:role:update', 2, 1, 1, NOW(), NULL, NULL),
(1033, '角色删除', 1030, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:role:delete', 3, 1, 1, NOW(), NULL, NULL),
(1034, '角色导出', 1030, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:role:export', 4, 1, 1, NOW(), NULL, NULL),
(1040, '菜单管理', 1000, 2, '/system/menu', 'Menu', 'system/menu/index', 'menu', false, false, false, 'system:menu:list', 4, 1, 1, NOW(), NULL, NULL),
(1041, '菜单新增', 1040, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:menu:add', 1, 1, 1, NOW(), NULL, NULL),
(1042, '菜单修改', 1040, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:menu:update', 2, 1, 1, NOW(), NULL, NULL),
(1043, '菜单删除', 1040, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:menu:delete', 3, 1, 1, NOW(), NULL, NULL),
(1044, '菜单导出', 1040, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:menu:export', 4, 1, 1, NOW(), NULL, NULL),
(1050, '公告管理', 1000, 2, '/system/announcement', 'Announcement', 'system/announcement/index', 'advertising', false, false, false, 'system:announcement:list', 5, 1, 1, NOW(), NULL, NULL),
(1051, '公告新增', 1050, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:announcement:add', 1, 1, 1, NOW(), NULL, NULL),
(1052, '公告修改', 1050, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:announcement:update', 2, 1, 1, NOW(), NULL, NULL),
(1053, '公告删除', 1050, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:announcement:delete', 3, 1, 1, NOW(), NULL, NULL),
(1054, '公告导出', 1050, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:announcement:export', 4, 1, 1, NOW(), NULL, NULL),
(1060, '消息管理', 1000, 2, '/system/message', 'Message', 'system/message/index', 'notification', false, false, false, 'system:message:list', 6, 1, 1, NOW(), NULL, NULL),
(1061, '消息删除', 1060, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:message:delete', 1, 1, 1, NOW(), NULL, NULL),
(1070, '字典管理', 1000, 2, '/system/dict', 'Dict', 'system/dict/index', 'bookmark', false, false, false, 'system:dict:list', 7, 1, 1, NOW(), NULL, NULL),
(1071, '字典新增', 1070, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dict:add', 1, 1, 1, NOW(), NULL, NULL),
(1072, '字典修改', 1070, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dict:update', 2, 1, 1, NOW(), NULL, NULL),
(1073, '字典删除', 1070, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dict:delete', 3, 1, 1, NOW(), NULL, NULL),
(1074, '字典导出', 1070, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:dict:export', 4, 1, 1, NOW(), NULL, NULL),
(1080, '系统配置', 1000, 2, '/system/config', 'Config', 'system/config/index', 'desktop', false, false, false, 'system:config:list', 8, 1, 1, NOW(), NULL, NULL),
(1081, '修改配置', 1080, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:config:update', 1, 1, 1, NOW(), NULL, NULL),
(1082, '恢复默认', 1080, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:config:reset', 2, 1, 1, NOW(), NULL, NULL),
(1090, '文件管理', 1000, 2, '/system/file', 'File', 'system/file/index', 'file', false, false, false, 'system:file:list', 9, 1, 1, NOW(), NULL, NULL),
(1091, '文件上传', 1090, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:file:upload', 1, 1, 1, NOW(), NULL, NULL),
(1092, '文件修改', 1090, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:file:update', 2, 1, 1, NOW(), NULL, NULL),
(1093, '文件删除', 1090, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:file:delete', 3, 1, 1, NOW(), NULL, NULL),
(1094, '文件下载', 1090, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:file:download', 4, 1, 1, NOW(), NULL, NULL),
(1100, '存储管理', 1000, 2, '/system/storage', 'Storage', 'system/storage/index', 'storage', false, false, true, 'system:storage:list', 10, 1, 1, NOW(), NULL, NULL),
(1101, '存储新增', 1100, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:storage:add', 1, 1, 1, NOW(), NULL, NULL),
(1102, '存储修改', 1100, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:storage:update', 2, 1, 1, NOW(), NULL, NULL),
(1103, '存储删除', 1100, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:storage:delete', 3, 1, 1, NOW(), NULL, NULL),
(1104, '存储导出', 1100, 3, NULL, NULL, NULL, NULL, false, false, false, 'system:storage:export', 4, 1, 1, NOW(), NULL, NULL),
(2000, '系统工具', 0, 1, '/tool', 'Tool', NULL, 'tool', false, false, false, NULL, 2, 1, 1, NOW(), NULL, NULL),
(2010, '代码生成', 2000, 2, '/tool/generator', 'Generator', 'tool/generator/index', 'code', false, false, false, 'tool:generator:list', 1, 1, 1, NOW(), NULL, NULL),
(9000, '系统监控', 0, 1, '/monitor', 'Monitor', NULL, 'computer', false, false, false, NULL, 899, 1, 1, NOW(), NULL, NULL),
(9010, '在线用户', 9000, 2, '/monitor/online', 'OnlineUser', 'monitor/online/index', 'anonymity', false, false, false, 'monitor:online:user:list', 1, 1, 1, NOW(), NULL, NULL),
(9011, '强退用户', 9010, 3, NULL, NULL, NULL, NULL, false, false, false, 'monitor:online:user:delete', 1, 1, 1, NOW(), NULL, NULL),
(9030, '登录日志', 9000, 2, '/monitor/log/login', 'LoginLog', 'monitor/log/login/index', 'old-version', false, false, false, 'monitor:log:login:list', 2, 1, 1, NOW(), NULL, NULL),
(9050, '操作日志', 9000, 2, '/monitor/log/operation', 'OperationLog', 'monitor/log/operation/index', 'history', false, false, false, 'monitor:log:operation:list', 3, 1, 1, NOW(), NULL, NULL),
(9070, '系统日志', 9000, 2, '/monitor/log/system', 'SystemLog', 'monitor/log/system/index', 'behavior-anal', false, false, false, 'monitor:log:system:list', 4, 1, 1, NOW(), NULL, NULL),
(10000, '接口文档', 0, 1, 'https://api.charles7c.top/doc.html', NULL, NULL, 'code-square', true, false, false, NULL, 997, 1, 1, NOW(), NULL, NULL),
(10001, 'Arco Design Vue', 0, 1, 'https://arco.design/vue/docs/start', NULL, NULL, 'link', true, false, false, NULL, 998, 1, 1, NOW(), NULL, NULL),
(10002, 'GitHub', 0, 1, 'https://github.com/Charles7c/continew-admin', NULL, NULL, 'github', true, false, false, NULL, 999, 1, 1, NOW(), NULL, NULL);

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
("id", "label", "value", "color", "sort", "description", "dict_id", "create_user", "create_time", "update_user", "update_time")
VALUES
(547889649658363951, '通知', '1', 'blue', 1, NULL, 547889614262632491, 1, NOW(), NULL, NULL),
(547890124537462835, '活动', '2', 'orangered', 2, NULL, 547889614262632491, 1, NOW(), NULL, NULL);

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