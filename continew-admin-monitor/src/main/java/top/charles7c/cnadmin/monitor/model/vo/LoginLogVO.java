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

package top.charles7c.cnadmin.monitor.model.vo;

import java.io.Serial;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.monitor.enums.LogStatusEnum;

/**
 * 登录日志信息
 *
 * @author Charles7c
 * @since 2023/1/16 23:19
 */
@Data
@Schema(description = "登录日志信息")
public class LoginLogVO extends LogVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录行为
     */
    @Schema(description = "登录行为")
    private String description;

    /**
     * 登录状态（1：成功，2：失败）
     */
    @Schema(description = "登录状态（1：成功，2：失败）", type = "Integer", allowableValues = {"1", "2"})
    private LogStatusEnum status;

    /**
     * 登录 IP
     */
    @Schema(description = "登录 IP")
    private String clientIp;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点")
    private String location;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器")
    private String browser;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}
