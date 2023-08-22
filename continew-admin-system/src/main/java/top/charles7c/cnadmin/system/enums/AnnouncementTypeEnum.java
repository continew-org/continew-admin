/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.charles7c.cnadmin.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import top.charles7c.cnadmin.common.base.BaseEnum;

/**
 * 公告类型枚举（计划 v1.2.0 增加字典管理，用于维护此类信息）
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Getter
@RequiredArgsConstructor
public enum AnnouncementTypeEnum implements BaseEnum<Integer, String> {

    /** 活动 */
    ACTIVITY(1, "活动"),

    /** 消息 */
    MESSAGE(2, "消息"),

    /** 通知 */
    NOTICE(3, "通知"),;

    private final Integer value;
    private final String description;
}
