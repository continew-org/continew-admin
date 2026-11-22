-- liquibase formatted sql

-- changeset charles7c:1
-- comment 创建示例表
CREATE TABLE IF NOT EXISTS `biz_demo` (
    `id`          bigint      AUTO_INCREMENT      COMMENT 'ID',
    `name`        varchar(64) NOT NULL            COMMENT '名称',
    `quantity`    int         NOT NULL DEFAULT 0  COMMENT '数量',
    `online_time` datetime    DEFAULT NULL        COMMENT '上线时间',
    `ext_config`  json        DEFAULT NULL        COMMENT '扩展配置',
    `create_user` bigint      NOT NULL            COMMENT '创建人',
    `create_time` datetime    NOT NULL            COMMENT '创建时间',
    `update_user` bigint      DEFAULT NULL        COMMENT '修改人',
    `update_time` datetime    DEFAULT NULL        COMMENT '修改时间',
    `tenant_id`   bigint      NOT NULL DEFAULT 0  COMMENT '租户ID',
    `deleted`     bigint      NOT NULL DEFAULT 0  COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_create_user`(`create_user`),
    INDEX `idx_update_user`(`update_user`),
    INDEX `idx_tenant_id`(`tenant_id`),
    INDEX `idx_deleted`(`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例表';
