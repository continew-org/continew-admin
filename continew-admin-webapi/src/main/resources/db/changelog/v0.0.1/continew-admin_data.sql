-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认菜单
INSERT IGNORE INTO `sys_menu` VALUES (1000, '系统管理', 0, 1, 'system', NULL, NULL, 'settings', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1010, '用户管理', 1000, 2, '/system/user', 'User', '/system/user/index', NULL, b'0', b'0', b'0', 'system:user:list', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1011, '用户新增', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:add', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1012, '用户修改', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:update', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1013, '用户删除', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:delete', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1014, '用户导出', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:export', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1015, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:password:reset', 5, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1016, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:user:role:update', 6, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1020, '角色管理', 1000, 2, '/system/role', 'Role', 'system/role/index', NULL, b'0', b'0', b'0', 'system:role:list', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1021, '角色新增', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:add', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1022, '角色修改', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:update', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1023, '角色删除', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:delete', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1024, '角色导出', 1020, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:role:export', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1030, '菜单管理', 1000, 2, '/system/menu', 'Menu', 'system/menu/index', NULL, b'0', b'0', b'0', 'system:menu:list', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1031, '菜单新增', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:add', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1032, '菜单修改', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:update', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1033, '菜单删除', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:delete', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1034, '菜单导出', 1030, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:menu:export', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1040, '部门管理', 1000, 2, '/system/dept', 'Dept', 'system/dept/index', NULL, b'0', b'0', b'0', 'system:dept:list', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1041, '部门新增', 1040, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:add', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1042, '部门修改', 1040, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:update', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1043, '部门删除', 1040, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:delete', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (1044, '部门导出', 1040, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:dept:export', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2000, '系统监控', 0, 1, 'monitor', NULL, NULL, 'computer', b'0', b'0', b'0', NULL, 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2010, '在线用户', 2000, 2, '/monitor/online', 'OnlineUser', 'monitor/online/index', NULL, b'0', b'0', b'0', 'monitor:online:user:list', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2011, '强退用户', 2010, 3, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'monitor:online:user:delete', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2030, '登录日志', 2000, 2, '/monitor/log/login', 'LoginLog', 'monitor/log/login/index', NULL, b'0', b'0', b'0', 'monitor:log:login:list', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2050, '操作日志', 2000, 2, '/monitor/log/operation', 'OperationLog', 'monitor/log/operation/index', NULL, b'0', b'0', b'0', 'monitor:log:operation:list', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (2070, '系统日志', 2000, 2, '/monitor/log/system', 'SystemLog', 'monitor/log/system/index', NULL, b'0', b'0', b'0', 'monitor:log:system:list', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (10000, 'Arco Design Vue', 0, 1, 'https://arco.design/vue/docs/start', NULL, NULL, 'link', b'1', b'0', b'0', NULL, 100, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_menu` VALUES (10001, 'GitHub', 0, 1, 'https://github.com/Charles7c/continew-admin', NULL, NULL, 'github', b'1', b'0', b'0', NULL, 101, 1, 1, NOW(), 1, NOW());

-- 初始化默认部门
INSERT IGNORE INTO `sys_dept` VALUES (1, 'Xxx科技有限公司', 0, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (2, '天津总部', 1, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (3, '研发部', 2, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (4, 'UI部', 2, '系统初始部门', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (5, '测试部', 2, '系统初始部门', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (6, '运维部', 2, '系统初始部门', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (7, '研发一组', 3, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (8, '研发二组', 3, '系统初始部门', 2, 2, 1, NOW(), 1, NOW());

-- 初始化默认角色
INSERT IGNORE INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '系统初始角色', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_role` VALUES (2, '测试人员', 'test', 5, '系统初始角色', 2, 2, 1, NOW(), 1, NOW());

-- 初始化默认用户：admin/admin123；test/123456
INSERT IGNORE INTO `sys_user` VALUES (1, 'admin', '超级管理员', '9802815bcc5baae7feb1ae0d0566baf2', 1, 'charles7c@126.com', '18888888888', NULL, '系统初始用户', 1, NOW(), 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_user` VALUES (2, 'test', '测试员', '8e114197e1b33783a00542ad67e80516', 2, NULL, NULL, NULL, '系统初始用户', 2, NOW(), 2, 1, NOW(), 1, NOW());

-- 初始化默认角色和菜单关联数据
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1000);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1010);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1011);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1012);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1013);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1014);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1030);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1031);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1032);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1033);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1034);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1050);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1051);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1052);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1053);
INSERT IGNORE INTO `sys_role_menu` VALUES (2, 1054);

-- 初始化默认角色和部门关联数据
INSERT IGNORE INTO `sys_role_dept` VALUES (2, 5);

-- 初始化默认用户和角色关联数据
INSERT IGNORE INTO `sys_user_role` VALUES (1, 1);
INSERT IGNORE INTO `sys_user_role` VALUES (2, 2);