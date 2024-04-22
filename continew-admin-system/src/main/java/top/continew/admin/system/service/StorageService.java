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

import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.StorageQuery;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.model.resp.StorageResp;
import top.continew.starter.data.mybatis.plus.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

/**
 * 存储业务接口
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
public interface StorageService extends BaseService<StorageResp, StorageResp, StorageQuery, StorageReq>, IService<StorageDO> {

    /**
     * 查询默认存储
     *
     * @return 存储信息
     */
    StorageDO getDefaultStorage();

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 存储信息
     */
    StorageDO getByCode(String code);

    /**
     * 加载存储
     *
     * @param req 存储信息
     */
    void load(StorageReq req);

    /**
     * 卸载存储
     *
     * @param req 存储信息
     */
    void unload(StorageReq req);
}