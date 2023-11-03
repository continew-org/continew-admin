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

-- changeset BUSS_BCLS:2
CREATE TABLE IF NOT EXISTS `sys_message` (
    `id`          bigint(20)   AUTO_INCREMENT              COMMENT 'ID',
    `title`       varchar(50)  NOT NULL                    COMMENT '标题',
    `content`     varchar(255) DEFAULT NULL                COMMENT '内容',
    `type`        tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型（1：系统消息）',
    `create_user` bigint(20)   DEFAULT NULL                COMMENT '创建人',
    `create_time` datetime     NOT NULL                    COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE IF NOT EXISTS `sys_message_user` (
    `message_id` bigint(20) NOT NULL              COMMENT '消息ID',
    `user_id`    bigint(11) NOT NULL              COMMENT '用户ID',
    `is_read`    bit(1)     NOT NULL DEFAULT b'0' COMMENT '是否已读',
    `read_time`  datetime   DEFAULT NULL          COMMENT '读取时间',
    PRIMARY KEY (`message_id`,`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息和用户关联表';