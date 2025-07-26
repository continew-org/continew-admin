-- liquibase formatted sql

-- changeset 小熊:1
-- comment 初始化租户插件数据表
-- 初始化表结构
CREATE TABLE IF NOT EXISTS "tenant" (
    "id"          int8         NOT NULL,
    "name"        varchar(30)  NOT NULL,
    "code"        varchar(30)  NOT NULL,
    "domain"      varchar(255) DEFAULT NULL,
    "expire_time" timestamp    DEFAULT NULL,
    "description" varchar(200) DEFAULT NULL,
    "status"      int2         NOT NULL DEFAULT 1,
    "admin_user"  int8         DEFAULT NULL,
    "package_id"  int8         NOT NULL,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_tenant_code" ON "tenant" ("code");
CREATE INDEX "idx_tenant_admin_user" ON "tenant" ("admin_user");
CREATE INDEX "idx_tenant_package_id" ON "tenant" ("package_id");
CREATE INDEX "idx_tenant_create_user" ON "tenant" ("create_user");
CREATE INDEX "idx_tenant_update_user" ON "tenant" ("update_user");
COMMENT ON COLUMN "tenant"."id" IS 'ID';
COMMENT ON COLUMN "tenant"."name" IS '名称';
COMMENT ON COLUMN "tenant"."code" IS '编码';
COMMENT ON COLUMN "tenant"."domain" IS '绑定域名';
COMMENT ON COLUMN "tenant"."expire_time" IS '过期时间';
COMMENT ON COLUMN "tenant"."description" IS '描述';
COMMENT ON COLUMN "tenant"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "tenant"."package_id" IS '套餐ID';
COMMENT ON COLUMN "tenant"."admin_user" IS '租户管理员';
COMMENT ON COLUMN "tenant"."create_user" IS '创建人';
COMMENT ON COLUMN "tenant"."create_time" IS '创建时间';
COMMENT ON COLUMN "tenant"."update_user" IS '修改人';
COMMENT ON COLUMN "tenant"."update_time" IS '修改时间';
COMMENT ON TABLE "tenant" IS '租户表';

CREATE TABLE IF NOT EXISTS "tenant_package" (
    "id"                  int8         NOT NULL, 
    "name"                varchar(30)  NOT NULL, 
    "sort"                int4         NOT NULL DEFAULT 999, 
    "menu_check_strictly" bool         DEFAULT true, 
    "description"         varchar(200) DEFAULT NULL, 
    "status"              int2         NOT NULL DEFAULT 1, 
    "create_user"         int8         NOT NULL, 
    "create_time"         timestamp    NOT NULL, 
    "update_user"         int8         DEFAULT NULL, 
    "update_time"         timestamp    DEFAULT NULL, 
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_tenant_package_create_user" ON "tenant_package" ("create_user");
CREATE INDEX "idx_tenant_package_update_user" ON "tenant_package" ("update_user");
COMMENT ON COLUMN "tenant_package"."id" IS 'ID';
COMMENT ON COLUMN "tenant_package"."name" IS '名称';
COMMENT ON COLUMN "tenant_package"."sort" IS '排序';
COMMENT ON COLUMN "tenant_package"."menu_check_strictly" IS '菜单选择是否父子节点关联';
COMMENT ON COLUMN "tenant_package"."description" IS '描述';
COMMENT ON COLUMN "tenant_package"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "tenant_package"."create_user" IS '创建人';
COMMENT ON COLUMN "tenant_package"."create_time" IS '创建时间';
COMMENT ON COLUMN "tenant_package"."update_user" IS '修改人';
COMMENT ON COLUMN "tenant_package"."update_time" IS '修改时间';
COMMENT ON TABLE "tenant_package" IS '租户套餐表';

CREATE TABLE IF NOT EXISTS "tenant_package_menu" (
    "package_id" int8 NOT NULL, 
    "menu_id"    int8 NOT NULL, 
    PRIMARY KEY ("package_id", "menu_id")
);
COMMENT ON COLUMN "tenant_package_menu"."package_id" IS '套餐ID';
COMMENT ON COLUMN "tenant_package_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "tenant_package_menu" IS '租户套餐和菜单关联表';

-- 为已有表增加租户字段
ALTER TABLE "sys_dept" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_dept"."tenant_id" IS '租户ID';
CREATE INDEX "idx_dept_tenant_id" ON "sys_dept" ("tenant_id");

ALTER TABLE "sys_role" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_tenant_id" ON "sys_role" ("tenant_id");

ALTER TABLE "sys_user" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user"."tenant_id" IS '租户ID';
CREATE INDEX "idx_user_tenant_id" ON "sys_user" ("tenant_id");

ALTER TABLE "sys_user_password_history" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user_password_history"."tenant_id" IS '租户ID';
CREATE INDEX "idx_uph_tenant_id" ON "sys_user_password_history" ("tenant_id");

ALTER TABLE "sys_user_social" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user_social"."tenant_id" IS '租户ID';
CREATE INDEX "idx_user_source_tenant_id" ON "sys_user_social" ("tenant_id");

ALTER TABLE "sys_user_role" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user_role"."tenant_id" IS '租户ID';
CREATE INDEX "idx_user_role_tenant_id" ON "sys_user_role" ("tenant_id");

ALTER TABLE "sys_role_menu" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role_menu"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_menu_tenant_id" ON "sys_role_menu" ("tenant_id");

ALTER TABLE "sys_role_dept" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role_dept"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_dept_tenant_id" ON "sys_role_dept" ("tenant_id");

ALTER TABLE "sys_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_log_tenant_id" ON "sys_log" ("tenant_id");

ALTER TABLE "sys_message" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_message"."tenant_id" IS '租户ID';
CREATE INDEX "idx_message_tenant_id" ON "sys_message" ("tenant_id");

ALTER TABLE "sys_message_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_message_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_message_log_tenant_id" ON "sys_message_log" ("tenant_id");

ALTER TABLE "sys_notice" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_notice"."tenant_id" IS '租户ID';
CREATE INDEX "idx_notice_tenant_id" ON "sys_notice" ("tenant_id");

ALTER TABLE "sys_notice_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_notice_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_notice_log_tenant_id" ON "sys_notice_log" ("tenant_id");

ALTER TABLE "sys_file" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_file"."tenant_id" IS '租户ID';
CREATE INDEX "idx_file_tenant_id" ON "sys_file" ("tenant_id");

ALTER TABLE "sys_app" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_app"."tenant_id" IS '租户ID';
CREATE INDEX "idx_app_tenant_id" ON "sys_app" ("tenant_id");

-- 调整唯一索引
DROP INDEX IF EXISTS "uk_dept_name_parent_id";
CREATE UNIQUE INDEX "uk_dept_name_parent_id" ON "sys_dept" ("name", "parent_id", "tenant_id");

DROP INDEX IF EXISTS "uk_role_name", "uk_role_code";
CREATE UNIQUE INDEX "uk_role_name" ON "sys_role" ("name", "tenant_id");
CREATE UNIQUE INDEX "uk_role_code" ON "sys_role" ("code", "tenant_id");

DROP INDEX IF EXISTS "uk_user_username", "uk_user_email", "uk_user_phone";
CREATE UNIQUE INDEX "uk_user_username" ON "sys_user" ("username", "tenant_id");
CREATE UNIQUE INDEX "uk_user_email" ON "sys_user" ("email", "tenant_id");
CREATE UNIQUE INDEX "uk_user_phone" ON "sys_user" ("phone", "tenant_id");

DROP INDEX IF EXISTS "uk_app_access_key";
CREATE UNIQUE INDEX "uk_app_access_key" ON "sys_app" ("access_key", "tenant_id");

-- 初始化默认菜单
INSERT INTO "sys_menu" ("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(3000, '租户管理', 0, 1, '/tenant', 'Tenant', 'Layout', '/tenant/management', 'user-group', false, false, false, NULL, 6, 1, 1, NOW()),

(3010, '租户管理', 3000, 2, '/tenant/management', 'TenantManagement', 'tenant/management/index', NULL, 'user-group', false, false, false, NULL, 1, 1, 1, NOW()),
(3011, '列表', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:list', 1, 1, 1, NOW()),
(3012, '详情', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:get', 2, 1, 1, NOW()),
(3013, '新增', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:create', 3, 1, 1, NOW()),
(3014, '修改', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:update', 4, 1, 1, NOW()),
(3015, '删除', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:management:delete', 5, 1, 1, NOW()),
(3016, '修改租户管理员密码', 3010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'tenant:management:updateAdminUserPwd', 6, 1, 1, NOW()),

(3020, '套餐管理', 3000, 2, '/tenant/package', 'TenantPackage', 'tenant/package/index', NULL, 'project', false, false, false, NULL, 2, 1, 1, NOW()),
(3021, '列表', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:list', 1, 1, 1, NOW()),
(3022, '详情', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:get', 2, 1, 1, NOW()),
(3023, '新增', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:create', 3, 1, 1, NOW()),
(3024, '修改', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:update', 4, 1, 1, NOW()),
(3025, '删除', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'tenant:package:delete', 5, 1, 1, NOW());