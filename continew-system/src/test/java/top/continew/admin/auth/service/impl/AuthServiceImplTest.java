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

package top.continew.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.auth.LoginHandler;
import top.continew.admin.auth.LoginHandlerFactory;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证业务流程测试。
 *
 * <p>覆盖客户端校验（不存在/禁用/未授权认证类型）、处理器缺失报错与登录前置、
 * 登录、后置的调用顺序。处理器与客户端服务用 Mockito 模拟，不依赖 Spring 上下文。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class AuthServiceImplTest {

    private ClientService clientService;
    private LoginHandler<LoginReq> loginHandler;
    private LoginHandlerFactory loginHandlerFactory;
    private AuthServiceImpl authService;
    private MockedStatic<cn.hutool.extra.spring.SpringUtil> springUtilMock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // ValidationUtils 静态块经 SpringUtil 取 jakarta Validator，纯单测下桩一个恒通过的校验器
        springUtilMock = Mockito.mockStatic(cn.hutool.extra.spring.SpringUtil.class);
        jakarta.validation.Validator jakartaValidator = mock(jakarta.validation.Validator.class);
        Mockito.when(jakartaValidator.validate(any())).thenReturn(java.util.Set.of());
        springUtilMock.when(() -> cn.hutool.extra.spring.SpringUtil
            .getBean(jakarta.validation.Validator.class)).thenReturn(jakartaValidator);
        clientService = mock(ClientService.class);
        loginHandler = mock(LoginHandler.class);
        Mockito.doReturn(AuthTypeEnum.ACCOUNT).when(loginHandler).getAuthType();
        loginHandlerFactory = new LoginHandlerFactory(java.util.List.of(loginHandler));
        authService =
            new AuthServiceImpl(loginHandlerFactory, clientService, mock(RoleService.class),
                mock(MenuService.class), mock(CrudProperties.class));
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    private static ClientResp client(DisEnableStatusEnum status) {
        ClientResp client = new ClientResp();
        client.setAuthType(java.util.List.of(AuthTypeEnum.ACCOUNT.getValue()));
        client.setStatus(status);
        return client;
    }

    private static LoginReq loginReq() {
        LoginReq req = new LoginReq();
        req.setAuthType(AuthTypeEnum.ACCOUNT);
        return req;
    }

    @Test
    void shouldThrowWhenClientNotExists() {
        when(clientService.getByClientId("not-exist")).thenReturn(null);
        LoginReq req = loginReq();
        req.setClientId("not-exist");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService
            .login(req, mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

        assertEquals("客户端不存在", exception.getMessage());
        verify(loginHandler, never()).login(any(), any(), any(), any());
    }

    @Test
    void shouldThrowWhenClientDisabled() {
        when(clientService.getByClientId("client-1"))
            .thenReturn(client(DisEnableStatusEnum.DISABLE));
        LoginReq req = loginReq();
        req.setClientId("client-1");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService
            .login(req, mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

        assertEquals("客户端已禁用", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAuthTypeNotAuthorized() {
        when(clientService.getByClientId("client-1"))
            .thenReturn(client(DisEnableStatusEnum.ENABLE));
        LoginReq req = loginReq();
        req.setAuthType(AuthTypeEnum.EMAIL);
        req.setClientId("client-1");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService
            .login(req, mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

        assertEquals("该客户端暂未授权 [邮箱] 认证", exception.getMessage());
    }

    @Test
    void shouldThrowWhenHandlerMissing() {
        // 客户端授权了 PHONE，但工厂只注册了 ACCOUNT 处理器
        ClientResp authorizedClient = client(DisEnableStatusEnum.ENABLE);
        authorizedClient
            .setAuthType(java.util.List.of(AuthTypeEnum.ACCOUNT.getValue(), AuthTypeEnum.PHONE
                .getValue()));
        when(clientService.getByClientId("client-1")).thenReturn(authorizedClient);
        LoginReq req = loginReq();
        req.setAuthType(AuthTypeEnum.PHONE);
        req.setClientId("client-1");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService
            .login(req, mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

        assertEquals("不支持的认证类型 [手机号]", exception.getMessage());
    }

    @Test
    void shouldRunPreLoginLoginPostLoginInOrder() {
        when(clientService.getByClientId("client-1"))
            .thenReturn(client(DisEnableStatusEnum.ENABLE));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        LoginResp expected = LoginResp.builder()
            .accessToken("token")
            .build();
        LoginReq req = loginReq();
        req.setClientId("client-1");
        ClientResp client = clientService.getByClientId("client-1");
        Mockito.doReturn(expected).when(loginHandler).login(Mockito.same(req), Mockito
            .same(client), Mockito.same(request), Mockito.same(response));

        LoginResp resp = authService.login(req, request, response);

        assertEquals(expected, resp);
        org.mockito.InOrder inOrder = Mockito.inOrder(loginHandler);
        inOrder.verify(loginHandler).preLogin(Mockito.same(req), Mockito.any(), Mockito
            .same(request));
        inOrder.verify(loginHandler).login(Mockito.same(req), Mockito.any(), Mockito
            .same(request), Mockito.same(response));
        inOrder.verify(loginHandler).postLogin(Mockito.same(req), Mockito.any(), Mockito
            .same(request));
    }
}
