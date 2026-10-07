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

package top.continew.admin.tenant.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.api.tenant.PackageMenuApi;
import top.continew.admin.tenant.service.PackageMenuService;

import java.util.List;

/**
 * 套餐和菜单关联业务 API 实现
 *
 * @author Charles7c
 * @since 4.0.0
 */
@Service
@RequiredArgsConstructor
public class PackageMenuApiImpl implements PackageMenuApi {

    private final PackageMenuService baseService;

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseService.listMenuIdsByPackageId(packageId);
    }
}
