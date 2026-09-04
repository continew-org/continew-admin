-- liquibase formatted sql

-- changeset codex:refresh-token-client-fields-postgresql
-- refresh_token_timeout 的默认值与 RefreshTokenProperties.defaultTimeout（30 天）保持一致。
ALTER TABLE "sys_client"
    ADD COLUMN "is_enable_refresh_token" boolean NOT NULL DEFAULT true;
ALTER TABLE "sys_client"
    ADD COLUMN "refresh_token_timeout" int8 DEFAULT 2592000;
ALTER TABLE "sys_client"
    ADD COLUMN "refresh_token_mode" varchar(16) NOT NULL DEFAULT 'COOKIE';

COMMENT ON COLUMN "sys_client"."is_enable_refresh_token"
    IS '是否启用 Refresh Token（false：否；true：是，默认开启）';
COMMENT ON COLUMN "sys_client"."refresh_token_timeout"
    IS 'Refresh Token 绝对有效期（单位：秒）';
COMMENT ON COLUMN "sys_client"."refresh_token_mode"
    IS 'Refresh Token 传输模式（COOKIE：浏览器；BODY：App/小程序）';
