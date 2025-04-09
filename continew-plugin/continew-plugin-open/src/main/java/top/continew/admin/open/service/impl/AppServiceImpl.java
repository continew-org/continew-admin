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

package top.continew.admin.open.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.open.mapper.AppMapper;
import top.continew.admin.open.model.entity.AppDO;
import top.continew.admin.open.model.query.AppQuery;
import top.continew.admin.open.model.req.AppReq;
import top.continew.admin.open.model.resp.AppDetailResp;
import top.continew.admin.open.model.resp.AppResp;
import top.continew.admin.open.model.resp.AppSecretResp;
import top.continew.admin.open.service.AppService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.service.BaseServiceImpl;

/**
 * 应用业务实现
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends BaseServiceImpl<AppMapper, AppDO, AppResp, AppDetailResp, AppQuery, AppReq> implements AppService {

    @Override
    public void beforeCreate(AppReq req) {
        req.setAccessKey(Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY)
            .substring(0, 30));
        req.setSecretKey(this.generateSecret());
    }

    @Override
    public AppSecretResp getSecret(Long id) {
        AppDO app = super.getById(id);
        AppSecretResp appSecretResp = new AppSecretResp();
        appSecretResp.setAccessKey(app.getAccessKey());
        appSecretResp.setSecretKey(app.getSecretKey());
        return appSecretResp;
    }

    @Override
    public void resetSecret(Long id) {
        super.getById(id);
        AppDO app = new AppDO();
        app.setSecretKey(this.generateSecret());
        baseMapper.update(app, Wrappers.lambdaQuery(AppDO.class).eq(AppDO::getId, id));
    }

    @Override
    public AppDO getByAccessKey(String accessKey) {
        return baseMapper.selectByAccessKey(accessKey);
    }

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    private String generateSecret() {
        return Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY);
    }
}