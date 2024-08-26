-- liquibase formatted sql

-- changeset Charles7c:3.3-1
ALTER TABLE "gen_field_config" ADD COLUMN `dict_code` varchar(30) DEFAULT NULL COMMENT '字典编码' AFTER `query_type`;
COMMENT ON COLUMN "gen_field_config"."dict_code" IS '字典编码';
