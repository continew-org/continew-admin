-- liquibase formatted sql

-- changeset luoqiz:4.2.0-1
-- comment sys_client 客户端表更新
-- sys_client 添加双token列
ALTER TABLE `sys_client`
    ADD COLUMN `is_enable_refresh_token` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用refresh token (true: 启用;  false: 禁用)' AFTER `timeout`,
ADD COLUMN `refresh_token_timeout` bigint NULL DEFAULT 2592000 COMMENT 'Refresh token有效期（单位：秒; 值必须大于0，否则取token的有效时长）' AFTER `is_enable_refresh_token`;

-- 初始化客户端数据
INSERT INTO `sys_client`
(`id`, `client_id`, `client_type`, `auth_type`, `active_timeout`, `timeout`, `status`, `create_user`, `create_time`,
 `is_enable_refresh_token`, `refresh_token_timeout`)
VALUES (2, 'ef51c9a3e9046c4f2ea45142c8a8344b', 'XCX', '["ACCOUNT", "EMAIL", "PHONE", "SOCIAL"]', 1800, 86400, 1, 1,
        NOW(), b'1', 2592000);
