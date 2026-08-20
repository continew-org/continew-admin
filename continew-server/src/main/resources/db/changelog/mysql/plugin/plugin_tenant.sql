-- liquibase formatted sql

-- changeset 小熊:1
-- comment 初始化租户插件数据表
-- 初始化表结构
CREATE TABLE IF NOT EXISTS `tenant` (
    `id`             bigint       AUTO_INCREMENT              COMMENT 'ID',
    `name`           varchar(30)  NOT NULL                    COMMENT '名称',
    `code`           varchar(30)  NOT NULL                    COMMENT '编码',
    `domain`         varchar(255) DEFAULT NULL                COMMENT '域名',
    `expire_time`    datetime     DEFAULT NULL                COMMENT '过期时间',
    `description`    varchar(200) DEFAULT NULL                COMMENT '描述',
    `status`         tinyint      UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
    `admin_user`     bigint       DEFAULT NULL                COMMENT '管理员用户',
    `admin_username` varchar(64)  DEFAULT NULL                COMMENT '管理员用户名',
    `package_id`     bigint       NOT NULL                    COMMENT '套餐ID',
    `create_user`    bigint       NOT NULL                    COMMENT '创建人',
    `create_time`    datetime     NOT NULL                    COMMENT '创建时间',
    `update_user`    bigint       DEFAULT NULL                COMMENT '修改人',
    `update_time`    datetime     DEFAULT NULL                COMMENT '修改时间',
    `deleted`        bigint       NOT NULL DEFAULT 0          COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_code`(`code`, `deleted`),
    INDEX `idx_admin_user`(`admin_user`),
    INDEX `idx_package_id`(`package_id`),
    INDEX `idx_create_user`(`create_user`),
    INDEX `idx_update_user`(`update_user`),
    INDEX `idx_deleted`(`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

CREATE TABLE IF NOT EXISTS `tenant_package` (
    `id`                  bigint       AUTO_INCREMENT             COMMENT 'ID',
    `name`                varchar(30)  NOT NULL                   COMMENT '名称',
    `sort`                int          NOT NULL DEFAULT 999       COMMENT '排序',
    `menu_check_strictly` bit(1)       DEFAULT b'1'               COMMENT '菜单选择是否父子节点关联',
    `description`         varchar(200) DEFAULT NULL                COMMENT '描述',
    `status`              tinyint      UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
    `create_user`         bigint       NOT NULL                    COMMENT '创建人',
    `create_time`         datetime     NOT NULL                    COMMENT '创建时间',
    `update_user`         bigint       DEFAULT NULL                COMMENT '修改人',
    `update_time`         datetime     DEFAULT NULL                COMMENT '修改时间',
    `deleted`             bigint       NOT NULL DEFAULT 0          COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_create_user`(`create_user`),
    INDEX `idx_update_user`(`update_user`),
    INDEX `idx_deleted`(`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户套餐表';

CREATE TABLE IF NOT EXISTS `tenant_package_menu` (
    `package_id` bigint NOT NULL COMMENT '套餐ID',
    `menu_id`    bigint NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`package_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户套餐和菜单关联表';

-- 为已有表增加租户字段
ALTER TABLE `sys_dept`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_role`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_user`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_user_password_history`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_user_social`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_user_role`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_role_menu`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_role_dept`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_log`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_message`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_message_log`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_notice`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_notice_log`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_file`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `sys_app`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);

-- 调整唯一索引
ALTER TABLE `sys_dept`
    DROP INDEX `uk_name_parent_id`,
    ADD UNIQUE INDEX `uk_name_parent_id` (`name`, `parent_id`, `deleted`, `tenant_id`);
ALTER TABLE `sys_role`
    DROP INDEX `uk_name`,
    DROP INDEX `uk_code`,
    ADD UNIQUE INDEX `uk_name` (`name`, `deleted`, `tenant_id`),
    ADD UNIQUE INDEX `uk_code` (`code`, `deleted`, `tenant_id`);
ALTER TABLE `sys_user`
    DROP INDEX `uk_username`,
    DROP INDEX `uk_email`,
    DROP INDEX `uk_phone`,
    ADD UNIQUE INDEX `uk_username` (`username`, `deleted`, `tenant_id`),
    ADD UNIQUE INDEX `uk_email` (`email`, `deleted`, `tenant_id`),
    ADD UNIQUE INDEX `uk_phone` (`phone`, `deleted`, `tenant_id`);
ALTER TABLE `sys_user_social`
    DROP INDEX `uk_source_open_id`,
    ADD UNIQUE INDEX `uk_source_open_id` (`source`, `open_id`, `deleted`, `tenant_id`);
ALTER TABLE `sys_app`
    DROP INDEX `uk_access_key`,
    ADD UNIQUE INDEX `uk_access_key` (`access_key`, `deleted`, `tenant_id`);

-- 初始化默认菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(3000, '租户管理', 0, 1, '/tenant', 'Tenant', 'Layout', '/tenant/management', 'user-group', b'0', b'0', b'0', NULL, 6, 1, 1, NOW()),

(3010, '租户管理', 3000, 2, '/tenant/management', 'TenantManagement', 'tenant/management/index', NULL, 'user-group', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(3011, '列表', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:list', 1, 1, 1, NOW()),
(3012, '详情', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:get', 2, 1, 1, NOW()),
(3013, '新增', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:create', 3, 1, 1, NOW()),
(3014, '修改', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:update', 4, 1, 1, NOW()),
(3015, '删除', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:delete', 5, 1, 1, NOW()),
(3016, '修改租户管理员密码', 3010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'tenant:management:updateAdminUserPwd', 6, 1, 1, NOW()),

(3020, '套餐管理', 3000, 2, '/tenant/package', 'TenantPackage', 'tenant/package/index', NULL, 'project', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(3021, '列表', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:list', 1, 1, 1, NOW()),
(3022, '详情', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:get', 2, 1, 1, NOW()),
(3023, '新增', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:create', 3, 1, 1, NOW()),
(3024, '修改', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:update', 4, 1, 1, NOW()),
(3025, '删除', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:delete', 5, 1, 1, NOW());
