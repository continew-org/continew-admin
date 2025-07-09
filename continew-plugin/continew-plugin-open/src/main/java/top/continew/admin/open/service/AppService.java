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

package top.continew.admin.open.service;

import top.continew.admin.common.base.service.BaseService;
import top.continew.admin.open.model.entity.AppDO;
import top.continew.admin.open.model.query.AppQuery;
import top.continew.admin.open.model.req.AppReq;
import top.continew.admin.open.model.resp.AppDetailResp;
import top.continew.admin.open.model.resp.AppResp;
import top.continew.admin.open.model.resp.AppSecretResp;

/**
 * 应用业务接口
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
public interface AppService extends BaseService<AppResp, AppDetailResp, AppQuery, AppReq> {

    /**
     * 获取密钥
     *
     * @param id ID
     * @return 密钥信息
     */
    AppSecretResp getSecret(Long id);

    /**
     * 重置密钥
     *
     * @param id ID
     */
    void resetSecret(Long id);

    /**
     * 根据 Access Key 查询
     *
     * @param accessKey Access Key
     * @return 应用信息
     */
    AppDO getByAccessKey(String accessKey);

}