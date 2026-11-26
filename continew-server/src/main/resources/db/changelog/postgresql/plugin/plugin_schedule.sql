-- liquibase formatted sql

-- changeset kai:1 dbms:postgresql
-- comment 初始化任务调度插件数据表
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(8000, '任务调度', 0, 1, '/schedule', 'Schedule', 'Layout', '/schedule/job', 'schedule', false, false, false, NULL, 8, 1, 1, NOW()),
(8010, '任务管理', 8000, 2, '/schedule/job', 'ScheduleJob', 'schedule/job/index', NULL, 'select-all', false, false, false, NULL, 1, 1, 1, NOW()),
(8011, '列表', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:list', 1, 1, 1, NOW()),
(8012, '详情', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:get', 2, 1, 1, NOW()),
(8013, '新增', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:create', 3, 1, 1, NOW()),
(8014, '修改', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:update', 4, 1, 1, NOW()),
(8015, '删除', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:delete', 5, 1, 1, NOW()),
(8016, '执行', 8010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:trigger', 6, 1, 1, NOW()),

(8020, '任务日志', 8000, 2, '/schedule/log', 'ScheduleLog', 'schedule/log/index', NULL, 'find-replace', false, false, false, NULL, 2, 1, 1, NOW()),
(8021, '列表', 8020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:list', 1, 1, 1, NOW()),
(8022, '停止', 8020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:stop', 3, 1, 1, NOW()),
(8023, '重试', 8020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:retry', 4, 1, 1, NOW());
