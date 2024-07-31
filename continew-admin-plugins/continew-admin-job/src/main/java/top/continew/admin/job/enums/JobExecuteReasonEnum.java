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
 * 任务执行原因枚举
 *
 * @author Charles7c
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobExecuteReasonEnum implements BaseEnum<Integer> {

    /**
     * 无
     */
    NONE(0, "无"),

    /**
     * 任务执行超时
     */
    TIME_OUT(1, "任务执行超时"),

    /**
     * 无客户端节点
     */
    CLIENT_NOT_FOUND(2, "无客户端节点"),

    /**
     * 任务已关闭
     */
    TASK_CLOSED(3, "任务已关闭"),

    /**
     * 任务丢弃
     */
    TASK_DROPPED(4, "任务丢弃"),

    /**
     * 任务被覆盖
     */
    TASK_COVERED(5, "任务被覆盖"),

    /**
     * 无可执行任务项
     */
    TASK_NONE(6, "无可执行任务项"),

    /**
     * 任务执行期间发生非预期异常
     */
    TASK_EXCEPTION(7, "任务执行期间发生非预期异常"),

    /**
     * 手动停止
     */
    MANUAL_STOP(8, "手动停止"),

    /**
     * 条件节点执行异常
     */
    NODE_EXCEPTION(9, "条件节点执行异常"),

    /**
     * 任务中断
     */
    TASK_INTERRUPT(10, "任务中断"),

    /**
     * 回调节点执行异常
     */
    CALLBACK_EXCEPTION(11, "回调节点执行异常"),

    /**
     * 无需处理
     */
    NO_NEED_PROCESS(12, "无需处理"),

    /**
     * 节点关闭跳过执行
     */
    NODE_SKIP(13, "节点关闭跳过执行"),

    /**
     * 判定未通过
     */
    NOT_PASS(14, "判定未通过"),

    /**
     * 任务已完成
     */
    TASK_FINISHED(15, "任务已完成"),

    /**
     * 任务状态
     */
    TASK_RUNNING(16, "任务正在执行"),

    /**
     * 任务等待执行
     */
    TASK_WAITING(17, "任务等待执行"),

    /**
     * 任务执行失败
     */
    TASK_FAILED(18, "任务执行失败"),

    /**
     * 任务执行成功
     */
    TASK_SUCCESS(19, "任务执行成功"),;

    private final Integer value;
    private final String description;
}
