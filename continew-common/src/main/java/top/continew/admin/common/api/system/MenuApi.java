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

package top.continew.admin.common.api.system;

import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 菜单业务 API
 *
 * @author Charles7c
 * @since 2025/7/26 9:53
 */
public interface MenuApi {

    /**
     * 查询树结构列表
     *
     * @param excludeMenuIds 排除的菜单 ID 列表
     * @param isSimple       是否是简单树结构
     * @return 树结构列表
     */
    List<Tree<Long>> listTree(List<Long> excludeMenuIds, boolean isSimple);
}
