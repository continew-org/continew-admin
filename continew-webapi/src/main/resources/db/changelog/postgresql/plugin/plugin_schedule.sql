-- liquibase formatted sql

-- changeset kai:1
-- comment 初始化任务调度插件
-- 初始化默认菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(3000, '任务调度', 0, 1, '/schedule', 'Schedule', 'Layout', '/schedule/job', 'schedule', false, false, false, NULL, 3, 1, 1, NOW()),
(3010, '任务管理', 3000, 2, '/schedule/job', 'ScheduleJob', 'schedule/job/index', NULL, 'select-all', false, false, false, NULL, 1, 1, 1, NOW()),
(3011, '列表', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:list', 1, 1, 1, NOW()),
(3012, '详情', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:get', 2, 1, 1, NOW()),
(3013, '新增', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:create', 3, 1, 1, NOW()),
(3014, '修改', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:update', 4, 1, 1, NOW()),
(3015, '删除', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:delete', 5, 1, 1, NOW()),
(3016, '执行', 3010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:job:trigger', 6, 1, 1, NOW()),
(3020, '任务日志', 3000, 2, '/schedule/log', 'ScheduleLog', 'schedule/log/index', NULL, 'find-replace', false, false, false, NULL, 2, 1, 1, NOW()),
(3021, '列表', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:list', 1, 1, 1, NOW()),
(3022, '详情', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:get', 2, 1, 1, NOW()),
(3023, '停止', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:stop', 3, 1, 1, NOW()),
(3024, '重试', 3020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'schedule:log:retry', 4, 1, 1, NOW());
