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

package top.charles7c.continew.admin.auth.model.resp;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 路由信息
 *
 * @author Charles7c
 * @since 2023/2/26 22:51
 */
@Data
@Schema(description = "路由信息")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system/user")
    private String path;

    /**
     * 组件名称
     */
    @Schema(description = "组件名称", example = "User")
    private String name;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "/system/user/index")
    private String component;

    /**
     * 元数据
     */
    @Schema(description = "元数据")
    private MetaResp meta;

    /**
     * 子路由列表
     */
    @Schema(description = "子路由列表")
    private List<RouteResp> children;
}
