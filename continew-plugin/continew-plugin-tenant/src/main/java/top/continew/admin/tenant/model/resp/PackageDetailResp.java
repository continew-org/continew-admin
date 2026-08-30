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

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.tenant.service.PackageMenuService;

import java.io.Serial;
import java.util.List;

/**
 * 套餐详情响应参数
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 11:25
 */
@Data
@Schema(description = "套餐详情响应参数")
@AssembleMethod(key = "id", prop = ":menuIds", targetType = PackageMenuService.class,
    method = @ContainerMethod(bindMethod = "listMenuIdsByPackageId",
        type = MappingType.ORDER_OF_KEYS))
public class PackageDetailResp extends PackageResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联的菜单 ID 列表
     */
    @Schema(description = "关联的菜单 ID 列表", example = "[1000, 1010, 1011]")
    private List<Long> menuIds;
}
