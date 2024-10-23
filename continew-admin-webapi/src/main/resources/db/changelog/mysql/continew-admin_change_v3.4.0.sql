-- 消息通知表 新增通知范围 和 通知用户两个字段
START TRANSACTION;
ALTER TABLE sys_notice
    ADD COLUMN notice_scope INT NOT NULL COMMENT '通知范围' AFTER terminate_time,
    ADD COLUMN notice_users JSON DEFAULT NULL COMMENT '通知用户' AFTER notice_scope;
COMMIT;
