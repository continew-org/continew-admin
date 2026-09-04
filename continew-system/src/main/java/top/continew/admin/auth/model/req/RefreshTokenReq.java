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

package top.continew.admin.auth.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Refresh Token 请求参数。
 *
 * <p>当前 Web 客户端不填写该字段，后端从 HttpOnly Cookie 读取；该字段为未来 App、
 * 微信小程序的 BODY 传输模式预留。</p>
 *
 * @author luoqiz
 */
@Data
@Schema(description = "Refresh Token 请求参数")
public class RefreshTokenReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** App / 小程序提交的 Refresh Token，浏览器模式为空 */
    @Schema(description = "Refresh Token（浏览器 Cookie 模式不填写）")
    private String refreshToken;
}
