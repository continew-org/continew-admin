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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.model.entity.ClientDO;
import top.continew.admin.system.model.req.ClientReq;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;

/**
 * 客户端业务实现单元测试。
 *
 * <p>覆盖 clientId 生成、闲置失效阈值拦截、删除前在线用户校验与会话统一撤销、
 * 更新后撤销会话，以及按 clientId 查询的命中与未命中。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ClientServiceImplTest {

    private static final Long CLIENT_ID = 3L;

    private ClientMapper clientMapper;
    private OnlineUserService onlineUserService;
    private SessionInvalidationService sessionInvalidationService;
    private ClientServiceImpl clientService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(mock(Validator.class));
        clientMapper = mock(ClientMapper.class);
        onlineUserService = mock(OnlineUserService.class);
        sessionInvalidationService = mock(SessionInvalidationService.class);
        clientService = new ClientServiceImpl(onlineUserService, sessionInvalidationService);
        ReflectionTestUtils.setField(clientService, "baseMapper", clientMapper);
        // 父类 getById 经实体类缓存推导类型，mock Mapper 缺代理元数据，预置绕开
        ReflectionTestUtils.setField(clientService, "entityClass", ClientDO.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldGenerateMd5ClientIdOnCreate() {
        ClientReq req = req(null);

        clientService.beforeCreate(req);

        assertNotNull(req.getClientId());
        assertEquals(32, req.getClientId().length());
    }

    @Test
    void shouldRejectIdleTimeoutBetweenOneAndFiftyNine() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.beforeCreate(req(30L)));

        assertEquals("Refresh Token 闲置失效需为 0（不限制）或不小于 60 秒", exception.getMessage());
    }

    @Test
    void shouldAcceptZeroAndUnlimitedIdleTimeout() {
        clientService.beforeCreate(req(0L));
        clientService.beforeCreate(req(null));
        clientService.beforeCreate(req(60L));
    }

    @Test
    void shouldRejectDeleteWhenOnlineUsersExist() {
        when(clientMapper.selectById(CLIENT_ID)).thenReturn(client("cid-1"));
        when(onlineUserService.list(any(OnlineUserQuery.class)))
            .thenReturn(List.of(mock(OnlineUserResp.class)));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.beforeDelete(List.of(CLIENT_ID)));

        assertEquals("客户端 [cid-1] 还存在在线用户，不允许删除", exception.getMessage());
        verify(sessionInvalidationService, never()).invalidateClient(any());
    }

    @Test
    void shouldInvalidateSessionsOnlyAfterAllClientsPass() {
        when(clientMapper.selectById(1L)).thenReturn(client("cid-1"));
        when(clientMapper.selectById(2L)).thenReturn(client("cid-2"));
        when(onlineUserService.list(any(OnlineUserQuery.class))).thenReturn(List.of());

        clientService.beforeDelete(List.of(1L, 2L));

        verify(sessionInvalidationService).invalidateClient("cid-1");
        verify(sessionInvalidationService).invalidateClient("cid-2");
    }

    @Test
    void shouldInvalidateSessionAfterUpdate() {
        clientService.afterUpdate(req(null), client("cid-1"));

        verify(sessionInvalidationService).invalidateClient("cid-1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnRespWhenFoundByClientId() {
        LambdaQueryChainWrapper<ClientDO> chain = mock(LambdaQueryChainWrapper.class);
        when(clientMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.oneOpt()).thenReturn(Optional.of(client("cid-1")));

        ClientResp result = clientService.getByClientId("cid-1");

        assertNotNull(result);
        assertEquals("cid-1", result.getClientId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnNullWhenNotFoundByClientId() {
        LambdaQueryChainWrapper<ClientDO> chain = mock(LambdaQueryChainWrapper.class);
        when(clientMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.oneOpt()).thenReturn(Optional.empty());

        assertNull(clientService.getByClientId("not-exist"));
    }

    private ClientReq req(Long idleTimeout) {
        ClientReq req = new ClientReq();
        req.setClientType("web");
        req.setRefreshTokenIdleTimeout(idleTimeout);
        return req;
    }

    private ClientDO client(String clientId) {
        ClientDO client = new ClientDO();
        client.setClientId(clientId);
        return client;
    }
}
