-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', NULL, 'settings', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL),
(1010, '用户管理', 1000, 2, '/system/user', 'User', 'system/user/index', 'user', b'0', b'0', b'0', 'system:user:list', 1, 1, 1, NOW(), NULL, NULL),
(1011, '用户新增', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:add', 1, 1, 1, NOW(), NULL, NULL),
(1012, '用户修改', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:update', 2, 1, 1, NOW(), NULL, NULL),
(1013, '用户删除', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:delete', 3, 1, 1, NOW(), NULL, NULL),
(1014, '用户导出', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:export', 4, 1, 1, NOW(), NULL, NULL),
(1015, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:password:reset', 5, 1, 1, NOW(), NULL, NULL),
(1016, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:role:update', 6, 1, 1, NOW(), NULL, NULL),
(1020, '角色管理', 1000, 2, '/system/role', 'Role', 'system/role/index', 'safe', b'0', b'0', b'0', 'system:role:list', 2, 1, 1, NOW(), NULL, NULL),
(1021, '角色新增', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:add', 1, 1, 1, NOW(), NULL, NULL),
(1022, '角色修改', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:update', 2, 1, 1, NOW(), NULL, NULL),
(1023, '角色删除', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:delete', 3, 1, 1, NOW(), NULL, NULL),
(1024, '角色导出', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:export', 4, 1, 1, NOW(), NULL, NULL),
(1030, '部门管理', 1000, 2, '/system/dept', 'Dept', 'system/dept/index', 'user-group', b'0', b'0', b'0', 'system:dept:list', 3, 1, 1, NOW(), NULL, NULL),
(1031, '部门新增', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:add', 1, 1, 1, NOW(), NULL, NULL),
(1032, '部门修改', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:update', 2, 1, 1, NOW(), NULL, NULL),
(1033, '部门删除', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:delete', 3, 1, 1, NOW(), NULL, NULL),
(1034, '部门导出', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:export', 4, 1, 1, NOW(), NULL, NULL),
(1900, '菜单管理', 1000, 2, '/system/menu', 'Menu', 'system/menu/index', 'menu', b'0', b'0', b'0', 'system:menu:list', 999, 1, 1, NOW(), NULL, NULL),
(1901, '菜单新增', 1900, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:add', 1, 1, 1, NOW(), NULL, NULL),
(1902, '菜单修改', 1900, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:update', 2, 1, 1, NOW(), NULL, NULL),
(1903, '菜单删除', 1900, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:delete', 3, 1, 1, NOW(), NULL, NULL),
(1904, '菜单导出', 1900, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:export', 4, 1, 1, NOW(), NULL, NULL),
(9000, '系统监控', 0, 1, '/monitor', 'Monitor', NULL, 'computer', b'0', b'0', b'0', NULL, 899, 1, 1, NOW(), NULL, NULL),
(9010, '在线用户', 9000, 2, '/monitor/online', 'OnlineUser', 'monitor/online/index', 'anonymity', b'0', b'0', b'0', 'monitor:online:user:list', 1, 1, 1, NOW(), NULL, NULL),
(9011, '强退用户', 9010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'monitor:online:user:delete', 1, 1, 1, NOW(), NULL, NULL),
(9030, '登录日志', 9000, 2, '/monitor/log/login', 'LoginLog', 'monitor/log/login/index', 'old-version', b'0', b'0', b'0', 'monitor:log:login:list', 2, 1, 1, NOW(), NULL, NULL),
(9050, '操作日志', 9000, 2, '/monitor/log/operation', 'OperationLog', 'monitor/log/operation/index', 'history', b'0', b'0', b'0', 'monitor:log:operation:list', 3, 1, 1, NOW(), NULL, NULL),
(9070, '系统日志', 9000, 2, '/monitor/log/system', 'SystemLog', 'monitor/log/system/index', 'behavior-anal', b'0', b'0', b'0', 'monitor:log:system:list', 4, 1, 1, NOW(), NULL, NULL),
(10000, 'Arco Design Vue', 0, 1, 'https://arco.design/vue/docs/start', NULL, NULL, 'link', b'1', b'0', b'0', NULL, 998, 1, 1, NOW(), NULL, NULL),
(10001, 'GitHub', 0, 1, 'https://github.com/Charles7c/continew-admin', NULL, NULL, 'github', b'1', b'0', b'0', NULL, 999, 1, 1, NOW(), NULL, NULL);

-- 初始化默认部门
INSERT IGNORE INTO `sys_dept`
(`id`, `name`, `parent_id`, `ancestors`, `description`, `sort`, `status`, `type`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, 'Xxx科技有限公司', 0, '0', '系统初始部门', 1, 1, 1, 1, NOW(), NULL, NULL),
(2, '天津总部', 1, '0,1', NULL, 1, 1, 2, 1, NOW(), NULL, NULL),
(3, '研发部', 2, '0,1,2', NULL, 1, 1, 2, 1, NOW(), NULL, NULL),
(4, 'UI部', 2, '0,1,2', NULL, 2, 1, 2, 1, NOW(), NULL, NULL),
(5, '测试部', 2, '0,1,2', NULL, 3, 1, 2, 1, NOW(), NULL, NULL),
(6, '运维部', 2, '0,1,2', NULL, 4, 1, 2, 1, NOW(), NULL, NULL),
(7, '研发一组', 3, '0,1,2,3', NULL, 1, 1, 2, 1, NOW(), NULL, NULL),
(8, '研发二组', 3, '0,1,2,3', NULL, 2, 2, 2, 1, NOW(), NULL, NULL);

-- 初始化默认角色
INSERT IGNORE INTO `sys_role`
(`id`, `name`, `code`, `data_scope`, `description`, `sort`, `status`, `type`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, '超级管理员', 'admin', 1, '系统初始角色', 1, 1, 1, 1, NOW(), NULL, NULL),
(2, '测试人员', 'test', 5, NULL, 2, 1, 2, 1, NOW(), NULL, NULL);

-- 初始化默认用户：admin/admin123；test/123456
INSERT IGNORE INTO `sys_user`
(`id`, `username`, `nickname`, `password`, `gender`, `email`, `phone`, `avatar`, `description`, `status`, `type`, `pwd_reset_time`, `dept_id`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
(1, 'admin', '超级管理员', '9802815bcc5baae7feb1ae0d0566baf2', 1, 'charles7c@126.com', '18888888888', NULL, '系统初始用户', 1, 1, NOW(), 1, 1, NOW(), NULL, NULL),
(2, 'test', '测试员', '8e114197e1b33783a00542ad67e80516', 2, NULL, NULL, NULL, NULL, 2, 2, NOW(), 5, 1, NOW(), NULL, NULL);

-- 初始化默认角色和菜单关联数据
INSERT IGNORE INTO `sys_role_menu`
(`role_id`, `menu_id`)
VALUES
(2, 1000),
(2, 1010),
(2, 1011),
(2, 1012),
(2, 1013),
(2, 1014);

-- 初始化默认角色和部门关联数据
INSERT IGNORE INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 5);

-- 初始化默认用户和角色关联数据
INSERT IGNORE INTO `sys_user_role`
(`user_id`, `role_id`)
VALUES
(1, 1),
(2, 2);