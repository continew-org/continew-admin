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

package top.charles7c.cnadmin.monitor.model.resp;

import java.io.Serial;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统日志信息
 *
 * @author Charles7c
 * @since 2023/1/17 23:29
 */
@Data
@Schema(description = "系统日志信息")
public class SystemLogResp extends LogResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    @Schema(description = "状态码", example = "200")
    private Integer statusCode;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式", example = "POST")
    private String requestMethod;

    /**
     * 请求 URL
     */
    @Schema(description = "请求 URL", example = "http://api.charles7c.top/system/dept")
    private String requestUrl;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP", example = "192.168.0.1")
    private String clientIp;

    /**
     * IP归属地
     */
    @Schema(description = "IP归属地", example = "中国北京北京市")
    private String location;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    private String browser;

    /**
     * 请求耗时（ms）
     */
    @Schema(description = "请求耗时（ms）", example = "58")
    private Long elapsedTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 异常详情
     */
    @Schema(description = "异常详情")
    private String exceptionDetail;
}
