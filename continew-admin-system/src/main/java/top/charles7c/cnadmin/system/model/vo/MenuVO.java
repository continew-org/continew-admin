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

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.MenuTypeEnum;

/**
 * 菜单信息
 *
 * @author Charles7c
 * @since 2023/2/15 20:23
 */
@Data
@Accessors(chain = true)
@Schema(description = "菜单信息")
public class MenuVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID
     */
    @Schema(description = "菜单 ID")
    private Long menuId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;

    /**
     * 上级菜单 ID
     */
    @Schema(description = "上级菜单 ID")
    private Long parentId;

    /**
     * 菜单类型（1目录 2菜单 3按钮）
     */
    @Schema(description = "菜单类型（1目录 2菜单 3按钮）")
    private MenuTypeEnum menuType;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址")
    private String path;

    /**
     * 组件名称
     */
    @Schema(description = "组件名称")
    private String name;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    private String component;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String icon;

    /**
     * 是否外链
     */
    @Schema(description = "是否外链")
    private Boolean isExternal;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存")
    private Boolean isCache;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏")
    private Boolean isHidden;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String permission;

    /**
     * 菜单排序
     */
    @Schema(description = "菜单排序")
    private Integer menuSort;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    private DisEnableStatusEnum status;

    /**
     * 子菜单列表
     */
    @Schema(description = "子菜单列表")
    private List<MenuVO> children;
}
