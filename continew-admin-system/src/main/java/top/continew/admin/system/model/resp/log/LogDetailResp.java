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

package top.continew.admin.system.model.resp.log;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.condition.ConditionOnPropertyNotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.constant.ContainerConstants;
import top.continew.admin.system.enums.LogStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日志详情信息
 *
 * @author Charles7c
 * @since 2023/1/18 20:19
 */
@Data
@Schema(description = "日志详情信息")
public class LogDetailResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 链路 ID
     */
    @Schema(description = "链路 ID", example = "904846526308876288")
    private String traceId;

    /**
     * 日志描述
     */
    @Schema(description = "日志描述", example = "新增数据")
    private String description;

    /**
     * 所属模块
     */
    @Schema(description = "所属模块", example = "部门管理")
    private String module;

    /**
     * 请求 URL
     */
    @Schema(description = "请求 URL", example = "http://api.continew.top/system/dept")
    private String requestUrl;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式", example = "POST")
    private String requestMethod;

    /**
     * 请求头
     */
    @Schema(description = "请求头", example = "{\"Origin\": [\"https://admin.continew.top\"],...}")
    private String requestHeaders;

    /**
     * 请求体
     */
    @Schema(description = "请求体", example = "{\"name\": \"测试部\",...}")
    private String requestBody;

    /**
     * 状态码
     */
    @Schema(description = "状态码", example = "200")
    private Integer statusCode;

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
     * 耗时（ms）
     */
    @Schema(description = "耗时（ms）", example = "58")
    private Long timeTaken;

    /**
     * IP
     */
    @Schema(description = "IP", example = "")
    private String ip;

    /**
     * IP 归属地
     */
    @Schema(description = "IP 归属地", example = "中国北京北京市")
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
     * 状态
     */
    @Schema(description = "状态（1：成功；2：失败）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private LogStatusEnum status;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 创建人
     */
    @JsonIgnore
    @ConditionOnPropertyNotNull
    @Assemble(prop = ":createUserString", container = ContainerConstants.USER_NICKNAME)
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "张三")
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;
}
