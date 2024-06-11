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

package top.continew.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.admin.common.constant.UiConstants;
import top.continew.starter.data.mybatis.plus.base.IBaseEnum;

/**
 * 消息类型枚举
 *
 * @author Charles7c
 * @since 2023/11/2 20:08
 */
@Getter
@RequiredArgsConstructor
public enum MessageTypeEnum implements IBaseEnum<Integer> {

    /**
     * 安全消息
     */
    SECURITY(1, "安全消息", UiConstants.COLOR_PRIMARY),;

    private final Integer value;
    private final String description;
    private final String color;
}
