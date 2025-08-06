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

package top.continew.admin.system.api;

import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.api.system.MenuApi;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.service.MenuService;

import java.util.List;

/**
 * 菜单业务 API 实现
 *
 * @author Charles7c
 * @since 2025/7/26 9:53
 */
@Service
@RequiredArgsConstructor
public class MenuApiImpl implements MenuApi {

    private final MenuService baseService;

    @Override
    public List<Tree<Long>> listTree(List<Long> excludeMenuIds, boolean isSimple) {
        MenuQuery query = new MenuQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        // 过滤掉租户不能使用的菜单
        query.setExcludeMenuIdList(excludeMenuIds);
        return baseService.tree(query, null, isSimple);
    }
}
