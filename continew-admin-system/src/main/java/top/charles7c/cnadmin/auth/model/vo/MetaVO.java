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

package top.charles7c.cnadmin.auth.model.vo;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 元数据信息
 *
 * @author Charles7c
 * @since 2023/2/26 22:51
 */
@Data
@Accessors(chain = true)
@Schema(description = "元数据信息")
public class MetaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    private String locale;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String icon;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏")
    private Boolean hideInMenu;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存")
    private Boolean ignoreCache;

    /**
     * 是否需要登录才能访问
     */
    @Schema(description = "是否需要登录才能访问")
    private Boolean requiresAuth = true;
}
