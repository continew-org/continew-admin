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

package top.continew.admin.system.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.api.tenant.PackageMenuApi;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.mapper.MessageLogMapper;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.mapper.RoleDeptMapper;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.mapper.RoleMenuMapper;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.mapper.user.UserPasswordHistoryMapper;
import top.continew.admin.system.mapper.user.UserSocialMapper;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.List;

/**
 * 租户数据清理的删除目标回归测试。
 *
 * <p>{@code clear()} 需要分别清理消息表与消息日志表。两处删除若误用同一个 Mapper 类型，
 * 消息日志表的数据会残留。测试以纯 Mockito 构造实现类的全部构造器依赖，断言两个不同类型的
 * Mapper 各被删除一次，锁定清理目标与字段类型。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class TenantDataApiClearTest {

    @Test
    void shouldDeleteMessageAndMessageLogOnce() {
        MessageMapper messageMapper = mock(MessageMapper.class);
        MessageLogMapper messageLogMapper = mock(MessageLogMapper.class);
        // 文件列表为空时 clear() 不会调用 fileService.delete，避免进入真实文件删除逻辑
        FileService fileService = mock(FileService.class);
        when(fileService.list()).thenReturn(List.of());

        TenantDataApiForSystemImpl api = new TenantDataApiForSystemImpl(
            mock(PackageMenuApi.class), mock(TenantApi.class), mock(UserRoleService.class),
            fileService, mock(RoleMenuService.class), mock(DeptMapper.class),
            mock(RoleMapper.class), mock(RoleMenuMapper.class), mock(LogMapper.class),
            messageMapper, messageLogMapper, mock(NoticeMapper.class), mock(RoleDeptMapper.class),
            mock(UserMapper.class), mock(UserPasswordHistoryMapper.class),
            mock(UserRoleMapper.class), mock(UserSocialMapper.class),
            mock(SessionInvalidationService.class));

        // TenantContextHolder 是静态租户上下文，L1 单测不启动 Spring，需替换其取值
        try (MockedStatic<TenantContextHolder> tenantContextHolder =
            mockStatic(TenantContextHolder.class)) {
            tenantContextHolder.when(TenantContextHolder::getTenantId).thenReturn(0L);

            api.clear();
        }

        verify(messageMapper, times(1)).delete(any());
        verify(messageLogMapper, times(1)).delete(any());
    }
}
