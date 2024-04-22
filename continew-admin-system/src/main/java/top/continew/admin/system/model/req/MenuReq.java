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

package top.continew.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.MenuTypeEnum;
import top.continew.starter.extension.crud.model.req.BaseReq;

import java.io.Serial;

/**
 * 创建或修改菜单信息
 *
 * @author Charles7c
 * @since 2023/2/15 20:21
 */
@Data
@Schema(description = "创建或修改菜单信息")
public class MenuReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 类型
     */
    @Schema(description = "类型（1：目录；2：菜单；3：按钮）", type = "Integer", allowableValues = {"1", "2", "3"}, example = "2")
    @NotNull(message = "类型非法")
    private MenuTypeEnum type;

    /**
     * 图标
     */
    @Schema(description = "图标", example = "user")
    @Length(max = 50, message = "图标长度不能超过 {max} 个字符")
    private String icon;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "用户管理")
    @NotBlank(message = "标题不能为空")
    @Length(max = 30, message = "标题长度不能超过 {max} 个字符")
    private String title;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    @Min(value = 1, message = "排序最小值为 {value}")
    private Integer sort;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    @Length(max = 100, message = "权限标识长度不能超过 {max} 个字符")
    private String permission;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system/user")
    @Length(max = 255, message = "路由地址长度不能超过 {max} 个字符")
    private String path;

    /**
     * 组件名称
     */
    @Schema(description = "组件名称", example = "User")
    @Length(max = 50, message = "组件名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "/system/user/index")
    @Length(max = 255, message = "组件路径长度不能超过 {max} 个字符")
    private String component;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    private String redirect;

    /**
     * 是否外链
     */
    @Schema(description = "是否外链", example = "false")
    private Boolean isExternal;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存", example = "false")
    private Boolean isCache;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏", example = "false")
    private Boolean isHidden;

    /**
     * 上级菜单 ID
     */
    @Schema(description = "上级菜单 ID", example = "1000")
    private Long parentId;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    @NotNull(message = "状态非法")
    private DisEnableStatusEnum status;
}
