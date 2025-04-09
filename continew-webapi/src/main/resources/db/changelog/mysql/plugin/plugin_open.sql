-- liquibase formatted sql

-- changeset chengzi:1
-- comment 初始化能力开放插件
-- 初始化表结构
CREATE TABLE IF NOT EXISTS `sys_app`  (
    `id`          bigint(20)   NOT NULL     AUTO_INCREMENT COMMENT 'ID',
    `name`        varchar(100) NOT NULL                    COMMENT '名称',
    `access_key`  varchar(255) NOT NULL                    COMMENT 'Access Key（访问密钥）',
    `secret_key`  varchar(255) NOT NULL                    COMMENT 'Secret Key（私有密钥）',
    `expire_time` datetime     DEFAULT NULL                COMMENT '失效时间',
    `description` varchar(200) DEFAULT NULL                COMMENT '描述',
    `status`      tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
    `create_user` bigint(20)   NOT NULL                    COMMENT '创建人',
    `create_time` datetime     NOT NULL                    COMMENT '创建时间',
    `update_user` bigint(20)   DEFAULT NULL                COMMENT '修改人',
    `update_time` datetime     DEFAULT NULL                COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_access_key`(`access_key`),
    INDEX `idx_create_user`(`create_user`),
    INDEX `idx_update_user`(`update_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- 初始化默认菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(7000, '能力开放', 0, 1, '/open', 'Open', 'Layout', '/open/app', 'expand', b'0', b'0', b'0', NULL, 7, 1, 1, NOW()),
(7010, '应用管理', 7000, 2, '/open/app', 'OpenApp', 'open/app/index', NULL, 'common', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(7011, '列表', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:list', 1, 1, 1, NOW()),
(7012, '详情', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:get', 2, 1, 1, NOW()),
(7013, '新增', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:create', 3, 1, 1, NOW()),
(7014, '修改', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:update', 4, 1, 1, NOW()),
(7015, '删除', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:delete', 5, 1, 1, NOW()),
(7016, '导出', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:export', 6, 1, 1, NOW()),
(7017, '查看密钥', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:secret', 7, 1, 1, NOW()),
(7018, '重置密钥', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:resetSecret', 8, 1, 1, NOW());
