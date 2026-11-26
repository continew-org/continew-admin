-- liquibase formatted sql

-- changeset charles7c:1 dbms:postgresql
-- comment 移除代码生成器：清理菜单、按钮权限、租户套餐关联与数据表
-- 子菜单（按钮权限）先删，再删页面与目录菜单；sys_role_menu 与 tenant_package_menu 关联一并清理
DELETE FROM "sys_role_menu" WHERE "menu_id" IN (9000, 9010, 9011, 9012, 9013, 9014);
DELETE FROM "tenant_package_menu" WHERE "menu_id" IN (9000, 9010, 9011, 9012, 9013, 9014);
DELETE FROM "sys_menu" WHERE "id" IN (9011, 9012, 9013, 9014, 9010, 9000);
DROP TABLE IF EXISTS "gen_field_config";
DROP TABLE IF EXISTS "gen_config";
