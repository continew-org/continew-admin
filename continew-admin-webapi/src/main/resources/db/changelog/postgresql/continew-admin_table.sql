-- liquibase formatted sql

-- changeset Charles7c:1
-- comment 初始化表结构
CREATE TABLE IF NOT EXISTS "sys_menu" (
    "id"          int8         NOT NULL,
    "title"       varchar(30)  NOT NULL,
    "parent_id"   int8         NOT NULL DEFAULT 0,
    "type"        int2         NOT NULL DEFAULT 1,
    "path"        varchar(255) DEFAULT NULL,
    "name"        varchar(50)  DEFAULT NULL,
    "component"   varchar(255) DEFAULT NULL,
    "redirect"    varchar(255) DEFAULT NULL,
    "icon"        varchar(50)  DEFAULT NULL,
    "is_external" bool         DEFAULT false,
    "is_cache"    bool         DEFAULT false,
    "is_hidden"   bool         DEFAULT false,
    "permission"  varchar(100) DEFAULT NULL,
    "sort"        int4         NOT NULL DEFAULT 999,
    "status"      int2         NOT NULL DEFAULT 1,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_menu_parent_id"   ON "sys_menu" ("parent_id");
CREATE INDEX "idx_menu_create_user" ON "sys_menu" ("create_user");
CREATE INDEX "idx_menu_update_user" ON "sys_menu" ("update_user");
CREATE UNIQUE INDEX "uk_menu_title_parent_id" ON "sys_menu" ("title", "parent_id");
COMMENT ON COLUMN "sys_menu"."id"          IS 'ID';
COMMENT ON COLUMN "sys_menu"."title"       IS '标题';
COMMENT ON COLUMN "sys_menu"."parent_id"   IS '上级菜单ID';
COMMENT ON COLUMN "sys_menu"."type"        IS '类型（1：目录；2：菜单；3：按钮）';
COMMENT ON COLUMN "sys_menu"."path"        IS '路由地址';
COMMENT ON COLUMN "sys_menu"."name"        IS '组件名称';
COMMENT ON COLUMN "sys_menu"."component"   IS '组件路径';
COMMENT ON COLUMN "sys_menu"."redirect"    IS '重定向地址';
COMMENT ON COLUMN "sys_menu"."icon"        IS '图标';
COMMENT ON COLUMN "sys_menu"."is_external" IS '是否外链';
COMMENT ON COLUMN "sys_menu"."is_cache"    IS '是否缓存';
COMMENT ON COLUMN "sys_menu"."is_hidden"   IS '是否隐藏';
COMMENT ON COLUMN "sys_menu"."permission"  IS '权限标识';
COMMENT ON COLUMN "sys_menu"."sort"        IS '排序';
COMMENT ON COLUMN "sys_menu"."status"      IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_menu"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_menu"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_menu"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_menu"               IS '菜单表';

CREATE TABLE IF NOT EXISTS "sys_dept" (
    "id"          int8         NOT NULL,
    "name"        varchar(30)  NOT NULL,
    "parent_id"   int8         NOT NULL DEFAULT 0,
    "ancestors"   varchar(512) NOT NULL DEFAULT '',
    "description" varchar(200) DEFAULT NULL,
    "sort"        int4         NOT NULL DEFAULT 999,
    "status"      int2         NOT NULL DEFAULT 1,
    "is_system"   bool         NOT NULL DEFAULT false,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_dept_parent_id"   ON "sys_dept" ("parent_id");
CREATE INDEX "idx_dept_create_user" ON "sys_dept" ("create_user");
CREATE INDEX "idx_dept_update_user" ON "sys_dept" ("update_user");
CREATE UNIQUE INDEX "uk_dept_name_parent_id" ON "sys_dept" ("name", "parent_id");
COMMENT ON COLUMN "sys_dept"."id"          IS 'ID';
COMMENT ON COLUMN "sys_dept"."name"        IS '名称';
COMMENT ON COLUMN "sys_dept"."parent_id"   IS '上级部门ID';
COMMENT ON COLUMN "sys_dept"."ancestors"   IS '祖级列表';
COMMENT ON COLUMN "sys_dept"."description" IS '描述';
COMMENT ON COLUMN "sys_dept"."sort"        IS '排序';
COMMENT ON COLUMN "sys_dept"."status"      IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_dept"."is_system"   IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_dept"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_dept"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_dept"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_dept"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_dept"               IS '部门表';

CREATE TABLE IF NOT EXISTS "sys_role" (
    "id"          int8         NOT NULL,
    "name"        varchar(30)  NOT NULL,
    "code"        varchar(30)  NOT NULL,
    "data_scope"  int2         NOT NULL DEFAULT 4,
    "description" varchar(200) DEFAULT NULL,
    "sort"        int4         NOT NULL DEFAULT 999,
    "is_system"   bool         NOT NULL DEFAULT false,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_role_name"  ON "sys_role" ("name");
CREATE UNIQUE INDEX "uk_role_code"  ON "sys_role" ("code");
CREATE INDEX "idx_role_create_user" ON "sys_role" ("create_user");
CREATE INDEX "idx_role_update_user" ON "sys_role" ("update_user");
COMMENT ON COLUMN "sys_role"."id"          IS 'ID';
COMMENT ON COLUMN "sys_role"."name"        IS '名称';
COMMENT ON COLUMN "sys_role"."code"        IS '编码';
COMMENT ON COLUMN "sys_role"."data_scope"  IS '数据权限（1：全部数据权限；2：本部门及以下数据权限；3：本部门数据权限；4：仅本人数据权限；5：自定义数据权限）';
COMMENT ON COLUMN "sys_role"."description" IS '描述';
COMMENT ON COLUMN "sys_role"."sort"        IS '排序';
COMMENT ON COLUMN "sys_role"."is_system"   IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_role"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_role"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_role"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_role"               IS '角色表';

CREATE TABLE IF NOT EXISTS "sys_user" (
    "id"             int8         NOT NULL,
    "username"       varchar(64)  NOT NULL,
    "nickname"       varchar(30)  NOT NULL,
    "password"       varchar(255) DEFAULT NULL,
    "gender"         int2         NOT NULL DEFAULT 0,
    "email"          varchar(255) DEFAULT NULL,
    "phone"          varchar(255) DEFAULT NULL,
    "avatar"         text         DEFAULT NULL,
    "description"    varchar(200) DEFAULT NULL,
    "status"         int2         NOT NULL DEFAULT 1,
    "is_system"      bool         NOT NULL DEFAULT false,
    "pwd_reset_time" timestamp    DEFAULT NULL,
    "dept_id"        int8         NOT NULL,
    "create_user"    int8         DEFAULT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_user"    int8         DEFAULT NULL,
    "update_time"    timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_user_username" ON "sys_user" ("username");
CREATE UNIQUE INDEX "uk_user_email"    ON "sys_user" ("email");
CREATE UNIQUE INDEX "uk_user_phone"    ON "sys_user" ("phone");
CREATE INDEX "idx_user_dept_id"        ON "sys_user" ("dept_id");
CREATE INDEX "idx_user_create_user"    ON "sys_user" ("create_user");
CREATE INDEX "idx_user_update_user"    ON "sys_user" ("update_user");
COMMENT ON COLUMN "sys_user"."id"             IS 'ID';
COMMENT ON COLUMN "sys_user"."username"       IS '用户名';
COMMENT ON COLUMN "sys_user"."nickname"       IS '昵称';
COMMENT ON COLUMN "sys_user"."password"       IS '密码';
COMMENT ON COLUMN "sys_user"."gender"         IS '性别（0：未知；1：男；2：女）';
COMMENT ON COLUMN "sys_user"."email"          IS '邮箱';
COMMENT ON COLUMN "sys_user"."phone"          IS '手机号码';
COMMENT ON COLUMN "sys_user"."avatar"         IS '头像';
COMMENT ON COLUMN "sys_user"."description"    IS '描述';
COMMENT ON COLUMN "sys_user"."status"         IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_user"."is_system"      IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_user"."pwd_reset_time" IS '最后一次修改密码时间';
COMMENT ON COLUMN "sys_user"."dept_id"        IS '部门ID';
COMMENT ON COLUMN "sys_user"."create_user"    IS '创建人';
COMMENT ON COLUMN "sys_user"."create_time"    IS '创建时间';
COMMENT ON COLUMN "sys_user"."update_user"    IS '修改人';
COMMENT ON COLUMN "sys_user"."update_time"    IS '修改时间';
COMMENT ON TABLE  "sys_user"                  IS '用户表';

CREATE TABLE IF NOT EXISTS "sys_user_password_history" (
    "id"          int8         NOT NULL,
    "user_id"     int8         NOT NULL,
    "password"    varchar(255) NOT NULL,
    "create_time" timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_uph_user_id" ON "sys_user_password_history" ("user_id");
COMMENT ON COLUMN "sys_user_password_history"."id"          IS 'ID';
COMMENT ON COLUMN "sys_user_password_history"."user_id"     IS '用户ID';
COMMENT ON COLUMN "sys_user_password_history"."password"    IS '密码';
COMMENT ON COLUMN "sys_user_password_history"."create_time" IS '创建时间';
COMMENT ON TABLE  "sys_user_password_history"               IS '用户历史密码表';

CREATE TABLE IF NOT EXISTS "sys_user_social" (
    "source"          varchar(255) NOT NULL,
    "open_id"         varchar(255) NOT NULL,
    "user_id"         int8         NOT NULL,
    "meta_json"       text         DEFAULT NULL,
    "last_login_time" timestamp    DEFAULT NULL,
    "create_time"     timestamp    NOT NULL
);
CREATE UNIQUE INDEX "uk_user_source_open_id" ON "sys_user_social" ("source", "open_id");
COMMENT ON COLUMN "sys_user_social"."source"          IS '来源';
COMMENT ON COLUMN "sys_user_social"."open_id"         IS '开放ID';
COMMENT ON COLUMN "sys_user_social"."user_id"         IS '用户ID';
COMMENT ON COLUMN "sys_user_social"."meta_json"       IS '附加信息';
COMMENT ON COLUMN "sys_user_social"."last_login_time" IS '最后登录时间';
COMMENT ON COLUMN "sys_user_social"."create_time"     IS '创建时间';
COMMENT ON TABLE  "sys_user_social"                   IS '用户社会化关联表';

CREATE TABLE IF NOT EXISTS "sys_user_role" (
    "user_id" int8 NOT NULL,
    "role_id" int8 NOT NULL,
    PRIMARY KEY ("user_id", "role_id")
);
COMMENT ON COLUMN "sys_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_user_role"."role_id" IS '角色ID';
COMMENT ON TABLE  "sys_user_role"           IS '用户和角色关联表';

CREATE TABLE IF NOT EXISTS "sys_role_menu" (
    "role_id" int8 NOT NULL,
    "menu_id" int8 NOT NULL,
    PRIMARY KEY ("role_id", "menu_id")
);
COMMENT ON COLUMN "sys_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE  "sys_role_menu"           IS '角色和菜单关联表';

CREATE TABLE IF NOT EXISTS "sys_role_dept" (
    "role_id" int8 NOT NULL,
    "dept_id" int8 NOT NULL,
    PRIMARY KEY ("role_id", "dept_id")
);
COMMENT ON COLUMN "sys_role_dept"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role_dept"."dept_id" IS '部门ID';
COMMENT ON TABLE  "sys_role_dept"           IS '角色和部门关联表';

CREATE TABLE IF NOT EXISTS "sys_option" (
    "id"            int8         NOT NULL,
    "category"      varchar(50)  NOT NULL,
    "name"          varchar(50)  NOT NULL,
    "code"          varchar(100) NOT NULL,
    "value"         text         DEFAULT NULL,
    "default_value" text         DEFAULT NULL,
    "description"   varchar(200) DEFAULT NULL,
    "update_user"   int8         DEFAULT NULL,
    "update_time"   timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_option_category_code" ON "sys_option" ("category", "code");
COMMENT ON COLUMN "sys_option"."id"            IS 'ID';
COMMENT ON COLUMN "sys_option"."category"      IS '类别';
COMMENT ON COLUMN "sys_option"."name"          IS '名称';
COMMENT ON COLUMN "sys_option"."code"          IS '键';
COMMENT ON COLUMN "sys_option"."value"         IS '值';
COMMENT ON COLUMN "sys_option"."default_value" IS '默认值';
COMMENT ON COLUMN "sys_option"."description"   IS '描述';
COMMENT ON COLUMN "sys_option"."update_user"   IS '修改人';
COMMENT ON COLUMN "sys_option"."update_time"   IS '修改时间';
COMMENT ON TABLE  "sys_option"                 IS '参数表';

CREATE TABLE IF NOT EXISTS "sys_dict" (
    "id"          int8         NOT NULL,
    "name"        varchar(30)  NOT NULL,
    "code"        varchar(30)  NOT NULL,
    "description" varchar(200) DEFAULT NULL,
    "is_system"   bool         NOT NULL DEFAULT false,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_dict_name" ON "sys_dict" ("name");
CREATE UNIQUE INDEX "uk_dict_code" ON "sys_dict" ("code");
COMMENT ON COLUMN "sys_dict"."id"          IS 'ID';
COMMENT ON COLUMN "sys_dict"."name"        IS '名称';
COMMENT ON COLUMN "sys_dict"."code"        IS '编码';
COMMENT ON COLUMN "sys_dict"."description" IS '描述';
COMMENT ON COLUMN "sys_dict"."is_system"   IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_dict"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_dict"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_dict"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_dict"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_dict"               IS '字典表';

CREATE TABLE IF NOT EXISTS "sys_dict_item" (
    "id"          int8         NOT NULL,
    "label"       varchar(30)  NOT NULL,
    "value"       varchar(30)  NOT NULL,
    "color"       varchar(30)  DEFAULT NULL,
    "sort"        int4         NOT NULL DEFAULT 999,
    "description" varchar(200) DEFAULT NULL,
    "status"      int2         NOT NULL DEFAULT 1,
    "dict_id"     int8         NOT NULL,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_dict_item_value_dict_id" ON "sys_dict_item" ("value", "dict_id");
CREATE INDEX "idx_dict_item_dict_id"     ON "sys_dict_item" ("dict_id");
CREATE INDEX "idx_dict_item_create_user" ON "sys_dict_item" ("create_user");
CREATE INDEX "idx_dict_item_update_user" ON "sys_dict_item" ("update_user");
COMMENT ON COLUMN "sys_dict_item"."id"          IS 'ID';
COMMENT ON COLUMN "sys_dict_item"."label"       IS '标签';
COMMENT ON COLUMN "sys_dict_item"."value"       IS '值';
COMMENT ON COLUMN "sys_dict_item"."color"       IS '标签颜色';
COMMENT ON COLUMN "sys_dict_item"."sort"        IS '排序';
COMMENT ON COLUMN "sys_dict_item"."description" IS '描述';
COMMENT ON COLUMN "sys_dict_item"."status"      IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_dict_item"."dict_id"     IS '字典ID';
COMMENT ON COLUMN "sys_dict_item"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_dict_item"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_dict_item"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_dict_item"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_dict_item"               IS '字典项表';

CREATE TABLE IF NOT EXISTS "sys_log" (
    "id"               int8         NOT NULL,
    "trace_id"         varchar(255) DEFAULT NULL,
    "description"      varchar(255) NOT NULL,
    "module"           varchar(50)  NOT NULL,
    "request_url"      varchar(512) NOT NULL,
    "request_method"   varchar(10)  NOT NULL,
    "request_headers"  text         DEFAULT NULL,
    "request_body"     text         DEFAULT NULL,
    "status_code"      int4         NOT NULL,
    "response_headers" text         DEFAULT NULL,
    "response_body"    text         DEFAULT NULL,
    "time_taken"       int8         NOT NULL,
    "ip"               varchar(100) DEFAULT NULL,
    "address"          varchar(255) DEFAULT NULL,
    "browser"          varchar(100) DEFAULT NULL,
    "os"               varchar(100) DEFAULT NULL,
    "status"           int2         NOT NULL DEFAULT 1,
    "error_msg"        text         DEFAULT NULL,
    "create_user"      int8         DEFAULT NULL,
    "create_time"      timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_log_module"      ON "sys_log" ("module");
CREATE INDEX "idx_log_ip"          ON "sys_log" ("ip");
CREATE INDEX "idx_log_create_time" ON "sys_log" ("create_time");
COMMENT ON COLUMN "sys_log"."id"               IS 'ID';
COMMENT ON COLUMN "sys_log"."trace_id"         IS '链路ID';
COMMENT ON COLUMN "sys_log"."description"      IS '日志描述';
COMMENT ON COLUMN "sys_log"."module"           IS '所属模块';
COMMENT ON COLUMN "sys_log"."request_url"      IS '请求URL';
COMMENT ON COLUMN "sys_log"."request_method"   IS '请求方式';
COMMENT ON COLUMN "sys_log"."request_headers"  IS '请求头';
COMMENT ON COLUMN "sys_log"."request_body"     IS '请求体';
COMMENT ON COLUMN "sys_log"."status_code"      IS '状态码';
COMMENT ON COLUMN "sys_log"."response_headers" IS '响应头';
COMMENT ON COLUMN "sys_log"."response_body"    IS '响应体';
COMMENT ON COLUMN "sys_log"."time_taken"       IS '耗时（ms）';
COMMENT ON COLUMN "sys_log"."ip"               IS 'IP';
COMMENT ON COLUMN "sys_log"."address"          IS 'IP归属地';
COMMENT ON COLUMN "sys_log"."browser"          IS '浏览器';
COMMENT ON COLUMN "sys_log"."os"               IS '操作系统';
COMMENT ON COLUMN "sys_log"."status"           IS '状态（1：成功；2：失败）';
COMMENT ON COLUMN "sys_log"."error_msg"        IS '错误信息';
COMMENT ON COLUMN "sys_log"."create_user"      IS '创建人';
COMMENT ON COLUMN "sys_log"."create_time"      IS '创建时间';
COMMENT ON TABLE  "sys_log"                    IS '系统日志表';

CREATE TABLE IF NOT EXISTS "sys_message" (
    "id"          int8         NOT NULL,
    "title"       varchar(50)  NOT NULL,
    "content"     varchar(255) DEFAULT NULL,
    "type"        int2         NOT NULL DEFAULT 1,
    "create_user" int8         DEFAULT NULL,
    "create_time" timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "sys_message"."id"          IS 'ID';
COMMENT ON COLUMN "sys_message"."title"       IS '标题';
COMMENT ON COLUMN "sys_message"."content"     IS '内容';
COMMENT ON COLUMN "sys_message"."type"        IS '类型（1：系统消息）';
COMMENT ON COLUMN "sys_message"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_message"."create_time" IS '创建时间';
COMMENT ON TABLE  "sys_message"               IS '消息表';

CREATE TABLE IF NOT EXISTS "sys_message_user" (
    "message_id" int8      NOT NULL,
    "user_id"    int8      NOT NULL,
    "is_read"    bool      NOT NULL DEFAULT false,
    "read_time"  timestamp DEFAULT NULL,
    PRIMARY KEY ("message_id", "user_id")
);
COMMENT ON COLUMN "sys_message_user"."message_id" IS '消息ID';
COMMENT ON COLUMN "sys_message_user"."user_id"    IS '用户ID';
COMMENT ON COLUMN "sys_message_user"."is_read"    IS '是否已读';
COMMENT ON COLUMN "sys_message_user"."read_time"  IS '读取时间';
COMMENT ON TABLE  "sys_message_user"              IS '消息和用户关联表';

CREATE TABLE IF NOT EXISTS "sys_notice" (
    "id"             int8         NOT NULL,
    "title"          varchar(150) NOT NULL,
    "content"        text         NOT NULL,
    "type"           varchar(30)  NOT NULL,
    "effective_time" timestamp    DEFAULT NULL,
    "terminate_time" timestamp    DEFAULT NULL,
    "sort"           int4         NOT NULL DEFAULT 999,
    "create_user"    int8         NOT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_user"    int8         DEFAULT NULL,
    "update_time"    timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_notice_create_user" ON "sys_notice" ("create_user");
CREATE INDEX "idx_notice_update_user" ON "sys_notice" ("update_user");
COMMENT ON COLUMN "sys_notice"."id"             IS 'ID';
COMMENT ON COLUMN "sys_notice"."title"          IS '标题';
COMMENT ON COLUMN "sys_notice"."content"        IS '内容';
COMMENT ON COLUMN "sys_notice"."type"           IS '类型';
COMMENT ON COLUMN "sys_notice"."effective_time" IS '生效时间';
COMMENT ON COLUMN "sys_notice"."terminate_time" IS '终止时间';
COMMENT ON COLUMN "sys_notice"."sort"           IS '排序';
COMMENT ON COLUMN "sys_notice"."create_user"    IS '创建人';
COMMENT ON COLUMN "sys_notice"."create_time"    IS '创建时间';
COMMENT ON COLUMN "sys_notice"."update_user"    IS '修改人';
COMMENT ON COLUMN "sys_notice"."update_time"    IS '修改时间';
COMMENT ON TABLE  "sys_notice"                  IS '公告表';

CREATE TABLE IF NOT EXISTS "sys_storage" (
    "id"          int8         NOT NULL,
    "name"        varchar(100) NOT NULL,
    "code"        varchar(30)  NOT NULL,
    "type"        int2         NOT NULL DEFAULT 1,
    "access_key"  varchar(255) DEFAULT NULL,
    "secret_key"  varchar(255) DEFAULT NULL,
    "endpoint"    varchar(255) DEFAULT NULL,
    "bucket_name" varchar(255) DEFAULT NULL,
    "domain"      varchar(255) NOT NULL DEFAULT '',
    "description" varchar(200) DEFAULT NULL,
    "is_default"  bool         NOT NULL DEFAULT false,
    "sort"        int4         NOT NULL DEFAULT 999,
    "status"      int2         NOT NULL DEFAULT 1,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_storage_code"  ON "sys_storage" ("code");
CREATE INDEX "idx_storage_create_user" ON "sys_storage" ("create_user");
CREATE INDEX "idx_storage_update_user" ON "sys_storage" ("update_user");
COMMENT ON COLUMN "sys_storage"."id"          IS 'ID';
COMMENT ON COLUMN "sys_storage"."name"        IS '名称';
COMMENT ON COLUMN "sys_storage"."code"        IS '编码';
COMMENT ON COLUMN "sys_storage"."type"        IS '类型（1：兼容S3协议存储；2：本地存储）';
COMMENT ON COLUMN "sys_storage"."access_key"  IS 'Access Key（访问密钥）';
COMMENT ON COLUMN "sys_storage"."secret_key"  IS 'Secret Key（私有密钥）';
COMMENT ON COLUMN "sys_storage"."endpoint"    IS 'Endpoint（终端节点）';
COMMENT ON COLUMN "sys_storage"."bucket_name" IS '桶名称';
COMMENT ON COLUMN "sys_storage"."domain"      IS '域名';
COMMENT ON COLUMN "sys_storage"."description" IS '描述';
COMMENT ON COLUMN "sys_storage"."is_default"  IS '是否为默认存储';
COMMENT ON COLUMN "sys_storage"."sort"        IS '排序';
COMMENT ON COLUMN "sys_storage"."status"      IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_storage"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_storage"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_storage"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_storage"."update_time" IS '修改时间';
COMMENT ON TABLE  "sys_storage"               IS '存储表';

CREATE TABLE IF NOT EXISTS "sys_file" (
    "id"             int8         NOT NULL,
    "name"           varchar(255) NOT NULL,
    "size"           int8         NOT NULL,
    "url"            varchar(512) NOT NULL,
    "extension"      varchar(100) DEFAULT NULL,
    "thumbnail_size" int8         DEFAULT NULL,
    "thumbnail_url"  varchar(512) DEFAULT NULL,
    "type"           int2         NOT NULL DEFAULT 1,
    "storage_id"     int8         NOT NULL,
    "create_user"    int8         NOT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_user"    int8         NOT NULL,
    "update_time"    timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_file_url"  ON "sys_file" ("url");
CREATE INDEX "idx_file_type" ON "sys_file" ("type");
CREATE INDEX "idx_file_create_user" ON "sys_file" ("create_user");
CREATE INDEX "idx_file_update_user" ON "sys_file" ("update_user");
COMMENT ON COLUMN "sys_file"."id"             IS 'ID';
COMMENT ON COLUMN "sys_file"."name"           IS '名称';
COMMENT ON COLUMN "sys_file"."size"           IS '大小（字节）';
COMMENT ON COLUMN "sys_file"."url"            IS 'URL';
COMMENT ON COLUMN "sys_file"."extension"      IS '扩展名';
COMMENT ON COLUMN "sys_file"."thumbnail_size" IS '缩略图大小（字节)';
COMMENT ON COLUMN "sys_file"."thumbnail_url"  IS '缩略图URL';
COMMENT ON COLUMN "sys_file"."type"           IS '类型（1：其他；2：图片；3：文档；4：视频；5：音频）';
COMMENT ON COLUMN "sys_file"."storage_id"     IS '存储ID';
COMMENT ON COLUMN "sys_file"."create_user"    IS '创建人';
COMMENT ON COLUMN "sys_file"."create_time"    IS '创建时间';
COMMENT ON COLUMN "sys_file"."update_user"    IS '修改人';
COMMENT ON COLUMN "sys_file"."update_time"    IS '修改时间';
COMMENT ON TABLE  "sys_file"                  IS '文件表';

CREATE TABLE IF NOT EXISTS "gen_config" (
    "table_name"    varchar(64)  NOT NULL,
    "module_name"   varchar(60)  NOT NULL,
    "package_name"  varchar(60)  NOT NULL,
    "business_name" varchar(50)  NOT NULL,
    "author"        varchar(100) NOT NULL,
    "table_prefix"  varchar(20)  DEFAULT NULL,
    "is_override"   bool         NOT NULL DEFAULT false,
    "create_time"   timestamp    NOT NULL,
    "update_time"   timestamp    DEFAULT NULL,
    PRIMARY KEY ("table_name")
);
COMMENT ON COLUMN "gen_config"."table_name"    IS '表名称';
COMMENT ON COLUMN "gen_config"."module_name"   IS '模块名称';
COMMENT ON COLUMN "gen_config"."package_name"  IS '包名称';
COMMENT ON COLUMN "gen_config"."business_name" IS '业务名称';
COMMENT ON COLUMN "gen_config"."author"        IS '作者';
COMMENT ON COLUMN "gen_config"."table_prefix"  IS '表前缀';
COMMENT ON COLUMN "gen_config"."is_override"   IS '是否覆盖';
COMMENT ON COLUMN "gen_config"."create_time"   IS '创建时间';
COMMENT ON COLUMN "gen_config"."update_time"   IS '修改时间';
COMMENT ON TABLE  "gen_config"                 IS '生成配置表';

CREATE TABLE IF NOT EXISTS "gen_field_config" (
    "table_name"    varchar(64)  NOT NULL,
    "column_name"   varchar(64)  NOT NULL,
    "column_type"   varchar(25)  NOT NULL,
    "column_size"   int8         DEFAULT NULL,
    "field_name"    varchar(64)  NOT NULL,
    "field_type"    varchar(25)  NOT NULL,
    "field_sort"    int4         NOT NULL DEFAULT 999,
    "comment"       varchar(512) DEFAULT NULL,
    "is_required"   bool         NOT NULL DEFAULT true,
    "show_in_list"  bool         NOT NULL DEFAULT true,
    "show_in_form"  bool         NOT NULL DEFAULT true,
    "show_in_query" bool         NOT NULL DEFAULT true,
    "form_type"     int2         DEFAULT NULL,
    "query_type"    int2         DEFAULT NULL,
    "create_time"   timestamp    NOT NULL
);
CREATE INDEX "idx_field_config_table_name" ON "gen_field_config" ("table_name");
COMMENT ON COLUMN "gen_field_config"."table_name"    IS '表名称';
COMMENT ON COLUMN "gen_field_config"."column_name"   IS '列名称';
COMMENT ON COLUMN "gen_field_config"."column_type"   IS '列类型';
COMMENT ON COLUMN "gen_field_config"."column_size"   IS '列大小';
COMMENT ON COLUMN "gen_field_config"."field_name"    IS '字段名称';
COMMENT ON COLUMN "gen_field_config"."field_type"    IS '字段类型';
COMMENT ON COLUMN "gen_field_config"."field_sort"    IS '字段排序';
COMMENT ON COLUMN "gen_field_config"."comment"       IS '注释';
COMMENT ON COLUMN "gen_field_config"."is_required"   IS '是否必填';
COMMENT ON COLUMN "gen_field_config"."show_in_list"  IS '是否在列表中显示';
COMMENT ON COLUMN "gen_field_config"."show_in_form"  IS '是否在表单中显示';
COMMENT ON COLUMN "gen_field_config"."show_in_query" IS '是否在查询中显示';
COMMENT ON COLUMN "gen_field_config"."form_type"     IS '表单类型';
COMMENT ON COLUMN "gen_field_config"."query_type"    IS '查询方式';
COMMENT ON COLUMN "gen_field_config"."create_time"   IS '创建时间';
COMMENT ON TABLE  "gen_field_config"                 IS '字段配置表';