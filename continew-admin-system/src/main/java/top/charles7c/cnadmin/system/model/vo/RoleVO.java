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

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.consts.Constants;
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 角色信息
 *
 * @author Charles7c
 * @since 2023/2/8 23:05
 */
@Data
@Accessors(chain = true)
@Schema(description = "角色信息")
public class RoleVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID")
    private Long roleId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 数据权限（1全部数据权限 2本部门及以下数据权限 3本部门数据权限 4仅本人数据权限 5自定义数据权限）
     */
    @Schema(description = "数据权限（1全部数据权限 2本部门及以下数据权限 3本部门数据权限 4仅本人数据权限 5自定义数据权限）")
    private DataScopeEnum dataScope;

    /**
     * 角色排序
     */
    @Schema(description = "角色排序")
    private Integer roleSort;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 是否禁用修改
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    public Boolean getDisabled() {
        if (Constants.SUPER_ADMIN.equals(roleCode)) {
            return true;
        }
        return disabled;
    }
}
