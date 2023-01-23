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
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 部门信息
 *
 * @author Charles7c
 * @since 2023/1/22 13:53
 */
@Data
@Accessors(chain = true)
@Schema(description = "部门信息")
public class DeptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 上级部门 ID
     */
    @Schema(description = "上级部门 ID")
    private Long parentId;

    /**
     * 部门排序
     */
    @Schema(description = "部门排序")
    private Integer deptSort;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    private DisEnableStatusEnum status;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人昵称
     */
    @Schema(description = "修改人昵称")
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 子部门列表
     */
    @Schema(description = "子部门列表")
    private List<DeptVO> children;
}
