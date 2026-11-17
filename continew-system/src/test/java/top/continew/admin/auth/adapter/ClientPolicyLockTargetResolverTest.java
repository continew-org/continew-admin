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

package top.continew.admin.auth.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.model.entity.ClientDO;
import top.continew.starter.auth.refresh.token.api.AuthPolicyLockTarget;

import java.util.Collection;
import java.util.List;

/**
 * 客户端策略锁目标解析单元测试。
 *
 * <p>覆盖单 ID 与集合 ID 的参数提取、无效参数拒绝、空集合回退、
 * clientId 空白过滤与查询为空时的空集返回。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ClientPolicyLockTargetResolverTest {

    private ClientMapper clientMapper;
    private ClientPolicyLockTargetResolver resolver;

    @BeforeEach
    void setUp() {
        // 初始化 ClientDO 的 MyBatis-Plus Lambda 列缓存，避免构造 LambdaQueryWrapper 时找不到实体元数据
        MapperBuilderAssistant assistant =
            new MapperBuilderAssistant(new MybatisConfiguration(), ClientDO.class.getName());
        TableInfoHelper.initTableInfo(assistant, ClientDO.class);
        clientMapper = mock(ClientMapper.class);
        resolver = new ClientPolicyLockTargetResolver(clientMapper);
    }

    @Test
    void shouldResolveSingleLongArgToClientTarget() {
        stubClients(List.of(client("admin_client")));
        when(clientMapper.selectList(any())).thenReturn(List.of(client("admin_client")));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {5L});

        assertEquals(1, targets.size());
        AuthPolicyLockTarget target = targets.iterator().next();
        assertEquals("admin_client", target.key());
        assertEquals(AuthPolicyLockTarget.Type.CLIENT, target.type());
    }

    @Test
    void shouldResolveCollectionArgOfLongs() {
        when(clientMapper.selectList(any())).thenReturn(List.of(client("c1"), client("c2")));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {"x", List.of(1L,
            2L)});

        assertEquals(2, targets.size());
    }

    @Test
    void shouldFallThroughToNextArgWhenCollectionHasNoLongs() {
        stubClients(List.of(client("c9")));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {"not-long",
            List.of("str"), 9L});

        assertEquals(1, targets.size());
    }

    @Test
    void shouldRejectWhenNoUsableArg() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(new Object[] {"text",
            "more"}));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(new Object[] {}));
    }

    @Test
    void shouldFilterBlankClientIds() {
        when(clientMapper.selectList(any())).thenReturn(List.of(client("  "), client(null),
            client("valid")));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {1L});

        assertEquals(1, targets.size());
    }

    @Test
    void shouldReturnEmptyWhenNoClientRows() {
        when(clientMapper.selectList(any())).thenReturn(List.of());

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {1L});

        assertEquals(0, targets.size());
    }

    private ClientDO client(String clientId) {
        ClientDO client = new ClientDO();
        client.setClientId(clientId);
        return client;
    }

    private void stubClients(List<ClientDO> clients) {
        Mockito.lenient().when(clientMapper.selectList(any())).thenReturn(clients);
    }
}
