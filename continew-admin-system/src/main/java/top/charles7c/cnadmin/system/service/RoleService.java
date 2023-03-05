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

package top.charles7c.cnadmin.system.service;

import java.util.List;
import java.util.Set;

import top.charles7c.cnadmin.common.base.BaseService;
import top.charles7c.cnadmin.common.model.vo.LabelValueVO;
import top.charles7c.cnadmin.system.model.query.RoleQuery;
import top.charles7c.cnadmin.system.model.request.RoleRequest;
import top.charles7c.cnadmin.system.model.vo.RoleDetailVO;
import top.charles7c.cnadmin.system.model.vo.RoleVO;

/**
 * 角色业务接口
 *
 * @author Charles7c
 * @since 2023/2/8 23:15
 */
public interface RoleService extends BaseService<RoleVO, RoleDetailVO, RoleQuery, RoleRequest> {

    /**
     * 构建字典
     *
     * @param list
     *            原始列表数据
     * @return 字典列表
     */
    List<LabelValueVO<Long>> buildDict(List<RoleVO> list);

    /**
     * 根据 ID 列表查询
     *
     * @param ids
     *            ID 列表
     * @return 名称列表
     */
    List<String> listNameByIds(List<Long> ids);

    /**
     * 根据用户 ID 查询角色编码
     *
     * @param userId
     *            用户 ID
     * @return 角色编码集合
     */
    Set<String> listCodeByUserId(Long userId);
}
