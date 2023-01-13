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

package top.charles7c.cnadmin.common.model.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码信息
 *
 * @author Charles7c
 * @since 2022/12/11 13:55
 */
@Data
@Accessors(chain = true)
@Schema(description = "验证码信息")
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码标识
     */
    @Schema(description = "验证码标识")
    private String uuid;

    /**
     * 验证码图片（Base64编码，带图片格式：data:image/gif;base64）
     */
    @Schema(description = "验证码图片（Base64编码，带图片格式：data:image/gif;base64）")
    private String img;
}
