-- liquibase formatted sql

-- changeset charles7c:1
-- comment 创建示例表
CREATE TABLE IF NOT EXISTS "biz_demo" (
    "id"          int8        NOT NULL,
    "name"        varchar(64) NOT NULL,
    "quantity"    int4        NOT NULL DEFAULT 0,
    "online_time" timestamp   DEFAULT NULL,
    "ext_config"  json        DEFAULT NULL,
    "create_user" int8        NOT NULL,
    "create_time" timestamp   NOT NULL,
    "update_user" int8        DEFAULT NULL,
    "update_time" timestamp   DEFAULT NULL,
    "deleted"     int8        NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_demo_create_user" ON "biz_demo" ("create_user");
CREATE INDEX "idx_demo_update_user" ON "biz_demo" ("update_user");
CREATE INDEX "idx_demo_deleted"     ON "biz_demo" ("deleted");
COMMENT ON COLUMN "biz_demo"."id"          IS 'ID';
COMMENT ON COLUMN "biz_demo"."name"        IS '名称';
COMMENT ON COLUMN "biz_demo"."quantity"    IS '数量';
COMMENT ON COLUMN "biz_demo"."online_time" IS '上线时间';
COMMENT ON COLUMN "biz_demo"."ext_config"  IS '扩展配置';
COMMENT ON COLUMN "biz_demo"."create_user" IS '创建人';
COMMENT ON COLUMN "biz_demo"."create_time" IS '创建时间';
COMMENT ON COLUMN "biz_demo"."update_user" IS '修改人';
COMMENT ON COLUMN "biz_demo"."update_time" IS '修改时间';
COMMENT ON COLUMN "biz_demo"."deleted"     IS '是否已删除（0：否；id：是）';
COMMENT ON TABLE  "biz_demo"               IS '示例表';
