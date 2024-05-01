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

package top.continew.admin.system.service;

import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.req.DeptReq;
import top.continew.admin.system.model.resp.DeptResp;
import top.continew.starter.data.mybatis.plus.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

import java.util.List;

/**
 * 部门业务接口
 *
 * @author Charles7c
 * @since 2023/1/22 17:54
 */
public interface DeptService extends BaseService<DeptResp, DeptResp, DeptQuery, DeptReq>, IService<DeptDO> {

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<DeptDO> listChildren(Long id);
}
