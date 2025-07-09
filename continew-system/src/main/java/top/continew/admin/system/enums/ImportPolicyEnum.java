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

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

import java.util.List;

/**
 * 数据导入策略
 *
 * @author Kils
 * @since 2024-06-17 18:33
 */
@Getter
@RequiredArgsConstructor
public enum ImportPolicyEnum implements BaseEnum<Integer> {

    /**
     * 跳过该行
     */
    SKIP(1, "跳过该行"),

    /**
     * 修改数据
     */
    UPDATE(2, "修改数据"),

    /**
     * 停止导入
     */
    EXIT(3, "停止导入");

    private final Integer value;
    private final String description;

    public boolean validate(ImportPolicyEnum importPolicy, String data, List<String> existList) {
        return this == importPolicy && CollUtil.isNotEmpty(existList) && existList.contains(data);
    }
}
