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

/**
 * 系统日志详情信息
 *
 * @author Charles7c
 * @since 2023/1/18 20:19
 */
@Data
@Schema(description = "系统日志详情信息")
public class SystemLogDetailResp extends LogResp {

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
     * 请求 URI
     */
    @Schema(description = "请求 URI", example = "/system/dept")
    private String requestUri;

    /**
     * 请求头
     */
    @Schema(description = "请求头", example = "{\"Origin\": [\"https://cnadmin.charles7c.top\"],...}")
    private String requestHeaders;

    /**
     * 请求体
     */
    @Schema(description = "请求体", example = "{\"name\": \"测试部\",...}")
    private String requestBody;

    /**
     * 响应头
     */
    @Schema(description = "响应头", example = "{\"Content-Type\": [\"application/json\"],...}")
    private String responseHeaders;

    /**
     * 响应体
     */
    @Schema(description = "响应体", example = "{\"success\":true},...")
    private String responseBody;

    /**
     * IP
     */
    @Schema(description = "IP", example = "192.168.0.1")
    private String ip;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "中国北京北京市")
    private String address;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    /**
     * 耗时（ms）
     */
    @Schema(description = "耗时（ms）", example = "58")
    private Long timeTaken;
}
