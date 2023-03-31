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

package top.charles7c.cnadmin.system.model.entity;

import java.io.Serial;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

import top.charles7c.cnadmin.common.base.BaseDO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.MenuTypeEnum;

/**
 * 菜单实体
 *
 * @author Charles7c
 * @since 2023/2/15 20:14
 */
@Data
@TableName("sys_menu")
public class MenuDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 上级菜单 ID
     */
    private Long parentId;

    /**
     * 菜单类型（1：目录，2：菜单，3：按钮）
     */
    private MenuTypeEnum type;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 是否外链
     */
    private Boolean isExternal;

    /**
     * 是否缓存
     */
    private Boolean isCache;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单排序
     */
    private Integer sort;

    /**
     * 状态（1：启用，2：禁用）
     */
    private DisEnableStatusEnum status;
}
