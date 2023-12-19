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

package top.charles7c.continew.admin.tool.model.resp;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 生成预览信息
 *
 * @author Charles7c
 * @since 2023/12/19 21:34
 */
@Data
@Schema(description = "生成预览信息")
public class GeneratePreviewResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "UserController.java")
    private String fileName;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "public class UserController {...}")
    private String content;

    /**
     * 是否为后端代码
     */
    @Schema(hidden = true)
    @JsonIgnore
    private boolean backend;
}
