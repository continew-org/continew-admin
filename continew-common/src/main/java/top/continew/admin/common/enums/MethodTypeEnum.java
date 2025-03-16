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
import top.continew.starter.core.enums.BaseEnum;

/**
 * 方法类型枚举
 *
 * @author luoqiz
 * @since 2025/03/16 13:38
 */
@Getter
@RequiredArgsConstructor
public enum MethodTypeEnum implements BaseEnum<String> {

    /**
     * 新增
     */
    ADD("add", "新增"),

    /**
     * 更新
     */
    UPDATE("update", "更新"),

    /**
     * 删除
     */
    DELETE("delete", "删除"),
    /**
     * 删除
     */
    SEARCH("search", "查询"),;

    private final String value;
    private final String description;

}
