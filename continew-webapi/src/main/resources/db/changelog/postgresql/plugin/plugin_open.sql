-- liquibase formatted sql

-- changeset chengzi:1
-- comment 初始化能力开放插件
-- 初始化表结构
CREATE TABLE IF NOT EXISTS "sys_app" (
    "id"          int8         NOT NULL,
    "name"        varchar(100) NOT NULL,
    "access_key"  varchar(255) NOT NULL,
    "secret_key"  varchar(255) NOT NULL,
    "expire_time" timestamp    DEFAULT NULL,
    "description" varchar(200) DEFAULT NULL,
    "status"      int2         NOT NULL DEFAULT 1,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_app_access_key" ON "sys_app" ("access_key");
CREATE INDEX "idx_app_create_user" ON "sys_app" ("create_user");
CREATE INDEX "idx_app_update_user" ON "sys_app" ("update_user");
COMMENT ON COLUMN "sys_app"."id"              IS 'ID';
COMMENT ON COLUMN "sys_app"."name"            IS '名称';
COMMENT ON COLUMN "sys_app"."access_key"      IS 'Access Key（访问密钥）';
COMMENT ON COLUMN "sys_app"."secret_key"      IS 'Secret Key（私有密钥）';
COMMENT ON COLUMN "sys_app"."expire_time"     IS '失效时间';
COMMENT ON COLUMN "sys_app"."description"     IS '描述';
COMMENT ON COLUMN "sys_app"."status"          IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_app"."create_user"     IS '创建人';
COMMENT ON COLUMN "sys_app"."create_time"     IS '创建时间';
COMMENT ON COLUMN "sys_app"."update_user"     IS '修改人';
COMMENT ON COLUMN "sys_app"."update_time"     IS '修改时间';
COMMENT ON TABLE  "sys_app"                   IS '应用表';

-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(7000, '能力开放', 0, 1, '/open', 'Open', 'Layout', '/open/app', 'expand', false, false, false, NULL, 7, 1, 1, NOW()),
(7010, '应用管理', 7000, 2, '/open/app', 'OpenApp', 'open/app/index', NULL, 'common', false, false, false, NULL, 1, 1, 1, NOW()),
(7011, '列表', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:list', 1, 1, 1, NOW()),
(7012, '详情', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:get', 2, 1, 1, NOW()),
(7013, '新增', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:create', 3, 1, 1, NOW()),
(7014, '修改', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:update', 4, 1, 1, NOW()),
(7015, '删除', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:delete', 5, 1, 1, NOW()),
(7016, '导出', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:export', 6, 1, 1, NOW()),
(7017, '查看密钥', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:secret', 7, 1, 1, NOW()),
(7018, '重置密钥', 7010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'open:app:resetSecret', 8, 1, 1, NOW());
