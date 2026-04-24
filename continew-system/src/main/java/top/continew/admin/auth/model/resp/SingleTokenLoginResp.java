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
 * 单token模式，只有访问的token
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SingleTokenLoginResp extends LoginResp {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJ0eXAiOiJlV1QiLCJhbGciqiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb29pbiIsImxvZ2luSWQiOjEsInJuU3RyIjoiSjd4SUljYnU5cmNwU09vQ3Uyc1ND1BYYTYycFRjcjAifQ.KUPOYm-2wfuLUSfEEAbpGE527fzmkAJG7sMNcQ0pUZ8")
    private String token;

    /**
     * 有效时长（秒）
     */
    @Schema(description = "有效时长（秒）", example = "8600")
    private Long expiresIn;

}
