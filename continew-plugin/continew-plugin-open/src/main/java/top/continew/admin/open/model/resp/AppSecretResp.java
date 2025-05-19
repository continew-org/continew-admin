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

package top.continew.admin.open.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用密钥响应参数
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用密钥响应参数")
public class AppSecretResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @Schema(description = "Secret Key（私有密钥）", example = "MDI2YzQ3YTU1NGEyNDM1ZWIwNTU5NmNjNmZjM2M2Nzg=")
    private String secretKey;
}
