-- liquibase formatted sql

-- changeset codex:refresh-token-client-fields-mysql
-- refresh_token_timeout 的默认值与 RefreshTokenProperties.defaultTimeout（30 天）保持一致。
ALTER TABLE `sys_client`
    ADD COLUMN `is_enable_refresh_token` bit(1) NOT NULL DEFAULT b'1'
        COMMENT '是否启用 Refresh Token（0：否；1：是，默认开启）',
    ADD COLUMN `refresh_token_timeout` bigint DEFAULT 2592000
        COMMENT 'Refresh Token 绝对有效期（单位：秒）',
    ADD COLUMN `refresh_token_mode` varchar(16) NOT NULL DEFAULT 'COOKIE'
        COMMENT 'Refresh Token 传输模式（COOKIE：浏览器；BODY：App/小程序）';
