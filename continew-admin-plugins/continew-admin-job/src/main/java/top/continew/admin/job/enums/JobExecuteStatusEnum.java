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
import top.continew.admin.common.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 任务执行状态枚举
 *
 * @author Charles7c
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobExecuteStatusEnum implements BaseEnum<Integer> {

    /**
     * 待处理
     */
    WAITING(1, "待处理", UiConstants.COLOR_PRIMARY),

    /**
     * 运行中
     */
    RUNNING(2, "运行中", UiConstants.COLOR_WARNING),

    /**
     * 成功
     */
    SUCCEEDED(3, "成功", UiConstants.COLOR_SUCCESS),

    /**
     * 已失败
     */
    FAILED(4, "已失败", UiConstants.COLOR_ERROR),

    /**
     * 已停止
     */
    STOPPED(5, "已停止", UiConstants.COLOR_ERROR),

    /**
     * 已取消
     */
    CANCELED(6, "已取消", UiConstants.COLOR_DEFAULT),;

    private final Integer value;
    private final String description;
    private final String color;
}
