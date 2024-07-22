-- liquibase formatted sql

-- changeset snail-job-server:1.1.0
-- 默认命名空间：Default
INSERT INTO `sj_namespace` (`id`, `name`, `unique_id`, `create_dt`, `update_dt`, `deleted`)
VALUES (1, 'Default', '764d604ec6fc45f68cd92514c40e9e1a', NOW(), NOW(), 0);

-- 默认分组：continew-admin
INSERT INTO `sj_group_config` (`id`, `namespace_id`, `group_name`, `description`, `token`, `group_status`, `version`, `group_partition`, `id_generator_mode`, `init_scene`, `bucket_index`, `create_dt`, `update_dt`)
VALUES (1, '764d604ec6fc45f68cd92514c40e9e1a', 'continew-admin', '默认分组', 'SJ_Wyz3dmsdbDOkDujOTSSoBjGQP1BMsVnj', 1, 1, 0, 2, 1, 119, NOW(), NOW());

-- 默认用户：admin/admin
INSERT INTO `sj_system_user` (username, password, role)
VALUES ('admin', '465c194afb65670f38322df087f0a9bb225cc257e43eb4ac5a0c98ef5b3173ac', 2);
