-- liquibase formatted sql

-- changeset TopHub:2
-- comment 新增 短信厂商 字典

-- 新增 短信厂商 字典数据
INSERT INTO "sys_dict"
("id", "name", "code", "description", "is_system", "create_user", "create_time")
VALUES
(4, '短信厂商', 'sms_supplier_type', NULL, true, 1, NOW());

-- 新增 短信厂商 字典项
INSERT INTO "sys_dict_item"
("id", "label", "value", "color", "sort", "description", "status", "dict_id", "create_user", "create_time")
VALUES
(8, '阿里云', 'alibaba', 'primary', 1, NULL, 1, 4, 1, NOW()),
(9, '容联云', 'cloopen', 'success', 2, NULL, 1, 4, 1, NOW()),
(10, '天翼云', 'ctyun', 'warning', 3, NULL, 1, 4, 1, NOW()),
(11, '亿美软通', 'emay', 'primary', 4, NULL, 1, 4, 1, NOW()),
(12, '华为', 'huawei', 'success', 5, NULL, 1, 4, 1, NOW()),
(13, '京东', 'jdcloud', 'warning', 6, NULL, 1, 4, 1, NOW()),
(14, '网易', 'netease', 'primary', 7, NULL, 1, 4, 1, NOW()),
(15, '腾讯', 'tencent', 'success', 8, NULL, 1, 4, 1, NOW()),
(16, '合一', 'unisms', 'warning', 9, NULL, 1, 4, 1, NOW()),
(17, '云片', 'yunpian', 'primary', 10, NULL, 1, 4, 1, NOW()),
(18, '助通', 'zhutong', 'success', 11, NULL, 1, 4, 1, NOW()),
(19, '联麓', 'lianlu', 'warning', 12, NULL, 1, 4, 1, NOW()),
(20, '鼎众', 'dingzhong', 'primary', 13, NULL, 1, 4, 1, NOW()),
(21, '七牛云', 'qiniu', 'success', 14, NULL, 1, 4, 1, NOW()),
(22, '创蓝', 'chuanglan', 'warning', 15, NULL, 1, 4, 1, NOW()),
(23, '极光', 'jiguang', 'primary', 16, NULL, 1, 4, 1, NOW()),
(24, '布丁云V2', 'buding_v2', 'success', 17, NULL, 1, 4, 1, NOW()),
(25, '中国移动 云MAS', 'mas', 'warning', 18, NULL, 1, 4, 1, NOW()),
(26, '百度云', 'baidu', 'primary', 19, NULL, 1, 4, 1, NOW()),
(27, '螺丝帽', 'luosimao', 'success', 20, NULL, 1, 4, 1, NOW()),
(28, 'SUBMAIL短信', 'mysubmail', 'success', 21, NULL, 1, 4, 1, NOW()),
(29, '单米科技短信', 'danmi', 'success', 22, NULL, 1, 4, 1, NOW()),
(30, '联通一信通', 'yixintong', 'success', 23, NULL, 1, 4, 1, NOW());