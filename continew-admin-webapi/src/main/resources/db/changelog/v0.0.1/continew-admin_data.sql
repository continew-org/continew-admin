-- liquibase formatted sql

-- changeset Charles7c:1
-- 初始化默认用户：admin/123456；test/123456
INSERT IGNORE INTO `sys_user` VALUES (1, 'admin', '超级管理员', '9802815bcc5baae7feb1ae0d0566baf2', 1, '18888888888', 'charles7c@126.com', NULL, NULL, 1, NOW(), 1, NOW(), 1, NOW());
INSERT IGNORE INTO `sys_user` VALUES (2, 'test', '测试员', '8e114197e1b33783a00542ad67e80516', 0, NULL, NULL, NULL, NULL, 2, NOW(), 1, NOW(), 1, NOW());
