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

import top.continew.admin.system.model.query.ClientQuery;
import top.continew.admin.system.model.req.ClientReq;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.starter.extension.crud.service.BaseService;

/**
 * 客户端业务接口
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
public interface ClientService extends BaseService<ClientResp, ClientResp, ClientQuery, ClientReq> {

    /**
     * 根据客户端 ID 查詢
     *
     * @param clientId 客户端 ID
     * @return 客户端信息
     */
    ClientResp getByClientId(String clientId);
}