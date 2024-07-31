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

package top.continew.admin.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 任务路由策略枚举
 *
 * @author Charles7c
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobRouteStrategyEnum implements BaseEnum<Integer> {

    /**
     * 轮询
     */
    POLLING(4, "轮询"),

    /**
     * 随机
     */
    RANDOM(2, "随机"),

    /**
     * 一致性哈希
     */
    HASH(1, "一致性哈希"),

    /**
     * LRU
     */
    LRU(3, "LRU"),;

    private final Integer value;
    private final String description;
}
