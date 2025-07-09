-- liquibase formatted sql

-- changeset 小熊:1
-- comment 初始化表结构

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
`id` bigint NOT NULL COMMENT 'ID',
`name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '租户名称',
`tenant_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '租户编号',
`user_id` bigint DEFAULT NULL COMMENT '租户对应的用户',
`domain` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '绑定的域名',
`package_id` bigint NOT NULL COMMENT '租户套餐编号',
`status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态（1：启用；2：禁用）',
`expire_time` datetime DEFAULT NULL COMMENT '租户过期时间',
`create_user` bigint NOT NULL COMMENT '创建人',
`create_time` datetime NOT NULL COMMENT '创建时间',
`update_user` bigint DEFAULT NULL COMMENT '修改人',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`isolation_level` tinyint unsigned NOT NULL COMMENT '隔离级别',
`db_connect_id`  bigint DEFAULT NULL COMMENT '数据连接ID',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

-- ----------------------------
-- Table structure for sys_tenant_package
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_package`;
CREATE TABLE `sys_tenant_package` (
 `id` bigint NOT NULL COMMENT 'ID',
 `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '套餐名称',
 `menu_ids` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联的菜单ids',
 `menu_check_strictly` bit(1) DEFAULT b'0' COMMENT '菜单选择是否父子节点关联',
 `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态（1：启用；2：禁用）',
 `create_user` bigint NOT NULL COMMENT '创建人',
 `create_time` datetime NOT NULL COMMENT '创建时间',
 `update_user` bigint DEFAULT NULL COMMENT '修改人',
 `update_time` datetime DEFAULT NULL COMMENT '修改时间',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户套餐表';

-- ----------------------------
-- Table structure for sys_tenant_db_connect
-- ----------------------------
CREATE TABLE `sys_tenant_db_connect` (
  `id` bigint NOT NULL COMMENT 'ID',
  `connect_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接名称',
  `type` tinyint(1) NOT NULL COMMENT '连接类型',
  `host` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接主机地址',
  `port` smallint NOT NULL COMMENT '连接端口',
  `username` varchar(128) NOT NULL COMMENT '连接用户名',
  `password` varchar(128) NOT NULL COMMENT '连接密码',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户数据库连接表';


-- changeset 小熊:2
-- comment 添加租户列和索引
ALTER TABLE sys_app ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_app_tenant_id ON sys_app(tenant_id);
ALTER TABLE sys_dept ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_dept_tenant_id ON sys_dept(tenant_id);
ALTER TABLE sys_file ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_file_tenant_id ON sys_file(tenant_id);
ALTER TABLE sys_log ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_log_tenant_id ON sys_log(tenant_id);
ALTER TABLE sys_menu ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_menu_tenant_id ON sys_menu(tenant_id);
ALTER TABLE sys_message ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_message_tenant_id ON sys_message(tenant_id);
ALTER TABLE sys_message_log ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_message_log_tenant_id ON sys_message_log(tenant_id);
ALTER TABLE sys_notice ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_notice_tenant_id ON sys_notice(tenant_id);
ALTER TABLE sys_notice_log ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_notice_log_tenant_id ON sys_notice_log(tenant_id);
ALTER TABLE sys_role ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_role_tenant_id ON sys_role(tenant_id);
ALTER TABLE sys_user ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_user_tenant_id ON sys_user(tenant_id);
ALTER TABLE sys_role_dept ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_role_dept_tenant_id ON sys_role_dept(tenant_id);
ALTER TABLE sys_role_menu ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_role_menu_tenant_id ON sys_role_menu(tenant_id);
ALTER TABLE sys_user_password_history ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_user_password_history_tenant_id ON sys_user_password_history(tenant_id);
ALTER TABLE sys_user_role ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_user_role_tenant_id ON sys_user_role(tenant_id);
ALTER TABLE sys_user_social ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_sys_user_social_tenant_id ON sys_user_social(tenant_id);

-- changeset 小熊:3
-- comment 唯一索引变更
ALTER TABLE sys_app DROP INDEX `uk_access_key`;
ALTER TABLE sys_app ADD UNIQUE INDEX `uk_access_key` (`access_key`, `tenant_id`);
ALTER TABLE sys_dept DROP INDEX `uk_name_parent_id`;
ALTER TABLE sys_dept ADD UNIQUE INDEX `uk_name_parent_id` (`name`, `parent_id`, `tenant_id`);
ALTER TABLE sys_menu DROP INDEX `uk_title_parent_id`;
ALTER TABLE sys_menu ADD UNIQUE INDEX `uk_title_parent_id` (`title`, `parent_id`, `tenant_id`);
ALTER TABLE sys_role DROP INDEX `uk_name`;
ALTER TABLE sys_role ADD UNIQUE INDEX `uk_name` (`name`, `tenant_id`);
ALTER TABLE sys_role DROP INDEX `uk_code`;
ALTER TABLE sys_role ADD UNIQUE INDEX `uk_code` (`code`, `tenant_id`);
ALTER TABLE sys_user DROP INDEX `uk_username`;
ALTER TABLE sys_user ADD UNIQUE INDEX `uk_username` (`username`, `tenant_id`);
ALTER TABLE sys_user DROP INDEX `uk_email`;
ALTER TABLE sys_user ADD UNIQUE INDEX `uk_email` (`email`, `tenant_id`);
ALTER TABLE sys_user DROP INDEX `uk_phone`;
ALTER TABLE sys_user ADD UNIQUE INDEX `uk_phone` (`phone`, `tenant_id`);

-- changeset 小熊:4
-- comment 菜单录入
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`, `tenant_id`)
VALUES
(10010, '租户管理', 0, 1, '/tenant', 'Tenant', 'Layout', '/tenant/user', 'user-group', b'0', b'0', b'0', NULL, 6, 1, 1, NOW(), NULL, NULL, 0),
(10015, '租户套餐', 10010, 2, '/tenant/package', 'TenantPackage', 'tenant/package/index', NULL, 'menu', b'0', b'0', b'0', NULL, 2, 1, 1, NOW(), NULL, NULL, 0),
(10016, '列表', 10015, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:list', 1, 1, 1, NOW(), NULL, NULL, 0),
(10017, '详情', 10015, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:detail', 2, 1, 1, NOW(), NULL, NULL, 0),
(10018, '新增', 10015, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:add', 3, 1, 1, NOW(), NULL, NULL, 0),
(10019, '修改', 10015, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:update', 4, 1, 1, NOW(), NULL, NULL, 0),
(10020, '删除', 10015, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:delete', 5, 1, 1, NOW(), NULL, NULL, 0),
(10022, '租户管理', 10010, 2, '/tenant/user', 'TenantUser', 'tenant/user/index', NULL, 'user-group', b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL, 0),
(10023, '列表', 10022, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:user:list', 1, 1, 1, NOW(), NULL, NULL, 0),
(10024, '详情', 10022, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:user:detail', 2, 1, 1, NOW(), NULL, NULL, 0),
(10025, '新增', 10022, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:user:add', 3, 1, 1, NOW(), NULL, NULL, 0),
(10026, '修改', 10022, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:user:update', 4, 1, 1, NOW(), NULL, NULL, 0),
(10027, '删除', 10022, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:user:delete', 5, 1, 1, NOW(), NULL, NULL, 0),
(10028, '账号更新', 10022, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:user:editLoginUserInfo', 6, 1, 1, NOW(), NULL, NULL, 0),
(10040, '数据连接', 10010, 2, '/tenant/dbConnect', 'TenantDbConnect', 'tenant/tenantDbConnect/index', NULL, 'storage', b'0', b'0', b'0', NULL, 3, 1, 1, NOW(), NULL, NULL, 0),
(10041, '列表', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:dbConnect:list', 1, 1, 1, NOW(), NULL, NULL, 0),
(10042, '详情', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:dbConnect:detail', 2, 1, 1, NOW(), NULL, NULL, 0),
(10043, '新增', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:dbConnect:add', 3, 1, 1, NOW(), NULL, NULL, 0),
(10044, '修改', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:dbConnect:update', 4, 1, 1, NOW(), NULL, NULL, 0),
(10045, '删除', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:dbConnect:delete', 5, 1, 1, NOW(), NULL, NULL, 0);



