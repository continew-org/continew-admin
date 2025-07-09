-- liquibase formatted sql

-- changeset charles7c:1
-- comment 初始化表数据
-- 初始化默认菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', 'Layout', '/system/user', 'settings', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(1010, '用户管理', 1000, 2, '/system/user', 'SystemUser', 'system/user/index', NULL, 'user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(1011, '列表', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:list', 1, 1, 1, NOW()),
(1012, '详情', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:get', 2, 1, 1, NOW()),
(1013, '新增', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:create', 3, 1, 1, NOW()),
(1014, '修改', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:update', 4, 1, 1, NOW()),
(1015, '删除', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:delete', 5, 1, 1, NOW()),
(1016, '导出', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:export', 6, 1, 1, NOW()),
(1017, '导入', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:import', 7, 1, 1, NOW()),
(1018, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:resetPwd', 8, 1, 1, NOW()),
(1019, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:updateRole', 9, 1, 1, NOW()),

(1030, '角色管理', 1000, 2, '/system/role', 'SystemRole', 'system/role/index', NULL, 'user-group', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(1031, '列表', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:list', 1, 1, 1, NOW()),
(1032, '详情', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:get', 2, 1, 1, NOW()),
(1033, '新增', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:create', 3, 1, 1, NOW()),
(1034, '修改', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:update', 4, 1, 1, NOW()),
(1035, '删除', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:delete', 5, 1, 1, NOW()),
(1036, '修改权限', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:updatePermission', 6, 1, 1, NOW()),
(1037, '分配', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:assign', 7, 1, 1, NOW()),
(1038, '取消分配', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:unassign', 8, 1, 1, NOW()),

(1050, '菜单管理', 1000, 2, '/system/menu', 'SystemMenu', 'system/menu/index', NULL, 'menu', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(1051, '列表', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:list', 1, 1, 1, NOW()),
(1052, '详情', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:get', 2, 1, 1, NOW()),
(1053, '新增', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:create', 3, 1, 1, NOW()),
(1054, '修改', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:update', 4, 1, 1, NOW()),
(1055, '删除', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:delete', 5, 1, 1, NOW()),
(1056, '清除缓存', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:clearCache', 6, 1, 1, NOW()),

(1070, '部门管理', 1000, 2, '/system/dept', 'SystemDept', 'system/dept/index', NULL, 'mind-mapping', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(1071, '列表', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:list', 1, 1, 1, NOW()),
(1072, '详情', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:get', 2, 1, 1, NOW()),
(1073, '新增', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:create', 3, 1, 1, NOW()),
(1074, '修改', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:update', 4, 1, 1, NOW()),
(1075, '删除', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:delete', 5, 1, 1, NOW()),
(1076, '导出', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:export', 6, 1, 1, NOW()),

(1090, '通知公告', 1000, 2, '/system/notice', 'SystemNotice', 'system/notice/index', NULL, 'notification', b'0', b'0', b'0', NULL, 5, 1, 1, NOW()),
(1091, '列表', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:list', 1, 1, 1, NOW()),
(1092, '详情', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:get', 2, 1, 1, NOW()),
(1093, '查看公告', 1090, 2, '/system/notice/view', 'SystemNoticeView', 'system/notice/view/index', NULL, NULL, b'0', b'0', b'1', 'system:notice:view', 3, 1, 1, NOW()),
(1094, '发布公告', 1090, 2, '/system/notice/add', 'SystemNoticeAdd', 'system/notice/add/index', NULL, NULL, b'0', b'0', b'1', 'system:notice:create', 4, 1, 1, NOW()),
(1095, '修改', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:update', 5, 1, 1, NOW()),
(1096, '删除', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:delete', 6, 1, 1, NOW()),

(1110, '文件管理', 1000, 2, '/system/file', 'SystemFile', 'system/file/index', NULL, 'file', b'0', b'0', b'0', NULL, 6, 1, 1, NOW()),
(1111, '列表', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:list', 1, 1, 1, NOW()),
(1112, '详情', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:get', 2, 1, 1, NOW()),
(1113, '上传', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:upload', 3, 1, 1, NOW()),
(1114, '修改', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:update', 4, 1, 1, NOW()),
(1115, '删除', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:delete', 5, 1, 1, NOW()),
(1116, '下载', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:download', 6, 1, 1, NOW()),
(1117, '创建文件夹', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:createDir', 7, 1, 1, NOW()),
(1118, '计算文件夹大小', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:file:calcDirSize', 8, 1, 1, NOW()),

(1130, '字典管理', 1000, 2, '/system/dict', 'SystemDict', 'system/dict/index', NULL, 'bookmark', b'0', b'0', b'0', NULL, 7, 1, 1, NOW()),
(1131, '列表', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:list', 1, 1, 1, NOW()),
(1132, '详情', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:get', 2, 1, 1, NOW()),
(1133, '新增', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:create', 3, 1, 1, NOW()),
(1134, '修改', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:update', 4, 1, 1, NOW()),
(1135, '删除', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:delete', 5, 1, 1, NOW()),
(1136, '清除缓存', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:clearCache', 6, 1, 1, NOW()),
(1140, '字典项管理', 1000, 2, '/system/dict/item', 'SystemDictItem', 'system/dict/item/index', NULL, 'bookmark', b'0', b'0', b'1', NULL, 8, 1, 1, NOW()),
(1141, '列表', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:list', 1, 1, 1, NOW()),
(1142, '详情', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:get', 2, 1, 1, NOW()),
(1143, '新增', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:create', 3, 1, 1, NOW()),
(1144, '修改', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:update', 4, 1, 1, NOW()),
(1145, '删除', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:delete', 5, 1, 1, NOW()),

(1150, '系统配置', 1000, 2, '/system/config', 'SystemConfig', 'system/config/index', NULL, 'config', b'0', b'0', b'0', NULL, 999, 1, 1, NOW()),
(1160, '网站配置', 1150, 2, '/system/config?tab=site', 'SystemSiteConfig', 'system/config/site/index', NULL, 'apps', b'0', b'0', b'1', NULL, 1, 1, 1, NOW()),
(1161, '查询', 1160, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:siteConfig:get', 1, 1, 1, NOW()),
(1162, '修改', 1160, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:siteConfig:update', 2, 1, 1, NOW()),
(1170, '安全配置', 1150, 2, '/system/config?tab=security', 'SystemSecurityConfig', 'system/config/security/index', NULL, 'safe', b'0', b'0', b'1', NULL, 2, 1, 1, NOW()),
(1171, '查询', 1170, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:securityConfig:get', 1, 1, 1, NOW()),
(1172, '修改', 1170, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:securityConfig:update', 2, 1, 1, NOW()),
(1180, '登录配置', 1150, 2, '/system/config?tab=login', 'SystemLoginConfig', 'system/config/login/index', NULL, 'lock', b'0', b'0', b'1', NULL, 3, 1, 1, NOW()),
(1181, '查询', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:loginConfig:get', 1, 1, 1, NOW()),
(1182, '修改', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:loginConfig:update', 2, 1, 1, NOW()),
(1190, '邮件配置', 1150, 2, '/system/config?tab=mail', 'SystemMailConfig', 'system/config/mail/index', NULL, 'email', b'0', b'0', b'1', NULL, 4, 1, 1, NOW()),
(1191, '查询', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:mailConfig:get', 1, 1, 1, NOW()),
(1192, '修改', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:mailConfig:update', 2, 1, 1, NOW()),
(1210, '短信配置', 1150, 2, '/system/config?tab=sms', 'SystemSmsConfig', 'system/config/sms/index', NULL, 'message', b'0', b'0', b'1', NULL, 5, 1, 1, NOW()),
(1211, '列表', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:list', 1, 1, 1, NOW()),
(1212, '详情', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:get', 2, 1, 1, NOW()),
(1213, '新增', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:create', 3, 1, 1, NOW()),
(1214, '修改', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:update', 4, 1, 1, NOW()),
(1215, '删除', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:delete', 5, 1, 1, NOW()),
(1216, '导出', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:export', 6, 1, 1, NOW()),
(1217, '设为默认配置', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:setDefault', 7, 1, 1, NOW()),
(1230, '存储配置', 1150, 2, '/system/config?tab=storage', 'SystemStorage', 'system/config/storage/index', NULL, 'storage', b'0', b'0', b'1', NULL, 6, 1, 1, NOW()),
(1231, '列表', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:list', 1, 1, 1, NOW()),
(1232, '详情', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:get', 2, 1, 1, NOW()),
(1233, '新增', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:create', 3, 1, 1, NOW()),
(1234, '修改', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:update', 4, 1, 1, NOW()),
(1235, '删除', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:delete', 5, 1, 1, NOW()),
(1236, '修改状态', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:updateStatus', 6, 1, 1, NOW()),
(1237, '设为默认存储', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:setDefault', 7, 1, 1, NOW()),
(1250, '客户端配置', 1150, 2, '/system/config?tab=client', 'SystemClient', 'system/config/client/index', NULL, 'mobile', b'0', b'0', b'1', NULL, 7, 1, 1, NOW()),
(1251, '列表', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:list', 1, 1, 1, NOW()),
(1252, '详情', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:get', 2, 1, 1, NOW()),
(1253, '新增', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:create', 3, 1, 1, NOW()),
(1254, '修改', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:update', 4, 1, 1, NOW()),
(1255, '删除', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:delete', 5, 1, 1, NOW()),

(2000, '系统监控', 0, 1, '/monitor', 'Monitor', 'Layout', '/monitor/online', 'computer', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(2010, '在线用户', 2000, 2, '/monitor/online', 'MonitorOnline', 'monitor/online/index', NULL, 'user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(2011, '列表', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:list', 1, 1, 1, NOW()),
(2012, '强退', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:kickout', 2, 1, 1, NOW()),

(2030, '系统日志', 2000, 2, '/monitor/log', 'MonitorLog', 'monitor/log/index', NULL, 'history', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(2031, '列表', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:list', 1, 1, 1, NOW()),
(2032, '详情', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:get', 2, 1, 1, NOW()),
(2033, '导出', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:export', 3, 1, 1, NOW()),

(2050, '短信日志', 2000, 2, '/system/sms/log', 'SystemSmsLog', 'monitor/sms/log/index', NULL, 'message', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(2051, '列表', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:list', 1, 1, 1, NOW()),
(2052, '删除', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:delete', 2, 1, 1, NOW()),
(2053, '导出', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:export', 3, 1, 1, NOW());

-- 初始化默认部门
INSERT INTO `sys_dept`
(`id`, `name`, `parent_id`, `ancestors`, `description`, `sort`, `status`, `is_system`, `create_user`, `create_time`)
VALUES
(1, 'Xxx科技有限公司', 0, '0', '系统初始部门', 1, 1, b'1', 1, NOW()),
(547887852587843590, 'Xxx（天津）科技有限公司', 1, '0,1', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843591, '研发部', 547887852587843590, '0,1,547887852587843590', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843592, 'UI部', 547887852587843590, '0,1,547887852587843590', NULL, 2, 1, b'0', 1, NOW()),
(547887852587843593, '测试部', 547887852587843590, '0,1,547887852587843590', NULL, 3, 1, b'0', 1, NOW()),
(547887852587843594, '运维部', 547887852587843590, '0,1,547887852587843590', NULL, 4, 1, b'0', 1, NOW()),
(547887852587843595, '研发一组', 547887852587843591, '0,1,547887852587843590,547887852587843591', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843596, '研发二组', 547887852587843591, '0,1,547887852587843590,547887852587843591', NULL, 2, 2, b'0', 1, NOW()),

(547887852587843597, 'Xxx（四川）科技有限公司', 1, '0,1', NULL, 2, 1, b'0', 1, NOW()),
(547887852587843598, '研发部', 547887852587843597, '0,1,547887852587843597', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843599, '研发一组', 547887852587843598, '0,1,547887852587843597,547887852587843598', NULL, 1, 1, b'0', 1, NOW()),

(547887852587843600, 'Xxx（江西）科技有限公司', 1, '0,1', NULL, 3, 1, b'0', 1, NOW()),
(547887852587843601, '研发部', 547887852587843600, '0,1,547887852587843600', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843602, '研发一组', 547887852587843601, '0,1,547887852587843600,547887852587843601', NULL, 1, 1, b'0', 1, NOW()),

(547887852587843603, 'Xxx（江苏）科技有限公司', 1, '0,1', NULL, 4, 1, b'0', 1, NOW()),
(547887852587843604, '研发部', 547887852587843603, '0,1,547887852587843603', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843605, '研发一组', 547887852587843604, '0,1,547887852587843603,547887852587843604', NULL, 1, 1, b'0', 1, NOW()),

(547887852587843606, 'Xxx（浙江）科技有限公司', 1, '0,1', NULL, 5, 1, b'0', 1, NOW()),
(547887852587843607, '研发部', 547887852587843606, '0,1,547887852587843606', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843608, '研发一组', 547887852587843607, '0,1,547887852587843606,547887852587843607', NULL, 1, 1, b'0', 1, NOW()),

(547887852587843609, 'Xxx（湖南）科技有限公司', 1, '0,1', NULL, 6, 1, b'0', 1, NOW()),
(547887852587843610, '研发部', 547887852587843609, '0,1,547887852587843609', NULL, 1, 1, b'0', 1, NOW()),
(547887852587843611, '研发一组', 547887852587843610, '0,1,547887852587843609,547887852587843610', NULL, 1, 1, b'0', 1, NOW());

-- 初始化默认角色
INSERT INTO `sys_role`
(`id`, `name`, `code`, `data_scope`, `description`, `sort`, `is_system`, `create_user`, `create_time`)
VALUES
(1, '系统管理员', 'admin', 1, '系统初始角色', 1, b'1', 1, NOW()),
(2, '普通用户', 'general', 4, '系统初始角色', 2, b'1', 1, NOW()),
(547888897925840927, '测试人员', 'tester', 5, NULL, 3, b'0', 1, NOW()),
(547888897925840928, '研发人员', 'developer', 4, NULL, 4, b'0', 1, NOW());

-- 初始化默认用户：admin/admin123；test/test123
INSERT INTO `sys_user`
(`id`, `username`, `nickname`, `password`, `gender`, `email`, `phone`, `avatar`, `description`, `status`, `is_system`, `pwd_reset_time`, `dept_id`, `create_user`, `create_time`)
VALUES
(1, 'admin', '系统管理员', '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa', 1, '42190c6c5639d2ca4edb4150a35e058559ccf8270361a23745a2fd285a273c28', '5bda89a4609a65546422ea56bfe5eab4', NULL, '系统初始用户', 1, b'1', NOW(), 1, 1, NOW()),
(547889293968801822, 'test', '测试员', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 2, NULL, NULL, NULL, NULL, 1, b'0', NOW(), 547887852587843593, 1, NOW()),
(547889293968801823, 'Charles', 'Charles', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '代码写到极致，就是艺术。', 1, b'0', NOW(), 547887852587843595, 1, NOW()),
(547889293968801824, 'Yoofff', 'Yoofff', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '弱小和无知不是生存的障碍，傲慢才是。', 1, b'0', NOW(), 1, 1, NOW()),
(547889293968801825, 'Jasmine', 'Jasmine', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '干就完事了！', 1, b'0', NOW(), 547887852587843605, 1, NOW()),
(547889293968801826, 'AutumnSail', '秋登', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '只有追求完美，才能创造奇迹。', 1, b'0', NOW(), 547887852587843602, 1, NOW()),
(547889293968801827, 'Kils', 'Kils', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '可以摆烂，但不能真的菜。', 1, b'0', NOW(), 547887852587843599, 1, NOW()),
(547889293968801828, 'mochou', '莫愁', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '万事莫愁，皆得所愿。', 1, b'0', NOW(), 547887852587843602, 1, NOW()),
(547889293968801829, 'Jing', 'MS-Jing', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '路虽远，行则将至。', 2, b'0', NOW(), 547887852587843599, 1, NOW()),
(547889293968801830, 'domw', '梓陌', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '胜利是奖赏，挫折是常态。', 1, b'0', NOW(), 547887852587843608, 1, NOW()),
(547889293968801831, 'xtanyu', '小熊', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL, '不想上班。', 1, b'0', NOW(), 547887852587843611, 1, NOW());

-- 初始化默认参数
INSERT INTO `sys_option`
(`id`, `category`, `name`, `code`, `value`, `default_value`, `description`)
VALUES
(1, 'SITE', '系统名称', 'SITE_TITLE', NULL, 'ContiNew Admin', '显示在浏览器标题栏和登录界面的系统名称'),
(2, 'SITE', '系统描述', 'SITE_DESCRIPTION', NULL, '持续迭代优化的前后端分离中后台管理系统框架', '用于 SEO 的网站元描述'),
(3, 'SITE', '版权声明', 'SITE_COPYRIGHT', NULL, 'Copyright © 2022 - present ContiNew Admin 版权所有', '显示在页面底部的版权声明文本'),
(4, 'SITE', '备案号', 'SITE_BEIAN', NULL, NULL, '工信部 ICP 备案编号（如：京ICP备12345678号）'),
(5, 'SITE', '系统图标', 'SITE_FAVICON', NULL, '/favicon.ico', '浏览器标签页显示的网站图标（建议 .ico 格式）'),
(6, 'SITE', '系统LOGO', 'SITE_LOGO', NULL, '/logo.svg', '显示在登录页面和系统导航栏的网站图标（建议 .svg 格式）'),
(10, 'PASSWORD', '密码错误锁定阈值', 'PASSWORD_ERROR_LOCK_COUNT', NULL, '5', '连续登录失败次数达到该值将锁定账号（0-10次，0表示禁用锁定）'),
(11, 'PASSWORD', '账号锁定时长（分钟）', 'PASSWORD_ERROR_LOCK_MINUTES', NULL, '5', '账号锁定后自动解锁的时间（1-1440分钟，即24小时）'),
(12, 'PASSWORD', '密码有效期（天）', 'PASSWORD_EXPIRATION_DAYS', NULL, '0', '密码强制修改周期（0-999天，0表示永不过期）'),
(13, 'PASSWORD', '密码到期提醒（天）', 'PASSWORD_EXPIRATION_WARNING_DAYS', NULL, '0', '密码过期前的提前提醒天数（0表示不提醒）'),
(14, 'PASSWORD', '历史密码重复校验次数', 'PASSWORD_REPETITION_TIMES', NULL, '3', '禁止使用最近 N 次的历史密码（3-32次）'),
(15, 'PASSWORD', '密码最小长度', 'PASSWORD_MIN_LENGTH', NULL, '8', '密码最小字符长度要求（8-32个字符）'),
(16, 'PASSWORD', '是否允许密码包含用户名', 'PASSWORD_ALLOW_CONTAIN_USERNAME', NULL, '1', '是否允许密码包含正序或倒序的用户名字符'),
(17, 'PASSWORD', '密码是否必须包含特殊字符', 'PASSWORD_REQUIRE_SYMBOLS', NULL, '0', '是否要求密码必须包含特殊字符（如：!@#$%）'),
(20, 'MAIL', '邮件协议', 'MAIL_PROTOCOL', NULL, 'smtp', '邮件发送协议类型'),
(21, 'MAIL', '服务器地址', 'MAIL_HOST', NULL, 'smtp.126.com', '邮件服务器地址'),
(22, 'MAIL', '服务器端口', 'MAIL_PORT', NULL, '465', '邮件服务器连接端口'),
(23, 'MAIL', '邮箱账号', 'MAIL_USERNAME', NULL, 'charles7c@126.com', '发件人邮箱地址'),
(24, 'MAIL', '邮箱密码', 'MAIL_PASSWORD', NULL, NULL, '服务授权密码/客户端专用密码'),
(25, 'MAIL', '启用SSL加密', 'MAIL_SSL_ENABLED', NULL, '1', '是否启用SSL/TLS加密连接'),
(26, 'MAIL', 'SSL端口号', 'MAIL_SSL_PORT', NULL, '465', 'SSL加密连接的备用端口（通常与主端口一致）'),
(27, 'LOGIN', '是否启用验证码', 'LOGIN_CAPTCHA_ENABLED', NULL, '1', NULL);

-- 初始化默认字典
INSERT INTO `sys_dict`
(`id`, `name`, `code`, `description`, `is_system`, `create_user`, `create_time`)
VALUES
(1, '公告分类', 'notice_type', NULL, b'1', 1, NOW()),
(2, '客户端类型', 'client_type', NULL, b'1', 1, NOW()),
(3, '短信厂商', 'sms_supplier', NULL, b'1', 1, NOW());

INSERT INTO `sys_dict_item`
(`id`, `label`, `value`, `color`, `sort`, `description`, `status`, `dict_id`, `create_user`, `create_time`)
VALUES
(1, '产品新闻', '1', 'primary', 1, NULL, 1, 1, 1, NOW()),
(2, '企业动态', '2', 'success', 2, NULL, 1, 1, 1, NOW()),
(3, '桌面端', 'PC', 'primary', 1, NULL, 1, 2, 1, NOW()),
(4, '安卓', 'ANDROID', 'success', 2, NULL, 1, 2, 1, NOW()),
(5, '小程序', 'XCX', 'warning', 3, NULL, 1, 2, 1, NOW()),
(6, '阿里云', 'alibaba', 'warning', 1, NULL, 1, 3, 1, NOW()),
(7, '腾讯云', 'tencent', 'primary', 2, NULL, 1, 3, 1, NOW()),
(8, '容联云', 'cloopen', 'success', 3, NULL, 1, 3, 1, NOW());

-- 初始化默认用户和角色关联数据
INSERT INTO `sys_user_role`
(`id`, `user_id`, `role_id`)
VALUES
(1, 1, 1),
(2, 547889293968801822, 547888897925840927),
(3, 547889293968801823, 547888897925840928),
(4, 547889293968801824, 547888897925840928),
(5, 547889293968801825, 547888897925840928),
(6, 547889293968801826, 547888897925840928),
(7, 547889293968801827, 547888897925840928),
(8, 547889293968801828, 547888897925840928),
(9, 547889293968801829, 547888897925840928),
(10, 547889293968801830, 547888897925840928),
(11, 547889293968801831, 547888897925840928);

-- 初始化默认角色和菜单关联数据
INSERT INTO `sys_role_menu`
(`role_id`, `menu_id`)
VALUES
(547888897925840927, 1000),
(547888897925840927, 1010),
(547888897925840927, 1011),
(547888897925840927, 1012),
(547888897925840927, 1013),
(547888897925840927, 1014),
(547888897925840928, 2000),
(547888897925840928, 2010),
(547888897925840928, 2011),
(547888897925840928, 2020),
(547888897925840928, 2021),
(547888897925840928, 2022),
(547888897925840928, 2023);

-- 初始化默认角色和部门关联数据
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (547888897925840927, 547887852587843593);

-- 初始化默认存储
INSERT INTO `sys_storage`
(`id`, `name`, `code`, `type`, `access_key`, `secret_key`, `endpoint`, `bucket_name`, `domain`, `description`, `is_default`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(1, '开发环境', 'local_dev', 1, NULL, NULL, NULL, 'C:/continew-admin/data/file/', 'http://localhost:8000/file/', '本地存储', b'1', 1, 1, 1, NOW()),
(2, '生产环境', 'local_prod', 1, NULL, NULL, NULL, '../data/file/', 'http://api.continew.top/file/', '本地存储', b'0', 2, 2, 1, NOW());

-- 初始化客户端数据
INSERT INTO `sys_client`
(`id`, `client_id`, `client_type`, `auth_type`, `active_timeout`, `timeout`, `status`, `create_user`, `create_time`)
VALUES
(1, 'ef51c9a3e9046c4f2ea45142c8a8344a', 'PC', '["ACCOUNT", "EMAIL", "PHONE", "SOCIAL"]', 1800, 86400, 1, 1, NOW());
