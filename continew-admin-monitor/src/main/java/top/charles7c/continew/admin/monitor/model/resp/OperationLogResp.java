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

package top.charles7c.continew.admin.monitor.model.resp;

import java.io.Serial;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.continew.admin.monitor.enums.LogStatusEnum;

/**
 * 操作日志信息
 *
 * @author Charles7c
 * @since 2023/1/14 18:27
 */
@Data
@Schema(description = "操作日志信息")
public class OperationLogResp extends LogResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 操作内容
     */
    @Schema(description = "操作内容", example = "新增数据")
    private String description;

    /**
     * 所属模块
     */
    @Schema(description = "所属模块", example = "部门管理")
    private String module;

    /**
     * 操作状态
     */
    @Schema(description = "操作状态（1：成功；2：失败）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private LogStatusEnum status;

    /**
     * 操作IP
     */
    @Schema(description = "操作IP", example = "192.168.0.1")
    private String clientIp;

    /**
     * 操作地点
     */
    @Schema(description = "操作地点", example = "中国北京北京市")
    private String location;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    private String browser;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}
