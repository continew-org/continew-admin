-- liquibase formatted sql

-- changeset charles7c:1
-- comment 初始化表数据
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', 'Layout', '/system/user', 'settings', false, false, false, NULL, 1, 1, 1, NOW()),
(1010, '用户管理', 1000, 2, '/system/user', 'SystemUser', 'system/user/index', NULL, 'user', false, false, false, NULL, 1, 1, 1, NOW()),
(1011, '列表', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:list', 1, 1, 1, NOW()),
(1012, '详情', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:detail', 2, 1, 1, NOW()),
(1013, '新增', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:add', 3, 1, 1, NOW()),
(1014, '修改', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:update', 4, 1, 1, NOW()),
(1015, '删除', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:delete', 5, 1, 1, NOW()),
(1016, '导出', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:export', 6, 1, 1, NOW()),
(1017, '导入', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:import', 7, 1, 1, NOW()),
(1018, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:resetPwd', 8, 1, 1, NOW()),
(1019, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:updateRole', 9, 1, 1, NOW()),

(1030, '角色管理', 1000, 2, '/system/role', 'SystemRole', 'system/role/index', NULL, 'user-group', false, false, false, NULL, 2, 1, 1, NOW()),
(1031, '列表', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:list', 1, 1, 1, NOW()),
(1032, '详情', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:detail', 2, 1, 1, NOW()),
(1033, '新增', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:add', 3, 1, 1, NOW()),
(1034, '修改', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:update', 4, 1, 1, NOW()),
(1035, '删除', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:delete', 5, 1, 1, NOW()),
(1036, '分配', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:assign', 6, 1, 1, NOW()),

(1050, '菜单管理', 1000, 2, '/system/menu', 'SystemMenu', 'system/menu/index', NULL, 'menu', false, false, false, NULL, 3, 1, 1, NOW()),
(1051, '列表', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:list', 1, 1, 1, NOW()),
(1052, '详情', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:detail', 2, 1, 1, NOW()),
(1053, '新增', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:add', 3, 1, 1, NOW()),
(1054, '修改', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:update', 4, 1, 1, NOW()),
(1055, '删除', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:delete', 5, 1, 1, NOW()),

(1060, '部门管理', 1000, 2, '/system/dept', 'SystemDept', 'system/dept/index', NULL, 'mind-mapping', false, false, false, NULL, 4, 1, 1, NOW()),
(1061, '列表', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:list', 1, 1, 1, NOW()),
(1062, '详情', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:detail', 2, 1, 1, NOW()),
(1063, '新增', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:add', 3, 1, 1, NOW()),
(1064, '修改', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:update', 4, 1, 1, NOW()),
(1065, '删除', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:delete', 5, 1, 1, NOW()),
(1066, '导出', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:export', 6, 1, 1, NOW()),

(1070, '字典管理', 1000, 2, '/system/dict', 'SystemDict', 'system/dict/index', NULL, 'bookmark', false, false, false, NULL, 5, 1, 1, NOW()),
(1071, '列表', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:list', 1, 1, 1, NOW()),
(1072, '详情', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:detail', 2, 1, 1, NOW()),
(1073, '新增', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:add', 3, 1, 1, NOW()),
(1074, '修改', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:update', 4, 1, 1, NOW()),
(1075, '删除', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:delete', 5, 1, 1, NOW()),
(1080, '字典项管理', 1000, 2, '/system/dict/item', 'SystemDictItem', 'system/dict/item/index', NULL, 'bookmark', false, false, true, NULL, 5, 1, 1, NOW()),
(1081, '列表', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:list', 1, 1, 1, NOW()),
(1082, '详情', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:detail', 2, 1, 1, NOW()),
(1083, '新增', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:add', 3, 1, 1, NOW()),
(1084, '修改', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:update', 4, 1, 1, NOW()),
(1085, '删除', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:delete', 5, 1, 1, NOW()),

(1090, '通知公告', 1000, 2, '/system/notice', 'SystemNotice', 'system/notice/index', NULL, 'notification', false, false, false, NULL, 6, 1, 1, NOW()),
(1091, '列表', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:list', 1, 1, 1, NOW()),
(1092, '详情', 1090, 2, '/system/notice/detail', 'SystemNoticeDetail', 'system/notice/detail/index', NULL, NULL, false, false, true, 'system:notice:detail', 2, 1, 1, NOW()),
(1093, '新增', 1090, 2, '/system/notice/add', 'SystemNoticeAdd', 'system/notice/add/index', NULL, NULL, false, false, true, 'system:notice:add', 3, 1, 1, NOW()),
(1094, '修改', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:update', 4, 1, 1, NOW()),
(1095, '删除', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:delete', 5, 1, 1, NOW()),

(1100, '文件管理', 1000, 2, '/system/file', 'SystemFile', 'system/file/index', NULL, 'file', false, false, false, NULL, 7, 1, 1, NOW()),
(1101, '列表', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:list', 1, 1, 1, NOW()),
(1102, '详情', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:detail', 2, 1, 1, NOW()),
(1103, '上传', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:upload', 3, 1, 1, NOW()),
(1104, '修改', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:update', 4, 1, 1, NOW()),
(1105, '删除', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:delete', 5, 1, 1, NOW()),
(1106, '下载', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:download', 6, 1, 1, NOW()),

(1110, '存储管理', 1000, 2, '/system/storage', 'SystemStorage', 'system/storage/index', NULL, 'storage', false, false, false, NULL, 8, 1, 1, NOW()),
(1111, '列表', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:list', 1, 1, 1, NOW()),
(1112, '详情', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:detail', 2, 1, 1, NOW()),
(1113, '新增', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:add', 3, 1, 1, NOW()),
(1114, '修改', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:update', 4, 1, 1, NOW()),
(1115, '删除', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:delete', 5, 1, 1, NOW()),

( 1180, '客户端管理', 1000, 2, '/system/client', 'SystemClient', 'system/client/index', NULL, 'mobile', false, false, false, NULL, 9, 1, 1, NOW()),
(1181, '列表', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:list', 1, 1, 1, NOW()),
(1182, '详情', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:detail', 2, 1, 1, NOW()),
(1183, '新增', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:add', 3, 1, 1, NOW()),
(1184, '修改', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:update', 4, 1, 1, NOW()),
(1185, '删除', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:delete', 5, 1, 1, NOW()),

(1190, '系统配置', 1000, 2, '/system/config', 'SystemConfig', 'system/config/index', NULL, 'config', false, false, false, NULL, 999, 1, 1, NOW()),
(1191, '查看', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:list', 1, 1, 1, NOW()),
(1192, '修改', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:update', 2, 1, 1, NOW()),
(1193, '重置', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:reset', 3, 1, 1, NOW()),

(2000, '系统监控', 0, 1, '/monitor', 'Monitor', 'Layout', '/monitor/online', 'computer', false, false, false, NULL, 2, 1, 1, NOW()),
(2010, '在线用户', 2000, 2, '/monitor/online', 'MonitorOnline', 'monitor/online/index', NULL, 'user', false, false, false, NULL, 1, 1, 1, NOW()),
(2011, '列表', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:list', 1, 1, 1, NOW()),
(2012, '强退', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:kickout', 2, 1, 1, NOW()),

(2020, '系统日志', 2000, 2, '/monitor/log', 'MonitorLog', 'monitor/log/index', NULL, 'history', false, false, false, NULL, 2, 1, 1, NOW()),
(2021, '列表', 2020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:list', 1, 1, 1, NOW()),
(2022, '详情', 2020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:detail', 2, 1, 1, NOW()),
(2023, '导出', 2020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:export', 3, 1, 1, NOW());

-- 初始化默认部门
INSERT INTO "sys_dept"
("id", "name", "parent_id", "ancestors", "description", "sort", "status", "is_system", "create_user", "create_time")
VALUES
(1, 'Xxx科技有限公司', 0, '0', '系统初始部门', 1, 1, true, 1, NOW()),
(547887852587843590, '天津总部', 1, '0,1', NULL, 1, 1, false, 1, NOW()),
(547888008188133385, '研发部', 547887852587843590, '0,1,547887852587843590', NULL, 1, 1, false, 1, NOW()),
(547888460711591948, 'UI部', 547887852587843590, '0,1,547887852587843590', NULL, 2, 1, false, 1, NOW()),
(547888483713155087, '测试部', 547887852587843590, '0,1,547887852587843590', NULL, 3, 1, false, 1, NOW()),
(547888505959743506, '运维部', 547887852587843590, '0,1,547887852587843590', NULL, 4, 1, false, 1, NOW()),
(547888556819873814, '研发一组', 547888008188133385, '0,1,547887852587843590,547888008188133385', NULL, 1, 1, false, 1, NOW()),
(547888580614160409, '研发二组', 547888008188133385, '0,1,547887852587843590,547888008188133385', NULL, 2, 2, false, 1, NOW());

-- 初始化默认角色
INSERT INTO "sys_role"
("id", "name", "code", "data_scope", "description", "sort", "is_system", "create_user", "create_time")
VALUES
(1, '系统管理员', 'admin', 1, '系统初始角色', 1, true, 1, NOW()),
(547888897925840928, '测试人员', 'test', 5, NULL, 2, false, 1, NOW());

-- 初始化默认用户：admin/admin123；test/test123
INSERT INTO "sys_user"
("id", "username", "nickname", "password", "gender", "email", "phone", "avatar", "description", "status", "is_system", "pwd_reset_time", "dept_id", "create_user", "create_time")
VALUES
(1, 'admin', '系统管理员', '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa', 1, '42190c6c5639d2ca4edb4150a35e058559ccf8270361a23745a2fd285a273c28', '5bda89a4609a65546422ea56bfe5eab4', NULL, '系统初始用户', 1, true, NOW(), 1, 1, NOW()),
(547889293968801831, 'test', '测试员', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 2, NULL, NULL, NULL, NULL, 1, false, NOW(), 547888483713155087, 1, NOW());

-- 初始化默认参数
INSERT INTO "sys_option"
("id", "category", "name", "code", "value", "default_value", "description")
VALUES
(1, 'SITE', '系统标题', 'SITE_TITLE', NULL, 'ContiNew Admin', '用于显示登录页面的系统标题。'),
(2, 'SITE', '系统描述', 'SITE_DESCRIPTION', NULL, '持续迭代优化的前后端分离中后台管理系统框架', NULL),
(3, 'SITE', '版权信息', 'SITE_COPYRIGHT', NULL, 'Copyright © 2022 - present ContiNew Admin 版权所有', '用于显示登录页面的底部版权信息。'),
(4, 'SITE', '备案号', 'SITE_BEIAN', NULL, '津ICP备2022005864号-3', 'ICP备案号'),
(5, 'SITE', 'favicon', 'SITE_FAVICON', NULL, '/favicon.ico', '用于显示浏览器地址栏的系统LOGO。'),
(6, 'SITE', '系统LOGO', 'SITE_LOGO', NULL, '/logo.svg', '用于显示登录页面的系统LOGO。'),
(7, 'PASSWORD', '登录密码错误锁定账号的次数', 'PASSWORD_ERROR_LOCK_COUNT', NULL, '5', '取值范围为 0-10（0 表示不锁定）。'),
(8, 'PASSWORD', '登录密码错误锁定账号的时间（min）', 'PASSWORD_ERROR_LOCK_MINUTES', NULL, '5', '取值范围为 1-1440（一天）。'),
(9, 'PASSWORD', '密码有效期（天）', 'PASSWORD_EXPIRATION_DAYS', NULL, '0', '取值范围为 0-999（0 表示永久有效）。'),
(10, 'PASSWORD', '密码到期提前提示（天）', 'PASSWORD_EXPIRATION_WARNING_DAYS', NULL, '0', '密码到期 N 天前进行提示（0 表示不提示）。'),
(11, 'PASSWORD', '密码重复使用次数', 'PASSWORD_REPETITION_TIMES', NULL, '3', '不允许使用最近 N 次密码，取值范围为 3-32。'),
(12, 'PASSWORD', '密码最小长度', 'PASSWORD_MIN_LENGTH', NULL, '8', '取值范围为 8-32。'),
(13, 'PASSWORD', '密码是否允许包含正反序账号名', 'PASSWORD_ALLOW_CONTAIN_USERNAME', NULL, '1', NULL),
(14, 'PASSWORD', '密码是否必须包含特殊字符', 'PASSWORD_REQUIRE_SYMBOLS', NULL, '0', NULL),
(15, 'MAIL', '发送协议', 'MAIL_PROTOCOL', NULL, 'smtp', NULL),
(16, 'MAIL', '服务器地址', 'MAIL_HOST', NULL, 'smtp.126.com', NULL),
(17, 'MAIL', '服务器端口', 'MAIL_PORT', NULL, '465', NULL),
(18, 'MAIL', '用户名', 'MAIL_USERNAME', NULL, 'charles7c@126.com', NULL),
(19, 'MAIL', '密码', 'MAIL_PASSWORD', NULL, NULL, NULL),
(20, 'MAIL', '是否启用SSL', 'MAIL_SSL_ENABLED', NULL, '1', NULL),
(21, 'MAIL', 'SSL端口', 'MAIL_SSL_PORT', NULL, '465', NULL),
(22, 'LOGIN', '是否启用验证码', 'LOGIN_CAPTCHA_ENABLED', NULL, '1', '是否启用验证码（1：是；0：否）');

-- 初始化默认字典
INSERT INTO "sys_dict"
("id", "name", "code", "description", "is_system", "create_user", "create_time")
VALUES
(1, '公告类型', 'notice_type', NULL, true, 1, NOW()),
(2, '消息类型', 'message_type', NULL, true, 1, NOW()),
(3, '客户端类型', 'client_type', NULL, true, 1, NOW());

INSERT INTO "sys_dict_item"
("id", "label", "value", "color", "sort", "description", "status", "dict_id", "create_user", "create_time")
VALUES
(1, '通知', '1', 'blue', 1, NULL, 1, 1, 1, NOW()),
(2, '活动', '2', 'orangered', 2, NULL, 1, 1, 1, NOW()),
(3, '安全消息', '1', 'blue', 1, NULL, 1, 2, 1, NOW()),
(4, '活动消息', '2', 'orangered', 2, NULL, 1, 2, 1, NOW()),
(5, '桌面端', 'PC', 'blue', 1, NULL, 1, 3, 1, NOW()),
(6, '安卓', 'ANDROID', '#148628', 2, NULL, 1, 3, 1, NOW()),
(7, '小程序', 'XCX', '#7930AD', 3, NULL, 1, 3, 1, NOW());

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
(547888897925840928, 1000),
(547888897925840928, 1010),
(547888897925840928, 1011),
(547888897925840928, 1012),
(547888897925840928, 1013),
(547888897925840928, 1014);

-- 初始化默认角色和部门关联数据
INSERT INTO "sys_role_dept" ("role_id", "dept_id") VALUES (547888897925840928, 547888483713155087);

-- 初始化默认存储
INSERT INTO "sys_storage"
("id", "name", "code", "type", "access_key", "secret_key", "endpoint", "bucket_name", "domain", "description", "is_default", "sort", "status", "create_user", "create_time")
VALUES
(1, '开发环境', 'local_dev', 2, NULL, NULL, NULL, 'C:/continew-admin/data/file/', 'http://localhost:8000/file', '本地存储', true, 1, 1, 1, NOW()),
(2, '生产环境', 'local_prod', 2, NULL, NULL, NULL, '../data/file/', 'http://api.continew.top/file', '本地存储', false, 2, 2, 1, NOW());

-- 初始化客户端数据
INSERT INTO "sys_client"
("id", "client_id", "client_key", "client_secret", "auth_type", "client_type", "active_timeout", "timeout", "status", "create_user", "create_time")
VALUES
(1, 'ef51c9a3e9046c4f2ea45142c8a8344a', 'pc', 'dd77ab1e353a027e0d60ce3b151e8642', '["ACCOUNT", "EMAIL", "PHONE", "SOCIAL"]', 'PC', 1800, 86400, 1, 1, NOW());
