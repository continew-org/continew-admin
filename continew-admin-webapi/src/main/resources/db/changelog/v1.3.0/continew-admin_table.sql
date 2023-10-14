-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_user_social` (
    `source`          varchar(255) NOT NULL     COMMENT '来源',
    `open_id`         varchar(255) NOT NULL     COMMENT '开放ID',
    `user_id`         bigint(20)   NOT NULL     COMMENT '用户ID',
    `meta_json`       text         DEFAULT NULL COMMENT '附加信息',
    `last_login_time` datetime     DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     datetime     NOT NULL     COMMENT '创建时间',
    UNIQUE INDEX `uk_source_open_id`(`source`, `open_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户社会化关联表';