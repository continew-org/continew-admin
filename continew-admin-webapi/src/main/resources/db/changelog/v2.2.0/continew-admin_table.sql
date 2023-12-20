-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`            bigint(20)   NOT NULL                    COMMENT 'ID',
    `name`          varchar(255) NOT NULL                    COMMENT '名称',
    `size`          bigint(20)   NOT NULL                    COMMENT '大小（字节）',
    `url`           varchar(512) NOT NULL                    COMMENT 'URL',
    `extension`     varchar(100) DEFAULT NULL                COMMENT '扩展名',
    `mime_type`     varchar(100) DEFAULT NULL                COMMENT 'MIME类型',
    `category`      tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型（1：其他；2：图片；3：文档；4：视频；5：音频）',
    `storage_id`    bigint(20)   NOT NULL                    COMMENT '存储库ID',
    `create_user`   bigint(20)   NOT NULL                    COMMENT '创建人',
    `create_time`   datetime     NOT NULL                    COMMENT '创建时间',
    `update_user`   bigint(20)   DEFAULT NULL                COMMENT '修改人',
    `update_time`   datetime     DEFAULT NULL                COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_category`(`category`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

CREATE TABLE IF NOT EXISTS `sys_storage` (
    `id`          bigint(20)   AUTO_INCREMENT              COMMENT 'ID',
    `name`        varchar(100) NOT NULL                    COMMENT '名称',
    `code`        varchar(30)  NOT NULL                    COMMENT '编码',
    `access_key`  varchar(255) DEFAULT NULL                COMMENT 'Access Key（访问密钥）',
    `secret_key`  varchar(255) DEFAULT NULL                COMMENT 'Secret Key（私有访问密钥）',
    `endpoint`    varchar(255) DEFAULT NULL                COMMENT 'Endpoint（终端节点）',
    `bucket_name` varchar(255) DEFAULT NULL                COMMENT '桶名称',
    `domain`      varchar(255) DEFAULT NULL                COMMENT '自定义域名',
    `description` varchar(200) DEFAULT NULL                COMMENT '描述',
    `is_default`  bit(1)       NOT NULL DEFAULT b'0'       COMMENT '是否为默认存储',
    `sort`        int          NOT NULL DEFAULT 999        COMMENT '排序',
    `status`      tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
    `create_user` bigint(20)   NOT NULL                    COMMENT '创建人',
    `create_time` datetime     NOT NULL                    COMMENT '创建时间',
    `update_user` bigint(20)   DEFAULT NULL                COMMENT '修改人',
    `update_time` datetime     DEFAULT NULL                COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code`(`code`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储库表';