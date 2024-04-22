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

package top.continew.admin.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.data.mybatis.plus.base.IBaseEnum;

/**
 * 存储类型枚举
 *
 * @author Charles7c
 * @since 2023/12/27 21:45
 */
@Getter
@RequiredArgsConstructor
public enum StorageTypeEnum implements IBaseEnum<Integer> {

    /**
     * 兼容S3协议存储
     */
    S3(1, "兼容S3协议存储"),

    /**
     * 本地存储
     */
    LOCAL(2, "本地存储"),;

    private final Integer value;
    private final String description;
}
