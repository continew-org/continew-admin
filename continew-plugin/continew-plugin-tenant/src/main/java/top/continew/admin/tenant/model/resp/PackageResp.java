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

package top.continew.admin.tenant.model.resp;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.base.model.resp.BaseDetailResp;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.starter.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;

/**
 * 套餐响应参数
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 11:25
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "套餐响应参数")
public class PackageResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "初级套餐")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序", order = 3)
    private Integer sort;

    /**
     * 菜单选择是否父子节点关联
     */
    @Schema(description = "菜单选择是否父子节点关联", example = "true")
    @ExcelProperty(value = "菜单选择是否父子节点关联", order = 4)
    private Boolean menuCheckStrictly;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "初级套餐")
    @ExcelProperty(value = "描述", order = 5)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 6)
    private DisEnableStatusEnum status;
}
