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

import top.charles7c.cnadmin.common.annotation.TreeField;
import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 部门信息
 *
 * @author Charles7c
 * @since 2023/1/22 13:53
 */
@Data
@TreeField(value = "id", nameKey = "name")
@Schema(description = "部门信息")
public class DeptVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "测试部")
    private String name;

    /**
     * 上级部门 ID
     */
    @Schema(description = "上级部门 ID", example = "2")
    private Long parentId;

    /**
     * 部门排序
     */
    @Schema(description = "部门排序", example = "3")
    private Integer sort;

    /**
     * 状态（1：启用，2：禁用）
     */
    @Schema(description = "状态（1：启用，2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isSystem;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试部描述信息")
    private String description;

    @Override
    public Boolean getDisabled() {
        return this.getIsSystem();
    }
}
