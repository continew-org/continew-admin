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

package top.continew.admin.tenant.service;

import java.util.List;

/**
 * 套餐和菜单关联业务接口
 *
 * @author Charles7c
 * @since 4.0.0
 */
public interface PackageMenuService {

    /**
     * 新增
     *
     * @param menuIds   菜单 ID 列表
     * @param packageId 套餐 ID
     * @return 关联关系是否已变更（true：已变更，含置空撤销；false：无变更）
     */
    boolean add(List<Long> menuIds, Long packageId);

    /**
     * 根据套餐 ID 查询
     *
     * @param packageId 套餐 ID
     * @return 菜单 ID 列表
     */
    List<Long> listMenuIdsByPackageId(Long packageId);
}
