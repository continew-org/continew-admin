-- liquibase formatted sql

-- changeset snail-job-server:1.1.0
-- 默认命名空间：Default
INSERT INTO sj_namespace (id, name, unique_id, create_dt, update_dt, deleted)
VALUES (1, 'Default', '764d604ec6fc45f68cd92514c40e9e1a', NOW(), NOW(), 0);

-- 默认用户：admin/admin
INSERT INTO sj_system_user (username, password, role)
VALUES ('admin', '465c194afb65670f38322df087f0a9bb225cc257e43eb4ac5a0c98ef5b3173ac', 2);

-- 默认分组：continew-admin
INSERT INTO sj_group_config (id, namespace_id, group_name, description, token, group_status, version, group_partition, id_generator_mode, init_scene, create_dt, update_dt)
VALUES (1, '764d604ec6fc45f68cd92514c40e9e1a', 'continew-admin', '默认分组', 'SJ_Wyz3dmsdbDOkDujOTSSoBjGQP1BMsVnj', 1, 1, 0, 2, 1, NOW(), NOW());

-- 默认任务：NoticePublishJob
INSERT INTO sj_job
(namespace_id, group_name, job_name, args_type, next_trigger_at, job_status, task_type, route_key, executor_type, executor_info, trigger_type, trigger_interval, block_strategy, executor_timeout, max_retry_times, parallel_num, retry_interval, bucket_index, resident, notify_ids, owner_id, description, ext_attrs, deleted, create_dt, update_dt)
VALUES ('764d604ec6fc45f68cd92514c40e9e1a', 'continew-admin', '公告发布', 1, 1747546500000, 1, 1, 4, 1, 'NoticePublishJob', 3, '0 * * * * ?', 1, 60, 3, 1, 1, 27, 0, '', NULL, '定时发布公告', '', 0, NOW(), NOW());