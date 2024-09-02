-- liquibase formatted sql

-- changeset Charles7c:3.3-1
ALTER TABLE "gen_field_config" ADD COLUMN "dict_code" varchar(30) DEFAULT NULL;
COMMENT ON COLUMN "gen_field_config"."dict_code" IS '字典编码';

-- changeset Charles7c:3.3-2
ALTER TABLE "sys_role"
    ADD COLUMN "menu_check_strictly" bool DEFAULT false,
    ADD COLUMN "dept_check_strictly" bool DEFAULT false;
COMMENT ON COLUMN "sys_role"."menu_check_strictly" IS '菜单选择是否父子节点关联';
COMMENT ON COLUMN "sys_role"."dept_check_strictly" IS '部门选择是否父子节点关联';
