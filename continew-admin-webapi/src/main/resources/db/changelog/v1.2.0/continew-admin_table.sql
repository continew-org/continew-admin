-- liquibase formatted sql

-- changeset Charles7c:1
CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `name` varchar(50) NOT NULL COMMENT '字典名称',
    `code` varchar(50) NOT NULL COMMENT '字典编码',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_name`(`name`) USING BTREE,
    UNIQUE INDEX `uk_code`(`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

CREATE TABLE IF NOT EXISTS `sys_dict_item` (
    `id` bigint(20) UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    `label` varchar(50) NOT NULL COMMENT '字典标签',
    `value` varchar(50) NOT NULL COMMENT '字典值',
    `color` varchar(20) DEFAULT NULL COMMENT '背景颜色',
    `sort` int(11) UNSIGNED DEFAULT 999 COMMENT '字典项排序',
    `description` varchar(512) DEFAULT NULL COMMENT '描述',
    `dict_id` bigint(20) UNSIGNED NOT NULL COMMENT '字典ID',
    `create_user` bigint(20) UNSIGNED NOT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` bigint(20) UNSIGNED DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_value_dict_id`(`value`, `dict_id`) USING BTREE,
    INDEX `idx_dict_id`(`dict_id`) USING BTREE,
    INDEX `idx_create_user`(`create_user`) USING BTREE,
    INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';
