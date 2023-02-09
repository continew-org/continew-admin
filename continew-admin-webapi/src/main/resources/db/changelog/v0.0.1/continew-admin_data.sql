-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认部门
INSERT IGNORE INTO `sys_dept` VALUES (1, 'Xxx科技有限公司', 0, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (2, '天津总部', 1, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (3, '研发部', 2, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (4, 'UI部', 2, '系统初始部门', 2, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (5, '测试部', 2, '系统初始部门', 3, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (6, '运维部', 2, '系统初始部门', 4, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (7, '研发一组', 3, '系统初始部门', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_dept` VALUES (8, '研发二组', 3, '系统初始部门', 2, 2, 1, NOW(), 1, NOW());

-- 初始化默认用户：admin/admin123；test/123456
INSERT IGNORE INTO `sys_user` VALUES (1, 'admin', '超级管理员', '9802815bcc5baae7feb1ae0d0566baf2', 1, '18888888888', 'charles7c@126.com', NULL, '系统初始用户', 1, NOW(), 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_user` VALUES (2, 'test', '测试员', '8e114197e1b33783a00542ad67e80516', 0, NULL, NULL, NULL, '系统初始用户', 2, NOW(), 2, 1, NOW(), 1, NOW());

-- 初始化默认角色
INSERT IGNORE INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, NULL, '系统初始角色', 1, 1, 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_role` VALUES (2, '测试人员', 'test', 4, NULL, '系统初始角色', 2, 2, 1, NOW(), 1, NOW());

-- 初始化默认用户和角色关联数据
INSERT IGNORE INTO `sys_user_role` VALUES (1, 1);
INSERT IGNORE INTO `sys_user_role` VALUES (2, 2);
