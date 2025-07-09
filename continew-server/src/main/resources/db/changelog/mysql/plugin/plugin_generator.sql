-- liquibase formatted sql

-- changeset charles7c:1
-- comment 初始化代码生成插件
-- 初始化表结构
CREATE TABLE IF NOT EXISTS `gen_config` (
    `table_name`    varchar(64)  NOT NULL              COMMENT '表名称',
    `module_name`   varchar(60)  NOT NULL              COMMENT '模块名称',
    `package_name`  varchar(60)  NOT NULL              COMMENT '包名称',
    `business_name` varchar(50)  NOT NULL              COMMENT '业务名称',
    `author`        varchar(100) NOT NULL              COMMENT '作者',
    `table_prefix`  varchar(20)  DEFAULT NULL          COMMENT '表前缀',
    `is_override`   bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否覆盖',
    `create_time`   datetime     NOT NULL              COMMENT '创建时间',
    `update_time`   datetime     DEFAULT NULL          COMMENT '修改时间',
    PRIMARY KEY (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成配置表';

CREATE TABLE IF NOT EXISTS `gen_field_config` (
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `table_name`    varchar(64)  NOT NULL                COMMENT '表名称',
    `column_name`   varchar(64)  NOT NULL                COMMENT '列名称',
    `column_type`   varchar(25)  NOT NULL                COMMENT '列类型',
    `column_size`   bigint(20)   DEFAULT NULL            COMMENT '列大小',
    `field_name`    varchar(64)  NOT NULL                COMMENT '字段名称',
    `field_type`    varchar(25)  NOT NULL                COMMENT '字段类型',
    `field_sort`    int          NOT NULL DEFAULT 999    COMMENT '字段排序',
    `comment`       varchar(512) DEFAULT NULL            COMMENT '注释',
    `is_required`   bit(1)       NOT NULL DEFAULT b'1'   COMMENT '是否必填',
    `show_in_list`  bit(1)       NOT NULL DEFAULT b'1'   COMMENT '是否在列表中显示',
    `show_in_form`  bit(1)       NOT NULL DEFAULT b'1'   COMMENT '是否在表单中显示',
    `show_in_query` bit(1)       NOT NULL DEFAULT b'1'   COMMENT '是否在查询中显示',
    `form_type`     tinyint(1)   UNSIGNED DEFAULT NULL   COMMENT '表单类型',
    `query_type`    tinyint(1)   UNSIGNED DEFAULT NULL   COMMENT '查询方式',
    `dict_code`     varchar(30)  DEFAULT NULL            COMMENT '字典编码',
    `create_time`   datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_table_name`(`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段配置表';

-- 初始化默认菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(9000, '代码生成', 0, 1, '/code', 'Code', 'Layout', '/code/generator', 'code-release-managment', b'0', b'0', b'0', NULL, 9, 1, 1, NOW()),
(9010, '代码生成', 9000, 2, '/code/generator', 'CodeGenerator', 'code/generator/index', NULL, 'code', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(9011, '列表', 9010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'code:generator:list', 1, 1, 1, NOW()),
(9012, '配置', 9010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'code:generator:config', 2, 1, 1, NOW()),
(9013, '预览', 9010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'code:generator:preview', 3, 1, 1, NOW()),
(9014, '生成', 9010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'code:generator:generate', 4, 1, 1, NOW());