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

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.enums.DataTypeEnum;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 角色信息
 *
 * @author Charles7c
 * @since 2023/2/8 23:05
 */
@Data
@Schema(description = "角色信息")
public class RoleVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "测试人员")
    private String name;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "test")
    private String code;

    /**
     * 数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）
     */
    @Schema(description = "数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）", example = "5")
    private DataScopeEnum dataScope;

    /**
     * 角色排序
     */
    @Schema(description = "角色排序", example = "1")
    private Integer sort;

    /**
     * 状态（1：启用，2：禁用）
     */
    @Schema(description = "状态（1：启用，2：禁用）", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 类型（1：系统内置，2：自定义）
     */
    @Schema(description = "类型（1：系统内置，2：自定义）", example = "2")
    private DataTypeEnum type;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试人员描述信息")
    private String description;

    @Override
    public Boolean getDisabled() {
        return DataTypeEnum.SYSTEM.equals(type);
    }
}
