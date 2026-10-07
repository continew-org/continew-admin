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

package top.continew.admin.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.impl.OnlineUserServiceImpl;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 在线用户以 Refresh Session 为事实源测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class OnlineUserServiceImplTest {

    @Test
    void shouldListRefreshSessionAfterAccessTokenExpires() {
        SessionInvalidationService sessionInvalidationService =
            mock(SessionInvalidationService.class);
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        RefreshSession refreshSession = new RefreshSession();
        refreshSession.setSessionId("session-1");
        refreshSession.setUserId(1L);
        refreshSession.setUsername("tester");
        refreshSession.setNickname("Tester");
        refreshSession.setClientId("web");
        refreshSession.setClientType("WEB");
        refreshSession.setCreatedAt(1_767_262_400_000L);
        refreshSession.setLastRefreshAt(1_767_266_000_000L);
        SessionView session = SessionView.from(refreshSession);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(session));

        OnlineUserServiceImpl service = new OnlineUserServiceImpl(sessionInvalidationService,
            sessionQueryService);
        try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
            TenantContextHolder.class)) {
            tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);

            List<OnlineUserResp> result = service.list(new OnlineUserQuery());

            assertEquals(1, result.size());
            assertEquals("session-1", result.get(0).getSessionId());
            assertEquals(toLocalDateTime(session.getCreatedAt()), result.get(0).getLoginTime());
            assertEquals(toLocalDateTime(session.getLastRefreshAt()),
                result.get(0).getLastRefreshTime());
        }
    }

    private LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
