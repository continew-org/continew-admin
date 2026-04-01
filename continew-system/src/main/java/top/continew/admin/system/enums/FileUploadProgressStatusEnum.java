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
import top.continew.starter.core.enums.BaseEnum;

/**
 * 文件上传进度状态枚举
 *
 * @author echo
 * @since 2026/3/3 14:55
 */
@Getter
@RequiredArgsConstructor
public enum FileUploadProgressStatusEnum implements BaseEnum<String> {

    /**
     * 初始化
     */
    INIT("INIT", "初始化"),

    /**
     * 上传中
     */
    UPLOADING("UPLOADING", "上传中"),

    /**
     * 收尾处理中（文件字节已传输完成，等待后端处理完成）
     */
    FINALIZING("FINALIZING", "收尾处理中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 失败
     */
    FAILED("FAILED", "失败"),

    /**
     * 未找到任务
     */
    NOT_FOUND("NOT_FOUND", "未找到任务");

    private final String value;
    private final String description;
}
