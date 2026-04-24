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

package top.continew.admin.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 双token模式
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DoubleTokenLoginResp extends LoginResp {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJ0eXAiOiJlV1QiLCJhbGciqiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb29pbiIsImxvZ2luSWQiOjEsInJuU3RyIjoiSjd4SUljYnU5cmNwU09vQ3Uyc1ND1BYYTYycFRjcjAifQ.KUPOYm-2wfuLUSfEEAbpGE527fzmkAJG7sMNcQ0pUZ8")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "12a5c5e1f87d4b4db614c6229c2c8916")
    private String refreshToken;

    /**
     * 访问令牌有效时长（秒）
     */
    @Schema(description = "访问令牌有效时长（秒）", example = "1800")
    private Long accessExpiresIn;

    /**
     * 刷新令牌有效时长（秒）
     */
    @Schema(description = "刷新令牌有效时长（秒）", example = "2592000")
    private Long refreshExpiresIn;
}
