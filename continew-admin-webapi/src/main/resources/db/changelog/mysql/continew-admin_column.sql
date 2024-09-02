-- liquibase formatted sql

-- changeset Charles7c:3.3-1
ALTER TABLE `gen_field_config` ADD COLUMN `dict_code` varchar(30) DEFAULT NULL COMMENT '字典编码' AFTER `query_type`;

-- changeset Charles7c:3.3-2
ALTER TABLE `sys_role`
    ADD COLUMN `menu_check_strictly` bit(1) DEFAULT b'0' COMMENT '菜单选择是否父子节点关联' AFTER `is_system`,
    ADD COLUMN `dept_check_strictly` bit(1) DEFAULT b'0' COMMENT '部门选择是否父子节点关联' AFTER `menu_check_strictly`;
