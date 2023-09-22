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

package top.charles7c.cnadmin.system.model.vo;

import java.io.Serializable;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.hutool.core.util.StrUtil;

/**
 * 参数信息
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Data
@Schema(description = "参数信息")
public class OptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称", example = "系统标题")
    private String name;

    /**
     * 参数键
     */
    @Schema(description = "参数键", example = "site_title")
    private String code;

    /**
     * 参数值
     */
    @Schema(description = "参数值", example = "ContiNew Admin")
    private String value;

    /**
     * 参数默认值
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