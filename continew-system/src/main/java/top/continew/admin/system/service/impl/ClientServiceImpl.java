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

package top.continew.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.model.entity.ClientDO;
import top.continew.admin.system.model.query.ClientQuery;
import top.continew.admin.system.model.req.ClientReq;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端业务实现
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl
    extends BaseServiceImpl<ClientMapper, ClientDO, ClientResp, ClientResp, ClientQuery, ClientReq>
    implements ClientService {

    private final OnlineUserService onlineUserService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenProperties refreshTokenProperties;

    @Override
    public void beforeCreate(ClientReq req) {
        // 请求对象同时用于新增和修改，默认值只能在新增时补齐，避免修改请求遗漏字段时覆盖旧配置。
        if (req.getIsEnableRefreshToken() == null) {
            req.setIsEnableRefreshToken(true);
        }
        if (req.getRefreshTokenTimeout() == null) {
            req.setRefreshTokenTimeout(refreshTokenProperties.getDefaultTimeout());
        }
        if (req.getRefreshTokenMode() == null) {
            req.setRefreshTokenMode(RefreshTokenModeEnum.COOKIE);
        }
        req.setClientId(SecureUtil.md5(Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY)));
    }

    @Override
    public void beforeDelete(List<Long> ids) {
        // 如果还存在在线用户，则不能删除
        OnlineUserQuery query = new OnlineUserQuery();
        List<String> clientIds = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ClientDO client = this.getById(id);
            query.setClientId(client.getClientId());
            CheckUtils.throwIfNotEmpty(onlineUserService.list(query), "客户端 [{}] 还存在在线用户，不允许删除",
                client.getClientId());
            clientIds.add(client.getClientId());
        }
        // 所有客户端都通过在线校验后，再统一撤销长期会话，避免部分撤销后删除失败。
        for (String clientId : clientIds) {
            refreshTokenService.revokeByClient(clientId);
        }
    }

    @Override
    protected void beforeUpdate(ClientReq req, Long id) {
        ClientDO client = this.getById(id);
        // 禁用客户端或关闭 Refresh Token 时立即撤销该客户端的全部长期会话。
        if (DisEnableStatusEnum.DISABLE.equals(req.getStatus())
            || Boolean.FALSE.equals(req.getIsEnableRefreshToken())) {
            refreshTokenService.revokeByClient(client.getClientId());
        }
    }

    @Override
    public ClientResp getByClientId(String clientId) {
        return baseMapper.lambdaQuery()
            .eq(ClientDO::getClientId, clientId)
            .oneOpt()
            .map(client -> BeanUtil.copyProperties(client, ClientResp.class))
            .orElse(null);
    }
}
