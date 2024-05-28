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

package top.continew.admin.system.model.resp;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 参数信息
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Data
@Schema(description = "参数信息")
public class OptionResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "系统标题")
    private String name;

    /**
     * 键
     */
    @Schema(description = "键", example = "site_title")
    private String code;

    /**
     * 值
     */
    @Schema(description = "值", example = "ContiNew Admin")
    private String value;

    /**
     * 默认值
     */
    @JsonIgnore
    private String defaultValue;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "用于显示登录页面的系统标题。")
    private String description;

    public String getValue() {
        return StrUtil.nullToDefault(value, defaultValue);
    }
}