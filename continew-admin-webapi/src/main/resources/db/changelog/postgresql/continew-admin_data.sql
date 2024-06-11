-- liquibase formatted sql

-- changeset Charles7c:1
-- comment 初始化表数据
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', 'Layout', '/system/user', 'settings', false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(1010, '用户管理', 1000, 2, '/system/user', 'SystemUser', 'system/user/index', NULL, 'user', false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(1011, '查看', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:list', 1, 1, 1, NOW(), NULL, NULL),
(1012, '新增', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:add', 2, 1, 1, NOW(), NULL, NULL),
(1013, '修改', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:update', 3, 1, 1, NOW(), NULL, NULL),
(1014, '删除', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:delete', 4, 1, 1, NOW(), NULL, NULL),
(1015, '导出', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:export', 5, 1, 1, NOW(), NULL, NULL),
(1016, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:resetPwd', 6, 1, 1, NOW(), NULL, NULL),
(1030, '角色管理', 1000, 2, '/system/role', 'SystemRole', 'system/role/index', NULL, 'user-group', false, false, false, NULL, 2, 1, 1, NOW(), NULL, NULL),
(1031, '查看', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:list', 1, 1, 1, NOW(), NULL, NULL),
(1032, '新增', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:add', 2, 1, 1, NOW(), NULL, NULL),
(1033, '修改', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:update', 3, 1, 1, NOW(), NULL, NULL),
(1034, '删除', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:delete', 4, 1, 1, NOW(), NULL, NULL),
(1050, '菜单管理', 1000, 2, '/system/menu', 'SystemMenu', 'system/menu/index', NULL, 'menu', false, false, false, NULL, 3, 1, 1, NOW(), NULL, NULL),
(1051, '查看', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:list', 1, 1, 1, NOW(), NULL, NULL),
(1052, '新增', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:add', 2, 1, 1, NOW(), NULL, NULL),
(1053, '修改', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:update', 3, 1, 1, NOW(), NULL, NULL),
(1054, '删除', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:delete', 4, 1, 1, NOW(), NULL, NULL),
(1060, '部门管理', 1000, 2, '/system/dept', 'SystemDept', 'system/dept/index', NULL, 'mind-mapping', false, false, false, NULL, 4, 1, 1, NOW(), NULL, NULL),
(1061, '查看', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:list', 1, 1, 1, NOW(), NULL, NULL),
(1062, '新增', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:add', 2, 1, 1, NOW(), NULL, NULL),
(1063, '修改', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:update', 3, 1, 1, NOW(), NULL, NULL),
(1064, '删除', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:delete', 4, 1, 1, NOW(), NULL, NULL),
(1065, '导出', 1060, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:export', 5, 1, 1, NOW(), NULL, NULL),
(1070, '字典管理', 1000, 2, '/system/dict', 'SystemDict', 'system/dict/index', NULL, 'bookmark', false, false, false, NULL, 5, 1, 1, NOW(), NULL, NULL),
(1071, '查看', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:list', 1, 1, 1, NOW(), NULL, NULL),
(1072, '新增', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:add', 2, 1, 1, NOW(), NULL, NULL),
(1073, '修改', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:update', 3, 1, 1, NOW(), NULL, NULL),
(1074, '删除', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:delete', 4, 1, 1, NOW(), NULL, NULL),
(1080, '字典项管理', 1000, 2, '/system/dict/item', 'SystemDictItem', 'system/dict/item/index', NULL, 'bookmark', false, false, true, NULL, 5, 1, 1, NOW(), NULL, NULL),
(1081, '查看', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:list', 1, 1, 1, NOW(), NULL, NULL),
(1082, '新增', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:add', 2, 1, 1, NOW(), NULL, NULL),
(1083, '修改', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:update', 3, 1, 1, NOW(), NULL, NULL),
(1084, '删除', 1080, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:item:delete', 4, 1, 1, NOW(), NULL, NULL),
(1090, '通知公告', 1000, 2, '/system/notice', 'SystemNotice', 'system/notice/index', NULL, 'notification', false, false, false, NULL, 6, 1, 1, NOW(), NULL, NULL),
(1091, '查看', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:list', 1, 1, 1, NOW(), NULL, NULL),
(1092, '新增', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:add', 2, 1, 1, NOW(), NULL, NULL),
(1093, '修改', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:update', 3, 1, 1, NOW(), NULL, NULL),
(1094, '删除', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:delete', 4, 1, 1, NOW(), NULL, NULL),
(1100, '文件管理', 1000, 2, '/system/file', 'SystemFile', 'system/file/index', NULL, 'file', false, false, false, NULL, 7, 1, 1, NOW(), NULL, NULL),
(1101, '查看', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:list', 1, 1, 1, NOW(), NULL, NULL),
(1102, '上传', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:upload', 2, 1, 1, NOW(), NULL, NULL),
(1103, '修改', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:update', 3, 1, 1, NOW(), NULL, NULL),
(1104, '删除', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:delete', 4, 1, 1, NOW(), NULL, NULL),
(1105, '下载', 1100, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:download', 5, 1, 1, NOW(), NULL, NULL),
(1110, '存储管理', 1000, 2, '/system/storage', 'SystemStorage', 'system/storage/index', NULL, 'storage', false, false, false, NULL, 8, 1, 1, NOW(), NULL, NULL),
(1111, '查看', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:list', 1, 1, 1, NOW(), NULL, NULL),
(1112, '新增', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:add', 2, 1, 1, NOW(), NULL, NULL),
(1113, '修改', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:update', 3, 1, 1, NOW(), NULL, NULL),
(1114, '删除', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:delete', 4, 1, 1, NOW(), NULL, NULL),
(1190, '系统配置', 1000, 2, '/system/config', 'SystemConfig', 'system/config/index', NULL, 'desktop', false, false, false, NULL, 999, 1, 1, NOW(), NULL, NULL),
(1191, '查看', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:list', 1, 1, 1, NOW(), NULL, NULL),
(1192, '修改', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:update', 2, 1, 1, NOW(), NULL, NULL),
(1193, '重置', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:config:reset', 3, 1, 1, NOW(), NULL, NULL),
(2000, '系统监控', 0, 1, '/monitor', 'Monitor', 'Layout', '/monitor/online', 'computer', false, false, false, NULL, 2, 1, 1, NOW(), NULL, NULL),
(2010, '在线用户', 2000, 2, '/monitor/online', 'MonitorOnline', 'monitor/online/index', NULL, 'user', false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(2011, '查看', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:list', 1, 1, 1, NOW(), NULL, NULL),
(2012, '强退', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:kickout', 2, 1, 1, NOW(), NULL, NULL),
(2020, '系统日志', 2000, 2, '/monitor/log', 'MonitorLog', 'monitor/log/index', NULL, 'history', false, false, false, NULL, 2, 1, 1, NOW(), NULL, NULL),
(2021, '查看', 2020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:list', 1, 1, 1, NOW(), NULL, NULL),
(2022, '导出', 2020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:export', 2, 1, 1, NOW(), NULL, NULL),
(3000, '系统工具', 0, 1, '/tool', 'Tool', 'Layout', '/tool/generator', 'tool', false, false, false, NULL, 3, 1, 1, NOW(), NULL, NULL),
(3010, '代码生成', 3000, 2, '/tool/generator', 'ToolGenerator', 'tool/generator/index', NULL, 'code', false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(3011, '查看', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tool:generator:list', 1, 1, 1, NOW(), NULL, NULL),
(3012, '配置', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tool:generator:config', 2, 1, 1, NOW(), NULL, NULL),
(3013, '预览', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tool:generator:preview', 3, 1, 1, NOW(), NULL, NULL),
(3014, '生成', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tool:generator:generate', 4, 1, 1, NOW(), NULL, NULL),
(10000, '关于项目', 0, 1, '/project', 'Project', 'Layout', NULL, 'apps', false, false, false, NULL, 999, 1, 1, NOW(), NULL, NULL),
(10001, '接口文档', 10000, 2, 'https://api.continew.top/doc.html', NULL, NULL, NULL, 'code-square', true, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL),
(10002, 'Gitee', 10000, 2, 'https://gitee.com/continew/continew-admin', NULL, NULL, NULL, 'gitee', true, false, false, NULL, 2, 1, 1, NOW(), NULL, NULL),
(10003, 'GitHub', 10000, 2, 'https://github.com/charles7c/continew-admin', NULL, NULL, NULL, 'github', true, false, false, NULL, 3, 1, 1, NOW(), NULL, NULL);

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
("id", "name", "code", "data_scope", "description", "sort", "is_system", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, '系统管理员', 'admin', 1, '系统初始角色', 1, true, 1, NOW(), NULL, NULL),
(547888897925840928, '测试人员', 'test', 5, NULL, 2, false, 1, NOW(), NULL, NULL);

-- 初始化默认用户：admin/admin123；test/123456
INSERT INTO "sys_user"
("id", "username", "nickname", "password", "gender", "email", "phone", "avatar", "description", "status", "is_system", "pwd_reset_time", "dept_id", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, 'admin', '系统管理员', '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa', 1, '42190c6c5639d2ca4edb4150a35e058559ccf8270361a23745a2fd285a273c28', '5bda89a4609a65546422ea56bfe5eab4', NULL, '系统初始用户', 1, true, NOW(), 1, 1, NOW(), NULL, NULL),
(547889293968801831, 'test', '测试员', '{bcrypt}$2a$10$meMbyso06lupZjxT88fG8undZo6.DSNUmifRfnnre8r/s13ciq6M6', 2, NULL, NULL, NULL, NULL, 2, false, NOW(), 547888483713155087, 1, NOW(), NULL, NULL);

-- 初始化默认参数
INSERT INTO "sys_option"
("id", "category", "name", "code", "value", "default_value", "description", "update_user", "update_time")
VALUES
(1, 'SITE', '系统标题', 'SITE_TITLE', NULL, 'ContiNew Admin', '用于显示登录页面的系统标题。', NULL, NULL),
(2, 'SITE', '系统描述', 'SITE_DESCRIPTION', NULL, '持续迭代优化的前后端分离中后台管理系统框架', NULL, NULL, NULL),
(3, 'SITE', '版权信息', 'SITE_COPYRIGHT', NULL, 'Copyright © 2022 - present ContiNew Admin 版权所有', '用于显示登录页面的底部版权信息。', NULL, NULL),
(4, 'SITE', '备案号', 'SITE_BEIAN', NULL, '津ICP备2022005864号-3', 'ICP备案号', NULL, NULL),
(5, 'SITE', 'favicon', 'SITE_FAVICON', NULL, '/favicon.ico', '用于显示浏览器地址栏的系统LOGO。', NULL, NULL),
(6, 'SITE', '系统LOGO', 'SITE_LOGO', NULL, '/logo.svg', '用于显示登录页面的系统LOGO。', NULL, NULL),
(7, 'PASSWORD', '登录密码错误锁定账号的次数', 'PASSWORD_ERROR_LOCK_COUNT', NULL, '5', '取值范围为 0-10（0 表示不锁定）。', NULL, NULL),
(8, 'PASSWORD', '登录密码错误锁定账号的时间（min）', 'PASSWORD_ERROR_LOCK_MINUTES', NULL, '5', '取值范围为 1-1440（一天）。', NULL, NULL),
(9, 'PASSWORD', '密码有效期（天）', 'PASSWORD_EXPIRATION_DAYS', NULL, '0', '取值范围为 0-999（0 表示永久有效）。', NULL, NULL),
(10, 'PASSWORD', '密码到期提前提示（天）', 'PASSWORD_EXPIRATION_WARNING_DAYS', NULL, '0', '密码到期 N 天前进行提示（0 表示不提示）。', NULL, NULL),
(11, 'PASSWORD', '密码重复使用次数', 'PASSWORD_REPETITION_TIMES', NULL, '3', '不允许使用最近 N 次密码，取值范围为 3-32。', NULL, NULL),
(12, 'PASSWORD', '密码最小长度', 'PASSWORD_MIN_LENGTH', NULL, '8', '取值范围为 8-32。', NULL, NULL),
(13, 'PASSWORD', '密码是否允许包含正反序账号名', 'PASSWORD_ALLOW_CONTAIN_USERNAME', NULL, '1', NULL, NULL, NULL),
(14, 'PASSWORD', '密码是否必须包含特殊字符', 'PASSWORD_REQUIRE_SYMBOLS', NULL, '0', NULL, NULL, NULL),
(15, 'MAIL', '发送协议', 'MAIL_PROTOCOL', NULL, 'smtp', NULL, NULL, NULL),
(16, 'MAIL', '服务器地址', 'MAIL_HOST', NULL, 'smtp.126.com', NULL, NULL, NULL),
(17, 'MAIL', '服务器端口', 'MAIL_PORT', NULL, '465', NULL, NULL, NULL),
(18, 'MAIL', '用户名', 'MAIL_USERNAME', NULL, 'charles7c@126.com', NULL, NULL, NULL),
(19, 'MAIL', '密码', 'MAIL_PASSWORD', NULL, NULL, NULL, NULL, NULL),
(20, 'MAIL', '是否启用SSL', 'MAIL_SSL_ENABLED', NULL, '1', NULL, NULL, NULL),
(21, 'MAIL', 'SSL端口', 'MAIL_SSL_PORT', NULL, '465', NULL, NULL, NULL);

-- 初始化默认字典
INSERT INTO "sys_dict"
("id", "name", "code", "description", "is_system", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, '公告类型', 'notice_type', NULL, true, 1, NOW(), NULL, NULL),
(2, '消息类型', 'message_type', NULL, true, 1, NOW(), NULL, NULL);

INSERT INTO "sys_dict_item"
("id", "label", "value", "color", "sort", "description", "status", "dict_id", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, '通知', '1', 'blue', 1, NULL, 1, 1, 1, NOW(), NULL, NULL),
(2, '活动', '2', 'orangered', 2, NULL, 1, 1, 1, NOW(), NULL, NULL),
(3, '安全消息', '1', 'blue', 1, NULL, 1, 2, 1, NOW(), NULL, NULL),
(4, '活动消息', '2', 'orangered', 2, NULL, 1, 2, 1, NOW(), NULL, NULL);

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
("id", "name", "code", "type", "access_key", "secret_key", "endpoint", "bucket_name", "domain", "description", "is_default", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
(1, '开发环境', 'local_dev', 2, NULL, NULL, NULL, 'C:/continew-admin/data/file/', 'http://localhost:8000/file', '本地存储', true, 1, 1, 1, NOW(), NULL, NULL),
(2, '生产环境', 'local_prod', 2, NULL, NULL, NULL, '../data/file/', 'http://api.continew.top/file', '本地存储', false, 2, 2, 1, NOW(), NULL, NULL);
